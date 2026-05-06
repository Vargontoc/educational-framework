package es.vargontoc.educational.framework.session.infrastructure.dto;

public record OpenChildSessionRequest(
    Long childProfileId,
    Integer heartbeatIntervalSeconds
) {
}
