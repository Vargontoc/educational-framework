package es.vargontoc.educational.framework.content.model;

public record WorldNarrativeSituationProjection(
    Long id,
    String code,
    String displayText,
    SituationType situationType,
    Tone tone,
    Integer minAge,
    Integer maxAge,
    Integer sortOrder
) {}
