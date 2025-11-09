package com.shifter.freight_service.controllers;

import com.shifter.freight_service.configs.web_socket.StompPrincipal;
import com.shifter.freight_service.dto.ChatMessageDTO;
import com.shifter.freight_service.models.ChatMessage;
import com.shifter.freight_service.payloads.responses.ChatMessageResponse;
import com.shifter.freight_service.services.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatMessageController {

    private final SimpMessagingTemplate template;
    private final ChatMessageService service;

    /**
     * Clients should SEND to: /api/v1/app/chat.send
     */
    @MessageMapping("/chat.send")
    public void sendMessage(ChatMessageDTO incoming, SimpMessageHeaderAccessor headerAccessor) {
        var principal = headerAccessor.getUser() instanceof StompPrincipal sp ? sp : null;
        if (principal == null) {
            log.warn("Rejecting message: missing principal. incoming={}", incoming);
            return;
        }
        log.debug("Incoming chat.send from user={} headers={}", principal.getName(),
                headerAccessor.getMessageHeaders());

        var user = principal.getAuthUser();
        Long senderId = user.getId();

        ChatMessage msg = ChatMessage.builder()
                .message(incoming.getMessage())
                .senderId(senderId)
                .receiverId(incoming.getReceiverId())
                .build();

        if (incoming.getParentId() != null) {
            ChatMessage parent = new ChatMessage();
            parent.setId(incoming.getParentId());
            msg.setParent(parent);
        }

        ChatMessage saved = service.save(msg, incoming.getRequestId(), senderId);

        Long responseRequestId = saved.getRequest() != null ? saved.getRequest().getId() : incoming.getRequestId();

        ChatMessageResponse out = new ChatMessageResponse(
                saved.getId(),
                responseRequestId,
                saved.getSenderId(),
                saved.getReceiverId(),
                saved.getMessage(),
                saved.getCreatedAt()
        );

        String requestTopic = "/api/v1/topic/request." + (responseRequestId == null ? "global" : responseRequestId);
        template.convertAndSend(requestTopic, out);

        if (out.getReceiverId() != null) {
            template.convertAndSendToUser(String.valueOf(out.getReceiverId()), "/queue/messages", out);
        }
    }
}
