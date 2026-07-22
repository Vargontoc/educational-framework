package es.vargontoc.educational.framework.avatar.infrastructure.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AvatarAudioCache {

    private static final Logger log = LoggerFactory.getLogger(AvatarAudioCache.class);
    private static final String AUDIO_EXTENSION = ".mp3";

    private final Cache<AvatarCacheKey, byte[]> l1Cache;
    private final Path l2DiskPath;
    private final AvatarAudioCacheProperties properties;

    public AvatarAudioCache(AvatarAudioCacheProperties properties) {
        this.properties = properties;
        this.l1Cache = Caffeine.newBuilder()
            .maximumSize(properties.getMaxEntries())
            .expireAfterWrite(Duration.ofMinutes(properties.getExpireAfterWriteMinutes()))
            .build();

        this.l2DiskPath = Paths.get(properties.getDiskPath());
        ensureDiskCacheDirectory();
    }

    public byte[] get(AvatarCacheKey key) {
        byte[] l1Result = l1Cache.getIfPresent(key);
        if (l1Result != null) {
            log.debug("L1 cache hit: {}", key);
            return l1Result;
        }

        byte[] l2Result = loadFromDisk(key);
        if (l2Result != null) {
            log.debug("L2 cache hit, promoting to L1: {}", key);
            l1Cache.put(key, l2Result);
            return l2Result;
        }

        log.debug("Cache miss: {}", key);
        return null;
    }

    public void put(AvatarCacheKey key, byte[] audioData) {
        l1Cache.put(key, audioData);
        saveToDisk(key, audioData);
        log.debug("Cached {} bytes: {}", audioData.length, key);
    }

    private byte[] loadFromDisk(AvatarCacheKey key) {
        Path filePath = resolveDiskPath(key);
        if (!Files.exists(filePath)) {
            return null;
        }
        try {
            byte[] data = Files.readAllBytes(filePath);
            if (data == null || data.length == 0) {
                try { Files.deleteIfExists(filePath); } catch (IOException ignored) {}
                return null;
            }
            return data;
        } catch (IOException e) {
            log.warn("Failed to read L2 cache file {}: {}", filePath, e.getMessage());
            try { Files.deleteIfExists(filePath); } catch (IOException ignored) {}
            return null;
        }
    }

    private void saveToDisk(AvatarCacheKey key, byte[] audioData) {
        ensureDiskCacheDirectory();
        Path filePath = resolveDiskPath(key);
        try {
            Files.write(filePath, audioData);
            enforceDiskCapacity();
        } catch (IOException e) {
            log.warn("Failed to write L2 cache file {}: {}", filePath, e.getMessage());
        }
    }

    private Path resolveDiskPath(AvatarCacheKey key) {
        String encoded = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(digestKey(key))
            .replace("/", "_")
            .replace("+", "-");
        return l2DiskPath.resolve(encoded + AUDIO_EXTENSION);
    }

    private byte[] digestKey(AvatarCacheKey key) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String combined = key.locale() + "|" + key.tone() + "|" +
                             key.textHash() + "|" + key.context() + "|" +
                             key.audioFormatVersion();
            return md.digest(combined.getBytes());
        } catch (Exception e) {
            return String.valueOf(key.hashCode()).getBytes();
        }
    }

    private void ensureDiskCacheDirectory() {
        if (!Files.exists(l2DiskPath)) {
            try {
                Files.createDirectories(l2DiskPath);
            } catch (IOException e) {
                log.error("Failed to create L2 cache directory {}: {}", l2DiskPath, e.getMessage());
            }
        }
    }

    private void enforceDiskCapacity() {
        try {
            var files = Files.list(l2DiskPath)
                .filter(p -> p.toString().endsWith(AUDIO_EXTENSION))
                .toList();

            if (files.size() <= properties.getMaxDiskEntries()) {
                return;
            }

            int toDelete = files.size() - properties.getMaxDiskEntries();
            files.stream()
                .sorted(Comparator.comparingLong(f -> {
                    try { return Files.getLastModifiedTime(f).toMillis(); }
                    catch (IOException e) { return 0L; }
                }))
                .limit(toDelete)
                .forEach(f -> {
                    try { Files.delete(f); }
                    catch (IOException e) { log.warn("Failed to delete old cache file {}", f); }
                });

            log.debug("Cleaned {} old files from L2 cache", toDelete);
        } catch (IOException e) {
            log.warn("Failed to enforce L2 disk capacity: {}", e.getMessage());
        }
    }

    public void invalidateAll() {
        l1Cache.invalidateAll();
        try {
            Files.list(l2DiskPath)
                .filter(p -> p.toString().endsWith(AUDIO_EXTENSION))
                .forEach(f -> {
                    try { Files.delete(f); }
                    catch (IOException e) { log.warn("Failed to delete cache file {}", f); }
                });
            log.info("Invalidated all avatar audio cache entries");
        } catch (IOException e) {
            log.warn("Failed to invalidate L2 cache: {}", e.getMessage());
        }
    }
}