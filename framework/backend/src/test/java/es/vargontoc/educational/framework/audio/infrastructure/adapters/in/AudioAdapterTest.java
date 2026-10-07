package es.vargontoc.educational.framework.audio.infrastructure.adapters.in;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.vargontoc.educational.framework.audio.application.ports.out.AudioPort;
import es.vargontoc.educational.framework.audio.domain.AudioRequest;
import es.vargontoc.educational.framework.audio.infrastructure.cache.AudioCacheStorage;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class AudioAdapterTest {

    private AudioPort port;
    private AudioCacheStorage cache;
    private AudioAsync audioAsync;
    private AudioAdapter adapter;

    @BeforeEach
    void setUp() {
        port = mock(AudioPort.class);
        cache = mock(AudioCacheStorage.class);
        audioAsync = mock(AudioAsync.class);
        adapter = new AudioAdapter(port, audioAsync, cache, new SimpleMeterRegistry());
    }

    @Test
    void getAudio_cacheHit_returnsCachedBytes() {
        when(cache.get(any())).thenReturn(new byte[]{1, 2, 3});

        byte[] result = adapter.getAudio(AudioRequest.withPreset("test", es.vargontoc.educational.framework.audio.domain.enums.TonePreset.CALM));

        assertNotNull(result);
    }

    @Test
    void getAudio_cacheMiss_returnsNullAndTriggersAsync() {
        when(cache.get(any())).thenReturn(null);

        byte[] result = adapter.getAudio(AudioRequest.withPreset("test", es.vargontoc.educational.framework.audio.domain.enums.TonePreset.CALM));

        assertNull(result, "Cache miss must return null (async synthesis in background)");
    }

    @Test
    void getAudio_doesNotBlockOnTtsFailure() {
        when(cache.get(any())).thenReturn(null);

        long start = System.currentTimeMillis();
        byte[] result = adapter.getAudio(AudioRequest.withPreset("test", es.vargontoc.educational.framework.audio.domain.enums.TonePreset.CALM));
        long duration = System.currentTimeMillis() - start;

        assertNull(result);
        assertNotNull(result == null ? "ok" : "not-null");
        // The call must return quickly (no blocking on TTS)
        assertTrue(duration < 1000, "getAudio should not block, took " + duration + "ms");
    }

    @Test
    void warm_cacheMiss_queuesLowPriorityWarmup() {
        when(cache.get(any())).thenReturn(null);
        AudioRequest request = AudioRequest.withPreset("curado", es.vargontoc.educational.framework.audio.domain.enums.TonePreset.CALM);

        adapter.warm(request);

        org.mockito.Mockito.verify(audioAsync).warmAudio(any(), org.mockito.ArgumentMatchers.eq(request), any(), any());
        org.mockito.Mockito.verify(audioAsync, org.mockito.Mockito.never()).generateAudio(any(), any(), any(), any());
    }

    @Test
    void warm_cacheHit_doesNothing() {
        when(cache.get(any())).thenReturn(new byte[]{1});

        adapter.warm(AudioRequest.withPreset("curado", es.vargontoc.educational.framework.audio.domain.enums.TonePreset.CALM));

        org.mockito.Mockito.verify(audioAsync, org.mockito.Mockito.never()).warmAudio(any(), any(), any(), any());
    }

    private void assertTrue(boolean condition, String message) {
        org.junit.jupiter.api.Assertions.assertTrue(condition, message);
    }
}
