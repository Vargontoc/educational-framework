package es.vargontoc.educational.framework.shared.infrastructure;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameCacheStorageTest {

    private GameCacheStorage storage;

    @BeforeEach
    void setUp() {
        storage = new GameCacheStorage(
            Caffeine.newBuilder().maximumSize(10).recordStats().build(),
            Caffeine.newBuilder().maximumSize(10).recordStats().build(),
            Caffeine.newBuilder().maximumSize(10).recordStats().build(),
            Caffeine.newBuilder().maximumSize(10).recordStats().build(),
            Caffeine.newBuilder().maximumSize(10).recordStats().build(),
            Caffeine.newBuilder().maximumSize(10).recordStats().build()
        );
    }

    @Test
    void recognitionElement_cacheHitDoesNotRequireDb() {
        var element = new GameCacheStorage.CachedRecognitionElement(
            1L, 10L, "code1", "display1", "{}", 1, null, null, null);
        storage.putRecognitionElement(1L, element);

        Optional<GameCacheStorage.CachedRecognitionElement> result = storage.getRecognitionElement(1L);
        assertTrue(result.isPresent());
        assertEquals("code1", result.get().code());
    }

    @Test
    void recognitionElement_cacheMissReturnsEmpty() {
        Optional<GameCacheStorage.CachedRecognitionElement> result = storage.getRecognitionElement(999L);
        assertTrue(result.isEmpty());
    }

    @Test
    void childProfile_invalidateRemovesFromCache() {
        storage.putChildProfile(1L, "profile-data");
        assertTrue(storage.getChildProfile(1L).isPresent());

        storage.invalidateChildProfile(1L);
        assertTrue(storage.getChildProfile(1L).isEmpty());
    }

    @Test
    void worldCatalog_cacheStoresAndRetrieves() {
        List<String> data = List.of("host1", "host2");
        storage.putWorldCatalog("hosts:age=3", data);

        Optional<List<String>> result = storage.getWorldCatalog("hosts:age=3");
        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
    }

    @Test
    void worldCatalog_invalidateAllClearsCache() {
        storage.putWorldCatalog("hosts:age=3", List.of("host1"));
        storage.putWorldCatalog("situations:age=3", List.of("sit1"));

        storage.invalidateAllWorldCatalog();

        assertTrue(storage.getWorldCatalog("hosts:age=3").isEmpty());
        assertTrue(storage.getWorldCatalog("situations:age=3").isEmpty());
    }

    @Test
    void accessibleColorPalette_cacheKeyIncludesColorVisionMode() {
        var palette1 = new GameCacheStorage.CachedAccessibleColorPalette(1L, 10L, "NONE", "#FF0000", "red");
        var palette2 = new GameCacheStorage.CachedAccessibleColorPalette(2L, 10L, "DEUTERANOPIA", "#00FF00", "green");

        storage.putAccessibleColorPalette(10L, "NONE", palette1);
        storage.putAccessibleColorPalette(10L, "DEUTERANOPIA", palette2);

        assertEquals("#FF0000", storage.getAccessibleColorPalette(10L, "NONE").get().accessibleColorValue());
        assertEquals("#00FF00", storage.getAccessibleColorPalette(10L, "DEUTERANOPIA").get().accessibleColorValue());
    }

    @Test
    void invalidateAll_clearsEveryCache() {
        storage.putRecognitionElement(1L, new GameCacheStorage.CachedRecognitionElement(1L, 1L, "c", "d", "{}", 1, null, null, null));
        storage.putChildProfile(1L, "profile");
        storage.putWorldCatalog("key", List.of("val"));
        storage.putAvatarEvent("key", List.of("val"));

        storage.invalidateAll();

        assertTrue(storage.getRecognitionElement(1L).isEmpty());
        assertTrue(storage.getChildProfile(1L).isEmpty());
        assertTrue(storage.getWorldCatalog("key").isEmpty());
        assertTrue(storage.getAvatarEvent("key").isEmpty());
    }
}
