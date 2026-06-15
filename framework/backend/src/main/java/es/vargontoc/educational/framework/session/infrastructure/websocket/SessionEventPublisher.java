package es.vargontoc.educational.framework.session.infrastructure.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class SessionEventPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionEventPublisher.class);
    private static final String PARENT_TOPIC_PREFIX = "/topic/family/";
    private static final String PARENT_TOPIC_SUFFIX = "/sessions";

    private final SimpMessagingTemplate messagingTemplate;
    private final GameWebSocketHandler gameWebSocketHandler;
    private final ObjectMapper objectMapper;

    public SessionEventPublisher(
            SimpMessagingTemplate messagingTemplate,
            GameWebSocketHandler gameWebSocketHandler,
            ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.gameWebSocketHandler = gameWebSocketHandler;
        this.objectMapper = objectMapper;
    }

    public void notifyParent(Long familyId, SessionEvent event) {
        String destination = PARENT_TOPIC_PREFIX + familyId + PARENT_TOPIC_SUFFIX;
        try {
            String payload = objectMapper.writeValueAsString(event);
            messagingTemplate.convertAndSend(destination, payload);
            LOGGER.debug("STOMP event sent to {}: {}", destination, event.event());
        } catch (JsonProcessingException exception) {
            LOGGER.error("Failed to serialize session event: {}", exception.getMessage());
        }
    }

    public void notifyChild(Long childSessionId, SessionEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            boolean sent = gameWebSocketHandler.sendToSession(childSessionId, payload);
            if (!sent) {
                LOGGER.debug("No active game WebSocket for childSessionId={}, event dropped: {}",
                    childSessionId, event.event());
            }
        } catch (JsonProcessingException exception) {
            LOGGER.error("Failed to serialize session event: {}", exception.getMessage());
        }
    }

    public void notifyChildAndParent(Long childSessionId, Long familyId, SessionEvent event) {
        notifyChild(childSessionId, event);
        notifyParent(familyId, event);
    }

    public void notifyChildWithFarewellAndParent(Long childSessionId, Long familyId, SessionEvent event) {
        gameWebSocketHandler.sendFarewellAndClose(childSessionId);
        notifyParent(familyId, event);
    }
}
