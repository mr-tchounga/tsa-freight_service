package com.shifter.freight_service.configs.web_socket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.broker.BrokerAvailabilityEvent;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Component
@Slf4j
public class WebSocketEventListener {

    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        if (sha.getUser() != null) {
            log.info("WebSocket CONNECT user: {}", sha.getUser().getName());
        } else {
            log.info("WebSocket CONNECT (anonymous)");
        }
    }

    @EventListener
    public void handleSessionDisconnect(SessionConnectedEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        if (sha.getUser() != null) {
            log.info("WebSocket DISCONNECT user: {}", sha.getUser().getName());
        } else {
            log.info("WebSocket DISCONNECT (anonymous)");
        }
    }

    @EventListener
    public void handleBrokerAvailability(BrokerAvailabilityEvent event) {
        log.info("Broker available={}", event.isBrokerAvailable());
    }

}
