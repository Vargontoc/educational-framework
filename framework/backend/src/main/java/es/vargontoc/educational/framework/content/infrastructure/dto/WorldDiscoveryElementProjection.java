package es.vargontoc.educational.framework.content.infrastructure.dto;

import es.vargontoc.educational.framework.content.model.Biome;
import es.vargontoc.educational.framework.content.model.ElementType;
import es.vargontoc.educational.framework.content.model.InteractionCueType;

public record WorldDiscoveryElementProjection(
    Long id,
    String code,
    String displayName,
    ElementType elementType,
    Biome biome,
    Integer minAge,
    Integer maxAge,
    Long activityId,
    Long topicId,
    String visualAssetKey,
    InteractionCueType interactionCueType,
    Integer sortOrder
) {}
