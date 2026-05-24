package es.vargontoc.educational.framework.session.infrastructure.websocket;

import es.vargontoc.educational.framework.session.model.FamilySession;
import es.vargontoc.educational.framework.session.ports.in.FamilySessionUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);

    public static final String ATTR_FAMILY_ID = "familyId";
    public static final String ATTR_SESSION_ID = "familySessionId";

    private final FamilySessionUseCase familySessionUseCase;

    public WebSocketAuthInterceptor(FamilySessionUseCase familySessionUseCase) {
        this.familySessionUseCase = familySessionUseCase;
    }

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) {
        String token = extractBearerToken(request);
        if (token == null) {
            LOGGER.warn("WebSocket handshake rejected: no Bearer token");
            return false;
        }

        try {
            FamilySession session = familySessionUseCase.getByToken(token);
            attributes.put(ATTR_FAMILY_ID, session.getFamilyId());
            attributes.put(ATTR_SESSION_ID, session.getId());
            LOGGER.debug("WebSocket handshake accepted for familyId={}", session.getFamilyId());
            return true;
        } catch (Exception exception) {
            LOGGER.warn("WebSocket handshake rejected: invalid token - {}", exception.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {
        // No-op
    }

    private static String extractBearerToken(ServerHttpRequest request) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            String authorization = servletRequest.getServletRequest().getHeader("Authorization");
            if (authorization != null && authorization.startsWith("Bearer ")) {
                return authorization.substring("Bearer ".length());
            }
        }
        return null;
    }
}
