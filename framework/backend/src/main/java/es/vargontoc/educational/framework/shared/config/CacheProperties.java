package es.vargontoc.educational.framework.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cache")
public record CacheProperties(
    int recognitionElementMaxSize,
    int recognitionElementTtlMinutes,
    int accessibleColorMaxSize,
    int accessibleColorTtlMinutes,
    int worldCatalogMaxSize,
    int worldCatalogTtlMinutes,
    int avatarEventMaxSize,
    int avatarEventTtlMinutes,
    int childProfileMaxSize,
    int childProfileTtlMinutes
) {
    public CacheProperties {
        if (recognitionElementMaxSize <= 0) recognitionElementMaxSize = 512;
        if (recognitionElementTtlMinutes <= 0) recognitionElementTtlMinutes = 30;
        if (accessibleColorMaxSize <= 0) accessibleColorMaxSize = 256;
        if (accessibleColorTtlMinutes <= 0) accessibleColorTtlMinutes = 60;
        if (worldCatalogMaxSize <= 0) worldCatalogMaxSize = 64;
        if (worldCatalogTtlMinutes <= 0) worldCatalogTtlMinutes = 15;
        if (avatarEventMaxSize <= 0) avatarEventMaxSize = 128;
        if (avatarEventTtlMinutes <= 0) avatarEventTtlMinutes = 30;
        if (childProfileMaxSize <= 0) childProfileMaxSize = 16;
        if (childProfileTtlMinutes <= 0) childProfileTtlMinutes = 10;
    }
}
