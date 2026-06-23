package es.vargontoc.educational.framework.content.infrastructure.dto;

import es.vargontoc.educational.framework.content.model.SituationType;
import es.vargontoc.educational.framework.content.model.Tone;

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
