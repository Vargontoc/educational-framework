package es.vargontoc.educational.framework.agents.infrastructure.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import es.vargontoc.educational.framework.agents.model.ChatbotStreamEvent;

@Component
public class ChatbotEventPublisher {
    
    private static final String TOPIC_PATTERN = "/topic/family/%d/chatbot";
    private final SimpMessagingTemplate messagingTemplate;

    public ChatbotEventPublisher(SimpMessagingTemplate template){
        this.messagingTemplate = template;
    }

    public void publish(Long familyId, ChatbotStreamEvent event){
        messagingTemplate.convertAndSend(String.format(TOPIC_PATTERN, familyId), event);
    }
}
