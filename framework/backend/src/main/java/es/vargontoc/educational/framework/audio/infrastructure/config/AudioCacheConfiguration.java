package es.vargontoc.educational.framework.audio.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración de la cache de audio
 * AudioCacheConfiguration
 * @param maxEntries 
 * @param expireAfterWriteMinutes
 * @param maxDiskEntries
 */
@ConfigurationProperties(prefix = "app.audio.cache")
public record AudioCacheConfiguration(
    int maxEntries,
    int expireAfterWriteMinutes,
    int maxDiskEntries) { }
