    package com.shifter.freight_service.configs.web_socket;

    import com.shifter.freight_service.clients.AuthServiceClient;
    import com.shifter.freight_service.payloads.responses.AuthUserResponse;
    import jakarta.servlet.http.HttpServletRequest;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.http.server.ServerHttpRequest;
    import org.springframework.http.server.ServerHttpResponse;
    import org.springframework.http.server.ServletServerHttpRequest;
    import org.springframework.stereotype.Component;
    import org.springframework.web.socket.WebSocketHandler;
    import org.springframework.web.socket.server.HandshakeInterceptor;

    import java.util.Map;

    @Component
    @RequiredArgsConstructor
    @Slf4j
    public class AuthHandshakeInterceptor implements HandshakeInterceptor {

        private final AuthServiceClient authServiceClient;
        public static final String ATTR_AUTH_USER = "AUTH_USER";

        @Override
        public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

            if (!(request instanceof ServletServerHttpRequest servletReq)) {
                log.warn("Handshake request not a servlet request -> rejecting...");
                return false;
            }
            HttpServletRequest httpServletRequest = servletReq.getServletRequest();

    //        try Authorization header first
            String authHeader = httpServletRequest.getHeader("Authorization");
            if (authHeader == null && authHeader.isBlank()) {
                authHeader = httpServletRequest.getParameter("token");
            }
            if (authHeader == null && authHeader.isBlank()) {
                log.debug("No Authorization provided in handshake -> rejecting...");
                return false;
            }

            try{
                // this uses your provided AuthServiceClient (which throws on failure)
                AuthUserResponse user = authServiceClient.getCurrentUser(authHeader);
                if (user == null) {
                    log.debug("Auth service returned null user during handshake");
                    return false;
                }
                // store auth user in attributes for later retrieval
                attributes.put(ATTR_AUTH_USER, user);
                log.debug("Handshake accepted for user id={}", user.getId());
                return true;
            } catch (Exception ex) {
                log.debug("Handshake authentication failed:{} ", ex.getMessage());
                return false;
            }
        }

        @Override
        public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

        }
    }
