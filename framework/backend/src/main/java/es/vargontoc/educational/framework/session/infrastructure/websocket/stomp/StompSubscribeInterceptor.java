package es.vargontoc.educational.framework.session.infrastructure.websocket.stomp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class StompSubscribeInterceptor implements ChannelInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(StompSubscribeInterceptor.class);
    private static final Pattern FAMILY_TOPIC_PATTERN = Pattern.compile("^/topic/family/(\\d+)/(sessions|chatbot)$");

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || !StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            return message;
        }

        String destination = accessor.getDestination();
        if (destination == null) {
            return message;
        }

        Matcher matcher = FAMILY_TOPIC_PATTERN.matcher(destination);
        if (!matcher.matches()) {
            return message;
        }

        Long requestedFamilyId = Long.parseLong(matcher.group(1));
        Long authenticatedFamilyId = getAuthenticatedFamilyId(accessor);

        if (authenticatedFamilyId == null) {
            LOGGER.warn("STOMP SUBSCRIBE rejected: no authenticated family on session");
            return null;
        }

        if (!requestedFamilyId.equals(authenticatedFamilyId)) {
            LOGGER.warn("STOMP SUBSCRIBE rejected: familyId={} attempted to subscribe to familyId={}",
                authenticatedFamilyId, requestedFamilyId);
            return null;
        }

        return message;
    }

    private Long getAuthenticatedFamilyId(StompHeaderAccessor accessor) {
        Object attr = accessor.getSessionAttributes() != null
            ? accessor.getSessionAttributes().get(StompConnectAuthInterceptor.ATTR_FAMILY_ID)
            : null;
        return attr instanceof Long id ? id : null;
    }
}
