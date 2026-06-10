package es.vargontoc.educational.framework.avatar.infrastructure.cache;

import es.vargontoc.educational.framework.avatar.infrastructure.tts.TtsException;
import es.vargontoc.educational.framework.avatar.infrastructure.tts.TtsToneMapper;
import es.vargontoc.educational.framework.avatar.ports.out.TtsClient;
import es.vargontoc.educational.framework.content.model.AvatarTone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CachingTtsClientTest {

    @TempDir
    Path tempDir;

    private TtsClient delegate;
    private AvatarAudioCache cache;
    private TtsToneMapper toneMapper;
    private AvatarAudioCacheProperties properties;
    private CachingTtsClient cachingClient;

    @BeforeEach
    void setUp() {
        delegate = mock(TtsClient.class);

        properties = new AvatarAudioCacheProperties();
        properties.setEnabled(true);
        properties.setMaxEntries(100);
        properties.setExpireAfterWriteMinutes(60);
        properties.setDiskPath(tempDir.resolve("avatar-audio").toString());
        properties.setMaxDiskEntries(10);
        properties.setAudioFormatVersion(1);

        cache = new AvatarAudioCache(properties);
        toneMapper = new TtsToneMapper();
        cachingClient = new CachingTtsClient(delegate, cache, toneMapper, properties);
    }

    @Test
    void synthesize_cacheMiss_invokesTtsAndStoresResult() {
        byte[] expectedAudio = "mp3-data".getBytes();
        when(delegate.synthesize(anyString(), anyString(), any(), anyString()))
            .thenReturn(expectedAudio);

        byte[] result = cachingClient.synthesize("Hello", "es", AvatarTone.CALM, "npc");

        assertArrayEquals(expectedAudio, result);
        verify(delegate, times(1)).synthesize("Hello", "es", AvatarTone.CALM, "npc");
    }

    @Test
    void synthesize_cacheHit_avoidsTtsCall() {
        byte[] expectedAudio = "cached-audio".getBytes();
        when(delegate.synthesize(anyString(), anyString(), any(), anyString()))
            .thenReturn(expectedAudio);

        cachingClient.synthesize("Hello", "es", AvatarTone.CALM, "npc");
        cachingClient.synthesize("Hello", "es", AvatarTone.CALM, "npc");

        verify(delegate, times(1)).synthesize(anyString(), anyString(), any(), anyString());
    }

    @Test
    void synthesize_ttsFailure_propagatesException() {
        when(delegate.synthesize(anyString(), anyString(), any(), anyString()))
            .thenThrow(new TtsException("TTS timeout", "CONNECTION_ERROR", true));

        assertThrows(TtsException.class, () ->
            cachingClient.synthesize("Hello", "es", AvatarTone.CALM, "npc"));
    }

    @Test
    void synthesize_differentText_doesNotHitCache() {
        byte[] audio1 = "audio1".getBytes();
        byte[] audio2 = "audio2".getBytes();
        when(delegate.synthesize(anyString(), anyString(), any(), anyString()))
            .thenReturn(audio1)
            .thenReturn(audio2);

        byte[] result1 = cachingClient.synthesize("Hello", "es", AvatarTone.CALM, "npc");
        byte[] result2 = cachingClient.synthesize("World", "es", AvatarTone.CALM, "npc");

        assertArrayEquals(audio1, result1);
        assertArrayEquals(audio2, result2);
        verify(delegate, times(2)).synthesize(anyString(), anyString(), any(), anyString());
    }

    @Test
    void synthesize_differentLocale_doesNotHitCache() {
        byte[] audio1 = "audio1".getBytes();
        byte[] audio2 = "audio2".getBytes();
        when(delegate.synthesize(anyString(), anyString(), any(), anyString()))
            .thenReturn(audio1)
            .thenReturn(audio2);

        byte[] result1 = cachingClient.synthesize("Hello", "es", AvatarTone.CALM, "npc");
        byte[] result2 = cachingClient.synthesize("Hello", "en", AvatarTone.CALM, "npc");

        assertArrayEquals(audio1, result1);
        assertArrayEquals(audio2, result2);
    }

    @Test
    void synthesize_differentVoiceProfile_doesNotHitCache() {
        byte[] audio1 = "audio1".getBytes();
        byte[] audio2 = "audio2".getBytes();
        when(delegate.synthesize(anyString(), anyString(), any(), anyString()))
            .thenReturn(audio1)
            .thenReturn(audio2);

        byte[] result1 = cachingClient.synthesize("Hello", "es", AvatarTone.CALM, "npc");
        byte[] result2 = cachingClient.synthesize("Hello", "es", AvatarTone.CALM, "storyteller");

        assertArrayEquals(audio1, result1);
        assertArrayEquals(audio2, result2);
    }

    @Test
    void synthesize_toneMapping_neutralMapsToCalm() {
        byte[] expectedAudio = "audio".getBytes();
        when(delegate.synthesize(anyString(), anyString(), any(), anyString()))
            .thenReturn(expectedAudio);

        cachingClient.synthesize("Hello", "es", AvatarTone.NEUTRAL, "npc");

        verify(delegate).synthesize("Hello", "es", AvatarTone.NEUTRAL, "npc");
    }
}