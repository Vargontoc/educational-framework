package es.vargontoc.educational.framework.audio.infrastructure.adapters.out;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import com.sun.net.httpserver.HttpServer;

import es.vargontoc.educational.framework.audio.domain.AudioRequest;
import es.vargontoc.educational.framework.audio.domain.enums.TonePreset;
import es.vargontoc.educational.framework.audio.infrastructure.config.AudioConfiguration;

/**
 * Regresion: Chatterbox no admite sintesis simultaneas (errores de PyTorch por tensores de distinto tamano).
 * El adaptador debe serializarlas: el pre-calentamiento lanzaba 5 a la vez y fallaban todas.
 */
class ChatterboxAdapterConcurrencyTest {

    private HttpServer server;
    private final AtomicInteger inFlight = new AtomicInteger();
    private final AtomicInteger maxInFlight = new AtomicInteger();
    private final AtomicInteger served = new AtomicInteger();

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.setExecutor(Executors.newCachedThreadPool());
        server.createContext("/ping", exchange -> {
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        server.createContext("/audio/speech", exchange -> {
            exchange.getRequestBody().readAllBytes();
            int now = inFlight.incrementAndGet();
            maxInFlight.accumulateAndGet(now, Math::max);
            try {
                Thread.sleep(120);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            inFlight.decrementAndGet();
            served.incrementAndGet();
            byte[] wav = "RIFF".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, wav.length);
            exchange.getResponseBody().write(wav);
            exchange.close();
        });
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    private int runFiveSimultaneousSyntheses(int maxConcurrent) throws Exception {
        RestClient client = new AudioConfiguration()
            .getAudioClient("http://127.0.0.1:" + server.getAddress().getPort(), 3, 10);
        ChatterboxAdapter adapter = new ChatterboxAdapter(client, 60, maxConcurrent);

        ExecutorService pool = Executors.newFixedThreadPool(5);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<byte[]>> results = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            String text = "Texto " + i;
            results.add(pool.submit(() -> {
                start.await();
                return adapter.synthesizeAudio(AudioRequest.withPreset(text, TonePreset.CALM));
            }));
        }
        start.countDown();
        for (Future<byte[]> result : results) {
            assertTrue(result.get(20, TimeUnit.SECONDS).length > 0);
        }
        pool.shutdownNow();
        return maxInFlight.get();
    }

    @Test
    void synthesize_withDefaultLimit_neverRunsTwoRequestsAtOnce() throws Exception {
        assertEquals(1, runFiveSimultaneousSyntheses(1));
        assertEquals(5, served.get(), "las sintesis en espera no se pierden: todas terminan");
    }

    @Test
    void synthesize_withHigherLimit_allowsThatManyAtOnce() throws Exception {
        int max = runFiveSimultaneousSyntheses(2);
        assertTrue(max >= 2 && max <= 2, "esperaba 2 simultaneas, hubo " + max);
    }
}
