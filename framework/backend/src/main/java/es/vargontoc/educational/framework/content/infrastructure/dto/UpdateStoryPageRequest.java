package es.vargontoc.educational.framework.content.infrastructure.dto;

public record UpdateStoryPageRequest(
    Integer pageOrder,
    String text,
    String imageResourceRef,
    String audioResourceRef,
    String status
) {}
