package es.vargontoc.educational.framework.content.model;

public record WorldHostProjection(
    Long id,
    String code,
    String displayName,
    Biome biome,
    String description,
    Integer minAge,
    Integer maxAge,
    String visualAssetKey,
    Integer sortOrder
) {}
