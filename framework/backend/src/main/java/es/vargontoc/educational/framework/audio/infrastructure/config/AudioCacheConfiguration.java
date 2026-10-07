package es.vargontoc.educational.framework.audio.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.audio.cache")
public record AudioCacheConfiguration(
    int maxEntries,
    int expireAfterWriteMinutes,
    int maxDiskEntries,
    String diskPath,
    int enforceCapacityIntervalSeconds) { }
