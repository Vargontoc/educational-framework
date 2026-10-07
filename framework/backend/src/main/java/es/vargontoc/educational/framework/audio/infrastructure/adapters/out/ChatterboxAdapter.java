package es.vargontoc.educational.framework.audio.infrastructure.adapters.out;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import es.vargontoc.educational.framework.audio.application.ports.out.AudioPort;
import es.vargontoc.educational.framework.audio.domain.AudioRequest;
import es.vargontoc.educational.framework.shared.exception.AppException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ChatterboxAdapter implements AudioPort {

    private final RestClient client;
    private final long pingCacheTtlMs;
    private final AtomicLong lastPingTimestamp = new AtomicLong(0);
    private final AtomicBoolean lastPingResult = new AtomicBoolean(false);
    /**
     * Limita las sintesis simultaneas contra Chatterbox. El modelo no admite peticiones concurrentes: varias a la
     * vez fallan con errores de PyTorch ("stack expects each tensor to be equal size", "Kernel size can't be
     * greater than actual input size"). Las sintesis extra esperan turno en vez de fallar.
     */
    private final Semaphore synthesisPermits;

    @Autowired
    public ChatterboxAdapter(
            @Qualifier("audio-client") RestClient client,
            @Value("${app.audio.ping-cache-ttl-seconds:5}") long pingCacheTtlSeconds,
            @Value("${app.audio.tts-max-concurrent-requests:1}") int maxConcurrentRequests) {
        this.client = client;
        this.pingCacheTtlMs = pingCacheTtlSeconds * 1000;
        this.synthesisPermits = new Semaphore(Math.max(1, maxConcurrentRequests), true);
    }

    public ChatterboxAdapter(RestClient client, long pingCacheTtlSeconds) {
        this(client, pingCacheTtlSeconds, 1);
    }

    @Override
    public boolean isAvailable() {
        long now = System.currentTimeMillis();
        long lastCheck = lastPingTimestamp.get();
        if (now - lastCheck < pingCacheTtlMs) {
            return lastPingResult.get();
        }
        boolean available = doPing();
        lastPingResult.set(available);
        lastPingTimestamp.set(now);
        return available;
    }

    private boolean doPing() {
        try {
            client.get().uri("/ping").retrieve().toBodilessEntity();
            return true;
        } catch (Exception e) {
            log.warn("TTS /ping failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public byte[] synthesizeAudio(AudioRequest request) {
        if (!isAvailable()) {
            throw new AppException("Servicio audio no disponible", HttpStatus.SERVICE_UNAVAILABLE);
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("input", request.text());
        body.put("voice", "nubi-npc-voice");
        body.put("response_format", "wav");
        body.put("exaggeration", request.exageration());
        body.put("cfg_weight", request.cfg());
        body.put("temperature", request.temperature());

        try {
            synthesisPermits.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AppException("Sintesis de audio interrumpida", HttpStatus.SERVICE_UNAVAILABLE);
        }
        try {
            return client.post().uri("/audio/speech")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(byte[].class);
        } finally {
            synthesisPermits.release();
        }
    }
}
