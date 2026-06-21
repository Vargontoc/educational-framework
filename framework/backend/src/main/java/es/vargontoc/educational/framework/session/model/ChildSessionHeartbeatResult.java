package es.vargontoc.educational.framework.session.model;

import java.time.LocalDateTime;

public record ChildSessionHeartbeatResult(
    boolean active,
    String status,
    LocalDateTime lastActivityAt
) {
}
