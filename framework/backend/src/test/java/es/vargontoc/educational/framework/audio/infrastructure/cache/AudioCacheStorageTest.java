package es.vargontoc.educational.framework.audio.infrastructure.cache;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.vargontoc.educational.framework.audio.domain.AudioCache;
import es.vargontoc.educational.framework.audio.domain.ToneParams;
import es.vargontoc.educational.framework.audio.infrastructure.config.AudioCacheConfiguration;

class AudioCacheStorageTest {

    private Path tempDir;
    private AudioCacheStorage storage;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("audio-cache-test");
        AudioCacheConfiguration props = new AudioCacheConfiguration(64, 30, 5, tempDir.toString(), 300);
        storage = new AudioCacheStorage(tempDir, props);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (tempDir != null && Files.exists(tempDir)) {
            try (Stream<Path> walk = Files.walk(tempDir)) {
                walk.sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                    });
            }
        }
    }

    @Test
    void invalidCachePath_throwsExplicitError() throws IOException {
        Path fileAsPath = Files.createTempFile("not-a-dir", ".tmp");
        try {
            AudioCacheConfiguration props = new AudioCacheConfiguration(64, 30, 5, fileAsPath.toString(), 300);
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> new AudioCacheStorage(fileAsPath, props));
            assertNotNull(ex.getMessage());
        } finally {
            Files.deleteIfExists(fileAsPath);
        }
    }

    @Test
    void putAndGet_roundTrip() {
        ToneParams tone = new ToneParams(0.5, 0.5, 0.8);
        AudioCache key = AudioCache.of("test text", tone);
        byte[] data = {1, 2, 3, 4, 5};

        storage.put(key, data);
        byte[] result = storage.get(key);

        assertNotNull(result);
        assertEquals(5, result.length);
    }

    @Test
    void cacheMiss_returnsNull() {
        ToneParams tone = new ToneParams(0.5, 0.5, 0.8);
        AudioCache key = AudioCache.of("nonexistent", tone);

        assertNull(storage.get(key));
    }

    @Test
    void diskCacheSurvivesRestart() throws IOException {
        ToneParams tone = new ToneParams(0.5, 0.5, 0.8);
        AudioCache key = AudioCache.of("persistent text", tone);
        byte[] data = {10, 20, 30};

        storage.put(key, data);

        AudioCacheConfiguration props = new AudioCacheConfiguration(64, 30, 5, tempDir.toString(), 300);
        AudioCacheStorage newStorage = new AudioCacheStorage(tempDir, props);

        byte[] result = newStorage.get(key);
        assertNotNull(result);
        assertEquals(3, result.length);
    }

    @Test
    void enforceDiskCapacity_removesOldestFiles() throws IOException, InterruptedException {
        AudioCacheConfiguration props = new AudioCacheConfiguration(64, 30, 3, tempDir.toString(), 300);
        AudioCacheStorage smallStorage = new AudioCacheStorage(tempDir, props);

        for (int i = 0; i < 5; i++) {
            AudioCache key = AudioCache.of("text " + i, new ToneParams(0.5, 0.5, 0.8));
            smallStorage.put(key, new byte[]{(byte) i});
            Thread.sleep(50);
        }

        smallStorage.enforceDiskCapacity();

        try (Stream<Path> files = Files.list(tempDir).filter(p -> p.toString().endsWith(".wav"))) {
            long count = files.count();
            assertTrue(count <= 3, "Expected at most 3 files after enforcement, found " + count);
        }
    }

    @Test
    void cacheFileExtension_isWav() {
        ToneParams tone = new ToneParams(0.5, 0.5, 0.8);
        AudioCache key = AudioCache.of("extension test", tone);
        storage.put(key, new byte[]{1});

        try (Stream<Path> files = Files.list(tempDir)) {
            var wavFiles = files.filter(p -> p.toString().endsWith(".wav")).toList();
            assertEquals(1, wavFiles.size());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void differentTextsSameHashCode_doNotShareAudio() {
        String text1 = "Aa";
        String text2 = "BB";
        org.junit.jupiter.api.Assertions.assertEquals(text1.hashCode(), text2.hashCode());

        ToneParams tone = new ToneParams(0.5, 0.5, 0.8);
        AudioCache key1 = AudioCache.of(text1, tone);
        AudioCache key2 = AudioCache.of(text2, tone);

        byte[] data1 = {1, 1, 1};
        byte[] data2 = {2, 2, 2};

        storage.put(key1, data1);
        storage.put(key2, data2);

        byte[] result1 = storage.get(key1);
        byte[] result2 = storage.get(key2);

        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(1, result1[0]);
        assertEquals(2, result2[0]);
    }
}
