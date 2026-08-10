package es.vargontoc.educational.framework.agents.infrastructure.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ConversationResponse(
    UUID conversationId,
    LocalDateTime startedAt, 
    LocalDateTime lastMessageAt, 
    List<MessageResponse> message) {
    
}
