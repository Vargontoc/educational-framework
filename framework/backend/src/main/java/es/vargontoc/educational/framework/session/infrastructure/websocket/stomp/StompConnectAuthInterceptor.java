package es.vargontoc.educational.framework.session.infrastructure.websocket.stomp;

import es.vargontoc.educational.framework.session.model.FamilySession;
import es.vargontoc.educational.framework.session.ports.in.FamilySessionUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
public class StompConnectAuthInterceptor implements ChannelInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(StompConnectAuthInterceptor.class);

    public static final String ATTR_FAMILY_ID = "familyId";
    public static final String ATTR_SESSION_ID = "familySessionId";

    private final FamilySessionUseCase familySessionUseCase;

    public StompConnectAuthInterceptor(FamilySessionUseCase familySessionUseCase) {
        this.familySessionUseCase = familySessionUseCase;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        String token = extractBearerToken(accessor);
        if (token == null) {
            LOGGER.warn("STOMP CONNECT rejected: no Bearer token");
            return null;
        }

        try {
            FamilySession session = familySessionUseCase.getByToken(token);
            if (accessor.getSessionAttributes() != null) {
                accessor.getSessionAttributes().put(ATTR_FAMILY_ID, session.getFamilyId());
                accessor.getSessionAttributes().put(ATTR_SESSION_ID, session.getId());
            }
            LOGGER.debug("STOMP CONNECT accepted for familyId={}", session.getFamilyId());
            return message;
        } catch (Exception exception) {
            LOGGER.warn("STOMP CONNECT rejected: invalid token - {}", exception.getMessage());
            return null;
        }
    }

    private static String extractBearerToken(StompHeaderAccessor accessor) {
        String authorization = accessor.getFirstNativeHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring("Bearer ".length());
        }
        return null;
    }
}
