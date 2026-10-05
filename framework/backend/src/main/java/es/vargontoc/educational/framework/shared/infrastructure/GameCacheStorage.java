package es.vargontoc.educational.framework.shared.infrastructure;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class GameCacheStorage {

    private static final Logger log = LoggerFactory.getLogger(GameCacheStorage.class);

    private final Cache<Long, CachedRecognitionElement> recognitionElementCache;
    private final Cache<Long, CachedAccessibleColor> accessibleColorCache;
    private final Cache<AccessibleColorPaletteKey, CachedAccessibleColorPalette> accessibleColorPaletteCache;
    private final Cache<String, List<?>> worldCatalogCache;
    private final Cache<String, List<?>> avatarEventCache;
    private final Cache<Long, Object> childProfileCache;

    private Counter hitCounter;
    private Counter missCounter;

    public GameCacheStorage(
            Cache<Long, CachedRecognitionElement> recognitionElementCache,
            Cache<Long, CachedAccessibleColor> accessibleColorCache,
            Cache<AccessibleColorPaletteKey, CachedAccessibleColorPalette> accessibleColorPaletteCache,
            Cache<String, List<?>> worldCatalogCache,
            Cache<String, List<?>> avatarEventCache,
            Cache<Long, Object> childProfileCache) {
        this.recognitionElementCache = recognitionElementCache;
        this.accessibleColorCache = accessibleColorCache;
        this.accessibleColorPaletteCache = accessibleColorPaletteCache;
        this.worldCatalogCache = worldCatalogCache;
        this.avatarEventCache = avatarEventCache;
        this.childProfileCache = childProfileCache;
    }

    public void registerMetrics(MeterRegistry registry) {
        this.hitCounter = Counter.builder("game.cache.hits")
            .description("Game cache hits")
            .register(registry);
        this.missCounter = Counter.builder("game.cache.misses")
            .description("Game cache misses")
            .register(registry);
    }

    public Optional<CachedRecognitionElement> getRecognitionElement(Long id) {
        CachedRecognitionElement value = recognitionElementCache.getIfPresent(id);
        recordResult(value != null);
        return Optional.ofNullable(value);
    }

    public void putRecognitionElement(Long id, CachedRecognitionElement element) {
        recognitionElementCache.put(id, element);
    }

    public void invalidateRecognitionElement(Long id) {
        recognitionElementCache.invalidate(id);
    }

    public void invalidateAllRecognitionElements() {
        recognitionElementCache.invalidateAll();
    }

    public Optional<CachedAccessibleColor> getAccessibleColor(Long id) {
        CachedAccessibleColor value = accessibleColorCache.getIfPresent(id);
        recordResult(value != null);
        return Optional.ofNullable(value);
    }

    public void putAccessibleColor(Long id, CachedAccessibleColor color) {
        accessibleColorCache.put(id, color);
    }

    public void invalidateAllAccessibleColors() {
        accessibleColorCache.invalidateAll();
    }

    public Optional<CachedAccessibleColorPalette> getAccessibleColorPalette(Long accessibleColorId, String colorVisionMode) {
        AccessibleColorPaletteKey key = new AccessibleColorPaletteKey(accessibleColorId, colorVisionMode);
        CachedAccessibleColorPalette value = accessibleColorPaletteCache.getIfPresent(key);
        recordResult(value != null);
        return Optional.ofNullable(value);
    }

    public void putAccessibleColorPalette(Long accessibleColorId, String colorVisionMode, CachedAccessibleColorPalette palette) {
        accessibleColorPaletteCache.put(new AccessibleColorPaletteKey(accessibleColorId, colorVisionMode), palette);
    }

    public void invalidateAllAccessibleColorPalettes() {
        accessibleColorPaletteCache.invalidateAll();
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<List<T>> getWorldCatalog(String key) {
        List<?> value = worldCatalogCache.getIfPresent(key);
        recordResult(value != null);
        return Optional.ofNullable((List<T>) value);
    }

    @SuppressWarnings("unchecked")
    public <T> void putWorldCatalog(String key, List<T> value) {
        worldCatalogCache.put(key, (List<?>) value);
    }

    public void invalidateAllWorldCatalog() {
        worldCatalogCache.invalidateAll();
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<List<T>> getAvatarEvent(String key) {
        List<?> value = avatarEventCache.getIfPresent(key);
        recordResult(value != null);
        return Optional.ofNullable((List<T>) value);
    }

    @SuppressWarnings("unchecked")
    public <T> void putAvatarEvent(String key, List<T> value) {
        avatarEventCache.put(key, (List<?>) value);
    }

    public void invalidateAllAvatarEvents() {
        avatarEventCache.invalidateAll();
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getChildProfile(Long id) {
        Object value = childProfileCache.getIfPresent(id);
        recordResult(value != null);
        return Optional.ofNullable((T) value);
    }

    public void putChildProfile(Long id, Object profile) {
        childProfileCache.put(id, profile);
    }

    public void invalidateChildProfile(Long id) {
        childProfileCache.invalidate(id);
    }

    public void invalidateAll() {
        recognitionElementCache.invalidateAll();
        accessibleColorCache.invalidateAll();
        accessibleColorPaletteCache.invalidateAll();
        worldCatalogCache.invalidateAll();
        avatarEventCache.invalidateAll();
        childProfileCache.invalidateAll();
        log.info("All game caches invalidated");
    }

    public CacheStatsSnapshot stats() {
        return new CacheStatsSnapshot(
            aggregateStats(recognitionElementCache.stats(), accessibleColorCache.stats(),
                accessibleColorPaletteCache.stats(), worldCatalogCache.stats(),
                avatarEventCache.stats(), childProfileCache.stats())
        );
    }

    private CacheStats aggregateStats(CacheStats... statsArray) {
        long hits = 0, misses = 0;
        for (CacheStats s : statsArray) {
            hits += s.hitCount();
            misses += s.missCount();
        }
        return CacheStats.of(hits, misses, 0L, 0L, 0L, 0L, 0L);
    }

    private void recordResult(boolean hit) {
        if (hitCounter != null && missCounter != null) {
            if (hit) hitCounter.increment();
            else missCounter.increment();
        }
    }

    public record AccessibleColorPaletteKey(Long accessibleColorId, String colorVisionMode) {}

    public record CachedRecognitionElement(
        Long id, Long topicId, String code, String displayValue,
        String resourceRefs, Integer sortOrder, String similarityGroup,
        Long accessibleColorId, tools.jackson.databind.JsonNode parsedResourceRefs
    ) {}

    public record CachedAccessibleColor(
        Long id, String conceptualIdentity, String labelKey,
        String shapeIcon, String symbol
    ) {}

    public record CachedAccessibleColorPalette(
        Long id, Long accessibleColorId, String colorVisionMode,
        String accessibleColorValue, String accessibleLabelKey
    ) {}

    public record CacheStatsSnapshot(CacheStats stats) {}
}
