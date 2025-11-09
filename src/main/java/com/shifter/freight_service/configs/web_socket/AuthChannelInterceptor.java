package com.shifter.freight_service.configs.web_socket;

import com.shifter.freight_service.clients.AuthServiceClient;
import com.shifter.freight_service.models.Request;
import com.shifter.freight_service.payloads.responses.AuthUserResponse;
import com.shifter.freight_service.repositories.ChatMessageRepository;
import com.shifter.freight_service.repositories.RequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthChannelInterceptor implements ChannelInterceptor {

    private final AuthServiceClient authServiceClient;
    private final RequestRepository requestRepository;
    private final ChatMessageRepository chatMessageRepository;

    // pattern for topics like /api/v1/topic/request.123
    private static final Pattern REQUEST_TOPIC_PATTERN = Pattern.compile("^/api/v1/topic/request\\.(\\d+)$");

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            List<String> authHeaders = accessor.getNativeHeader("Authorization");
            String auth = (authHeaders != null && !authHeaders.isEmpty()) ? authHeaders.get(0) : null;
            if (auth == null || auth.isBlank()) {
                List<String> t = accessor.getNativeHeader("token");
                auth = (t != null && !t.isEmpty()) ? t.get(0) : null;
            }
            if (auth == null || auth.isBlank()) {
                log.debug("CONNECT without token -> reject");
                return null;
            }
            try {
                AuthUserResponse user = authServiceClient.getCurrentUser(auth);
                if (user == null) {
                    log.debug("CONNECT: auth service returned null -> reject");
                    return null;
                }
                accessor.setUser(new StompPrincipal(user));
                log.debug("CONNECT accepted for user {}", user.getId());
            } catch (Exception ex) {
                log.debug("CONNECT authentication failed: {}", ex.getMessage());
                return null;
            }
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String dest = accessor.getDestination();
            if (dest != null) {
                Matcher m = REQUEST_TOPIC_PATTERN.matcher(dest);
                if (m.matches()) {
                    long requestId = Long.parseLong(m.group(1));
                    var principal = accessor.getUser() instanceof StompPrincipal sp ? sp : null;
                    if (principal == null) {
                        log.debug("SUBSCRIBE to {} without principal -> reject", dest);
                        return null;
                    }
                    AuthUserResponse user = principal.getAuthUser();
                    // admin bypass
                    if (user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole().getName())) {
                        return message;
                    }
                    long userId = user.getId();

                    // Allowed if user created the request
                    boolean allowed = requestRepository.findById(requestId)
                            .map(r -> r.getCreatedBy() != null && r.getCreatedBy().equals(userId))
                            .orElse(false);

                    // Or allowed if the user already participated (messages sent/received) in that request
                    if (!allowed) {
                        long cnt = chatMessageRepository.countByRequestIdAndUserId(requestId, userId);
                        allowed = cnt > 0;
                    }

                    if (!allowed) {
                        log.debug("SUBSCRIBE to {} denied for user {}", dest, userId);
                        return null;
                    } else {
                        log.debug("SUBSCRIBE to {} accepted for user {}", dest, userId);
                    }
                }
            }
        }

        return message;
    }
}
