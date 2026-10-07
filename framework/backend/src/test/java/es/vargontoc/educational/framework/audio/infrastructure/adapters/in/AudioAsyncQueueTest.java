package es.vargontoc.educational.framework.audio.infrastructure.adapters.in;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.vargontoc.educational.framework.audio.application.ports.out.AudioPort;
import es.vargontoc.educational.framework.audio.domain.AudioCache;
import es.vargontoc.educational.framework.audio.domain.AudioRequest;
import es.vargontoc.educational.framework.audio.domain.ToneParams;
import es.vargontoc.educational.framework.audio.infrastructure.cache.AudioCacheStorage;

/**
 * La cola de sintesis protege a Chatterbox: una sola sintesis a la vez, sin duplicados, lo que pide un niño
 * antes que el calentamiento, con reintentos y en pausa mientras el TTS no responde.
 */
class AudioAsyncQueueTest {

    private static final byte[] WAV = { 1, 2, 3 };

    private AudioAsync queue;
    private AudioPort port;
    private AudioCacheStorage storage;
    private final Map<AudioCache, byte[]> stored = new ConcurrentHashMap<>();

    @BeforeEach
    void setUp() {
        queue = new AudioAsync(3, 1, 20);
        queue.start();
        port = mock(AudioPort.class);
        when(port.isAvailable()).thenReturn(true);
        storage = mock(AudioCacheStorage.class);
        when(storage.get(any())).thenAnswer(invocation -> stored.get(invocation.<AudioCache>getArgument(0)));
        doAnswer(invocation -> {
            stored.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(storage).put(any(), any());
    }

    @AfterEach
    void tearDown() {
        queue.stop();
    }

    private static AudioRequest request(String text) {
        return new AudioRequest(text, 0.5, 0.5, 0.5);
    }

    private static AudioCache key(AudioRequest request) {
        return AudioCache.of(request.text(), new ToneParams(request.exageration(), request.cfg(), request.temperature()));
    }

    private void onDemand(String text) {
        AudioRequest request = request(text);
        queue.generateAudio(key(request), request, port, storage);
    }

    private void warm(String text) {
        AudioRequest request = request(text);
        queue.warmAudio(key(request), request, port, storage);
    }

    private void awaitIdle() throws InterruptedException {
        long deadline = System.currentTimeMillis() + 10_000;
        while (!queue.isIdle() && System.currentTimeMillis() < deadline) {
            Thread.sleep(10);
        }
        assertTrue(queue.isIdle(), "la cola deberia haberse vaciado");
    }

    @Test
    void synthesizesOneAtATimeAndStoresEverything() throws Exception {
        AtomicInteger inFlight = new AtomicInteger();
        AtomicInteger maxInFlight = new AtomicInteger();
        when(port.synthesizeAudio(any())).thenAnswer(invocation -> {
            maxInFlight.accumulateAndGet(inFlight.incrementAndGet(), Math::max);
            Thread.sleep(30);
            inFlight.decrementAndGet();
            return WAV;
        });

        for (int i = 0; i < 6; i++) {
            warm("texto " + i);
        }
        awaitIdle();

        assertEquals(1, maxInFlight.get(), "nunca dos sintesis simultaneas");
        assertEquals(6, stored.size());
    }

    @Test
    void duplicateRequestsAreSynthesizedOnce() throws Exception {
        CountDownLatch firstStarted = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        List<String> synthesized = new CopyOnWriteArrayList<>();
        when(port.synthesizeAudio(any())).thenAnswer(invocation -> {
            AudioRequest request = invocation.getArgument(0);
            synthesized.add(request.text());
            if (request.text().equals("bloqueante")) {
                firstStarted.countDown();
                release.await(5, TimeUnit.SECONDS);
            }
            return WAV;
        });

        onDemand("bloqueante");
        assertTrue(firstStarted.await(5, TimeUnit.SECONDS));
        for (int i = 0; i < 3; i++) {
            warm("repetido");
            onDemand("repetido");
        }
        release.countDown();
        awaitIdle();

        assertEquals(1, synthesized.stream().filter("repetido"::equals).count());
    }

    @Test
    void onDemandRequestsJumpAheadOfWarmup() throws Exception {
        CountDownLatch firstStarted = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        List<String> order = new CopyOnWriteArrayList<>();
        when(port.synthesizeAudio(any())).thenAnswer(invocation -> {
            AudioRequest request = invocation.getArgument(0);
            order.add(request.text());
            if (request.text().equals("primero")) {
                firstStarted.countDown();
                release.await(5, TimeUnit.SECONDS);
            }
            return WAV;
        });

        warm("primero");
        assertTrue(firstStarted.await(5, TimeUnit.SECONDS));
        warm("calentamiento A");
        warm("calentamiento B");
        onDemand("un niño lo espera");
        release.countDown();
        awaitIdle();

        assertEquals(List.of("primero", "un niño lo espera", "calentamiento A", "calentamiento B"), order);
    }

    @Test
    void retriesAfterFailureAndStoresOnSuccess() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        when(port.synthesizeAudio(any())).thenAnswer(invocation -> {
            if (calls.incrementAndGet() < 3) {
                throw new RuntimeException("500 del TTS");
            }
            return WAV;
        });

        onDemand("reintento");
        awaitIdle();

        assertEquals(3, calls.get());
        assertArrayEquals(WAV, stored.get(key(request("reintento"))));
    }

    @Test
    void givesUpAfterMaxAttemptsButKeepsProcessingTheRest() throws Exception {
        AtomicInteger badCalls = new AtomicInteger();
        when(port.synthesizeAudio(any())).thenAnswer(invocation -> {
            AudioRequest request = invocation.getArgument(0);
            if (request.text().equals("roto")) {
                badCalls.incrementAndGet();
                throw new RuntimeException("siempre falla");
            }
            return WAV;
        });

        warm("roto");
        warm("bueno");
        awaitIdle();

        assertEquals(3, badCalls.get(), "max-attempts = 3");
        assertFalse(stored.containsKey(key(request("roto"))));
        assertTrue(stored.containsKey(key(request("bueno"))));
    }

    @Test
    void skipsTextsAlreadyInCache() throws Exception {
        stored.put(key(request("ya generado")), WAV);

        warm("ya generado");
        awaitIdle();

        verify(port, never()).synthesizeAudio(any());
    }

    @Test
    void pausesWhileTtsIsUnavailableAndResumesWithoutLosingTexts() throws Exception {
        AtomicInteger availabilityChecks = new AtomicInteger();
        when(port.isAvailable()).thenAnswer(invocation -> availabilityChecks.incrementAndGet() > 3);
        when(port.synthesizeAudio(any())).thenReturn(WAV);

        warm("uno");
        warm("dos");
        awaitIdle();

        assertTrue(availabilityChecks.get() > 3, "espero hasta que el TTS volvio");
        assertEquals(2, stored.size());
    }
}
