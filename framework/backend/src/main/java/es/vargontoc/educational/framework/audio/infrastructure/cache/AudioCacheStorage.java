package es.vargontoc.educational.framework.audio.infrastructure.cache;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Comparator;
import java.util.stream.Stream;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import es.vargontoc.educational.framework.audio.domain.AudioCache;
import es.vargontoc.educational.framework.audio.infrastructure.config.AudioCacheConfiguration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AudioCacheStorage {
    
    private static final String CACHE_FILE_EXTENSION = ".wav";

    private final Path cachePath;
    private final AudioCacheConfiguration properties;
    private final Cache<AudioCache, byte[]> internalCache;

    public AudioCacheStorage(Path cachePath, AudioCacheConfiguration properties) {
        this.properties = properties;
        this.cachePath = validateAndPrepareCachePath(cachePath);
        this.internalCache = Caffeine.newBuilder()
            .maximumSize(properties.maxEntries())
            .expireAfterWrite(Duration.ofMinutes(properties.expireAfterWriteMinutes()))
            .build();
    }

    private Path validateAndPrepareCachePath(Path configuredPath) {
        try {
            if (!Files.exists(configuredPath)) {
                Files.createDirectories(configuredPath);
            }
            if (!Files.isDirectory(configuredPath)) {
                throw new IllegalStateException("Audio cache path is not a directory: " + configuredPath);
            }
            Path probe = configuredPath.resolve(".startup-write-check");
            Files.write(probe, new byte[]{0});
            Files.deleteIfExists(probe);
            log.info("Audio cache disk path validated: {}", configuredPath.toAbsolutePath());
            return configuredPath;
        } catch (IOException e) {
            throw new IllegalStateException("Invalid audio cache path '" + configuredPath + "': " + e.getMessage(), e);
        }
    }

    public byte[] get(AudioCache key) {
        byte[] result = internalCache.getIfPresent(key);
        if (result != null) {
            log.debug("Internal cache hit: {}", key);
            return result;
        }

        result = loadFromDisk(key);
        if (result != null) {
            log.info("Disk cache hit, promoting to internal: {}", key);
            internalCache.put(key, result);
            return result;
        }

        log.debug("Cache miss: {}", key);
        return null;
    }

    public void put(AudioCache key, byte[] data) {
        internalCache.put(key, data);
        saveToDisk(key, data);
        log.info("Cached {} bytes: {}", data.length, key);
    }

    public void remove(AudioCache key) {
        if (internalCache.getIfPresent(key) != null) {
            internalCache.invalidate(key);
        }
        Path file = resolveDiskPath(key);
        if (Files.exists(file)) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException e) {
                log.error("Could not delete cache file '{}': {}", file, e.getMessage());
            }
        }
    }

    public void enforceDiskCapacity() {
        try (Stream<Path> stream = Files.list(cachePath)) {
            var files = stream
                .filter(p -> p.toString().endsWith(CACHE_FILE_EXTENSION))
                .toList();
            if (files.size() <= properties.maxDiskEntries()) {
                return;
            }
            int toDelete = files.size() - properties.maxDiskEntries();
            files.stream()
                .sorted(Comparator.comparingLong(f -> {
                    try {
                        return Files.getLastModifiedTime(f).toMillis();
                    } catch (IOException e) {
                        return 0L;
                    }
                }))
                .limit(toDelete)
                .forEach(f -> {
                    try {
                        Files.delete(f);
                    } catch (IOException e) {
                        log.warn("Failed to delete old cache file {}", f);
                    }
                });
            log.info("Cleaned {} old cache files from disk cache", toDelete);
        } catch (IOException e) {
            log.error("Failed to enforce disk capacity: {}", e.getMessage());
        }
    }

    public Path getCachePath() {
        return cachePath;
    }

    private void saveToDisk(AudioCache key, byte[] audio) {
        Path file = resolveDiskPath(key);
        try {
            Files.write(file, audio);
        } catch (IOException e) {
            log.error("Error writing cache file {}: {}", file, e.getMessage());
        }
    }

    private byte[] loadFromDisk(AudioCache key) {
        Path file = resolveDiskPath(key);
        if (!Files.exists(file)) {
            return null;
        }
        try {
            byte[] data = Files.readAllBytes(file);
            if (data == null || data.length == 0) {
                Files.deleteIfExists(file);
                return null;
            }
            return data;
        } catch (IOException e) {
            log.error("Error reading cache file {}: {}", file, e.getMessage());
            try {
                Files.deleteIfExists(file);
            } catch (IOException e1) {
                // ignored
            }
            return null;
        }
    }

    private Path resolveDiskPath(AudioCache key) {
        return cachePath.resolve(key.getTextHashSha256() + CACHE_FILE_EXTENSION);
    }
}
