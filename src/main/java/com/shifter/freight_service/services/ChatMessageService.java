package com.shifter.freight_service.services;

import com.shifter.freight_service.models.ChatMessage;
import com.shifter.freight_service.models.Request;
import com.shifter.freight_service.repositories.ChatMessageRepository;
import com.shifter.freight_service.repositories.RequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final RequestRepository requestRepository;

    /**
     * Save message. SenderId MUST be the authenticated user id (checked by caller).
     * If requestId provided we set the relation only if the request exists.
     */
    public ChatMessage save(ChatMessage msg, Long requestId, Long senderId) {
        try {
            if (requestId != null) {
                Optional<Request> req = requestRepository.findById(requestId);
                if (req.isPresent()) {
                    msg.setRequest(req.get());
                } else {
                    log.warn("save(): request {} not found; saving message without request", requestId);
                }
            }

            // If parentId was set as a transient object (only id), replace with reference
            if (msg.getParent() != null && msg.getParent().getId() != null) {
                Long pid = msg.getParent().getId();
                // use repository.getReferenceById to avoid loading full parent entity if not necessary
                ChatMessage parentRef = chatMessageRepository.getReferenceById(pid);
                msg.setParent(parentRef);
            }

            msg.setCreatedAt(Calendar.getInstance().getTime());
            msg.setCreatedBy(senderId);
            msg.setSenderId(senderId);

            ChatMessage saved = chatMessageRepository.save(msg);
            log.debug("Saved chat message id={} requestId={}", saved.getId(),
                    (saved.getRequest() != null ? saved.getRequest().getId() : null));
            return saved;
        } catch (Exception ex) {
            log.error("Failed to save chat message (requestId={}, sender={}): {}",
                    requestId, senderId, ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * Return messages for a request but only those where user is sender or receiver.
     */
    public List<ChatMessage> getByRequestForUser(Long requestId, Long userId) {
        return chatMessageRepository.findAllForRequestAndUser(requestId, userId);
    }
}
