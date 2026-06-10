package es.vargontoc.educational.framework.avatar.infrastructure.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AvatarAudioCacheTest {

    @TempDir
    Path tempDir;

    private AvatarAudioCache cache;
    private AvatarAudioCacheProperties properties;

    @BeforeEach
    void setUp() {
        properties = new AvatarAudioCacheProperties();
        properties.setEnabled(true);
        properties.setMaxEntries(100);
        properties.setExpireAfterWriteMinutes(60);
        properties.setDiskPath(tempDir.resolve("avatar-audio").toString());
        properties.setMaxDiskEntries(10);
        properties.setAudioFormatVersion(1);
        cache = new AvatarAudioCache(properties);
    }

    @Test
    void put_and_get_returnsStoredBytes() {
        AvatarCacheKey key = new AvatarCacheKey("es", "calm", "Hello".hashCode(), "npc", 1);
        byte[] audioData = "fake-mp3-data".getBytes();

        cache.put(key, audioData);
        byte[] result = cache.get(key);

        assertNotNull(result);
        assertArrayEquals(audioData, result);
    }

    @Test
    void get_unknownKey_returnsNull() {
        AvatarCacheKey key = new AvatarCacheKey("es", "calm", "Unknown".hashCode(), "npc", 1);
        byte[] result = cache.get(key);
        assertNull(result);
    }

    @Test
    void differentText_producesDifferentKey() {
        byte[] audioData = "mp3".getBytes();

        AvatarCacheKey key1 = new AvatarCacheKey("es", "calm", "Text1".hashCode(), "npc", 1);
        AvatarCacheKey key2 = new AvatarCacheKey("es", "calm", "Text2".hashCode(), "npc", 1);

        cache.put(key1, audioData);
        byte[] result = cache.get(key2);

        assertNull(result);
    }

    @Test
    void differentTone_producesDifferentKey() {
        byte[] audioData = "mp3".getBytes();

        AvatarCacheKey key1 = new AvatarCacheKey("es", "calm", "Text".hashCode(), "npc", 1);
        AvatarCacheKey key2 = new AvatarCacheKey("es", "joyful", "Text".hashCode(), "npc", 1);

        cache.put(key1, audioData);
        byte[] result = cache.get(key2);

        assertNull(result);
    }

    @Test
    void differentLocale_producesDifferentKey() {
        byte[] audioData = "mp3".getBytes();

        AvatarCacheKey key1 = new AvatarCacheKey("es", "calm", "Text".hashCode(), "npc", 1);
        AvatarCacheKey key2 = new AvatarCacheKey("en", "calm", "Text".hashCode(), "npc", 1);

        cache.put(key1, audioData);
        byte[] result = cache.get(key2);

        assertNull(result);
    }

    @Test
    void differentVoiceProfile_producesDifferentKey() {
        byte[] audioData = "mp3".getBytes();

        AvatarCacheKey key1 = new AvatarCacheKey("es", "calm", "Text".hashCode(), "npc", 1);
        AvatarCacheKey key2 = new AvatarCacheKey("es", "calm", "Text".hashCode(), "storyteller", 1);

        cache.put(key1, audioData);
        byte[] result = cache.get(key2);

        assertNull(result);
    }

    @Test
    void differentAudioFormatVersion_producesDifferentKey() {
        byte[] audioData = "mp3".getBytes();

        AvatarCacheKey key1 = new AvatarCacheKey("es", "calm", "Text".hashCode(), "npc", 1);
        AvatarCacheKey key2 = new AvatarCacheKey("es", "calm", "Text".hashCode(), "npc", 2);

        cache.put(key1, audioData);
        byte[] result = cache.get(key2);

        assertNull(result);
    }

    @Test
    void invalidateAll_clearsAllEntries() {
        AvatarCacheKey key1 = new AvatarCacheKey("es", "calm", "Text1".hashCode(), "npc", 1);
        AvatarCacheKey key2 = new AvatarCacheKey("es", "calm", "Text2".hashCode(), "npc", 1);
        byte[] audioData = "mp3".getBytes();

        cache.put(key1, audioData);
        cache.put(key2, audioData);

        cache.invalidateAll();

        assertNull(cache.get(key1));
        assertNull(cache.get(key2));
    }
}