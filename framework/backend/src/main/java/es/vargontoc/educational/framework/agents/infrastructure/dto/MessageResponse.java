package es.vargontoc.educational.framework.agents.infrastructure.dto;

import java.time.LocalDateTime;

public record MessageResponse(
    String role,
    String content,
    LocalDateTime createdAt
) {
    
}
