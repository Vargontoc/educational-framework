package es.vargontoc.educational.framework.content.model;

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
