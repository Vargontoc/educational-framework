package es.vargontoc.educational.framework.audio.infrastructure.adapters.in;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import es.vargontoc.educational.framework.audio.application.ports.out.AudioPort;
import es.vargontoc.educational.framework.audio.domain.AudioCache;
import es.vargontoc.educational.framework.audio.domain.AudioRequest;
import es.vargontoc.educational.framework.audio.infrastructure.cache.AudioCacheStorage;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

/**
 * Cola unica de sintesis de audio contra el TTS.
 *
 * Chatterbox solo admite una sintesis a la vez: varias simultaneas (o una cancelada por timeout mientras el
 * servidor sigue generandola) corrompen su estado y todas acaban fallando con errores de PyTorch. Por eso toda
 * peticion pasa por esta cola, que la procesa con un unico hilo:
 * - Deduplica por clave de cache: un mismo texto pedido varias veces se sintetiza una sola vez.
 * - Prioridad: lo que se pide en ese momento (un niño esperando) pasa por delante del calentamiento de arranque.
 * - Reintenta los fallos y se queda en pausa mientras el TTS no esta disponible, en vez de fallar texto a texto.
 */
@Slf4j
@Component
public class AudioAsync {

    /** Menor valor = antes. */
    static final int PRIORITY_ON_DEMAND = 0;
    static final int PRIORITY_WARMUP = 1;

    private record Job(AudioCache key, AudioRequest request, AudioPort port, AudioCacheStorage storage,
                       int priority, long sequence) implements Comparable<Job> {
        @Override
        public int compareTo(Job other) {
            int byPriority = Integer.compare(priority, other.priority);
            return byPriority != 0 ? byPriority : Long.compare(sequence, other.sequence);
        }
    }

    private final int maxAttempts;
    private final long retryDelayMs;
    private final long unavailableRetryDelayMs;

    private final PriorityBlockingQueue<Job> queue = new PriorityBlockingQueue<>();
    /** Claves en cola (o en proceso) con su mejor prioridad: evita encolar el mismo texto dos veces. */
    private final ConcurrentHashMap<AudioCache, Integer> pending = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();
    private final AtomicInteger processing = new AtomicInteger();
    private volatile Thread worker;

    public AudioAsync(
            @Value("${app.audio.synthesis.max-attempts:3}") int maxAttempts,
            @Value("${app.audio.synthesis.retry-delay-ms:2000}") long retryDelayMs,
            @Value("${app.audio.synthesis.unavailable-retry-delay-ms:30000}") long unavailableRetryDelayMs) {
        this.maxAttempts = Math.max(1, maxAttempts);
        this.retryDelayMs = Math.max(0, retryDelayMs);
        this.unavailableRetryDelayMs = Math.max(1, unavailableRetryDelayMs);
    }

    @PostConstruct
    public synchronized void start() {
        if (worker != null) return;
        Thread thread = new Thread(this::runLoop, "tts-synthesis");
        thread.setDaemon(true);
        worker = thread;
        thread.start();
    }

    @PreDestroy
    public synchronized void stop() {
        if (worker != null) {
            worker.interrupt();
            worker = null;
        }
    }

    /** Peticion con prioridad normal: alguien la necesita ahora (se adelanta al calentamiento). */
    public void generateAudio(AudioCache key, AudioRequest request, AudioPort audioPort, AudioCacheStorage storage) {
        enqueue(key, request, audioPort, storage, PRIORITY_ON_DEMAND);
    }

    /** Calentamiento de cache (arranque, contenido curado): va detras de las peticiones a demanda. */
    public void warmAudio(AudioCache key, AudioRequest request, AudioPort audioPort, AudioCacheStorage storage) {
        enqueue(key, request, audioPort, storage, PRIORITY_WARMUP);
    }

    /** Textos pendientes de sintetizar (en cola o en proceso). */
    public int pendingCount() {
        return pending.size();
    }

    boolean isIdle() {
        return queue.isEmpty() && processing.get() == 0;
    }

    private void enqueue(AudioCache key, AudioRequest request, AudioPort port, AudioCacheStorage storage, int priority) {
        boolean[] add = { false };
        pending.compute(key, (k, current) -> {
            if (current == null || priority < current) {
                add[0] = true;
                return priority;
            }
            return current; // ya esta en cola con igual o mejor prioridad
        });
        if (add[0]) {
            queue.add(new Job(key, request, port, storage, priority, sequence.incrementAndGet()));
        }
    }

    private void runLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            Job job;
            try {
                job = queue.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            processing.incrementAndGet();
            try {
                process(job);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                log.error("Unexpected error synthesizing audio for text='{}': {}", job.request().text(), e.getMessage(), e);
            } finally {
                pending.remove(job.key());
                processing.decrementAndGet();
            }
        }
    }

    private void process(Job job) throws InterruptedException {
        if (job.storage().get(job.key()) != null) {
            return; // ya generado por otra peticion
        }
        awaitTtsAvailable(job.port());

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                log.info("Calling TTS service: text={} (attempt {}/{}, {} pending)",
                    job.request().text(), attempt, maxAttempts, pending.size());
                byte[] audioData = job.port().synthesizeAudio(job.request());
                if (audioData != null && audioData.length > 0) {
                    job.storage().put(job.key(), audioData);
                    log.info("Stored audio in cache: key={}", job.key());
                    return;
                }
                log.warn("TTS returned empty audio for text='{}'", job.request().text());
            } catch (Exception e) {
                log.warn("Async TTS synthesis failed for text='{}' (attempt {}/{}): {}",
                    job.request().text(), attempt, maxAttempts, e.getMessage());
            }
            if (attempt < maxAttempts) {
                // Pausa creciente: tras un fallo o un timeout el servidor puede seguir ocupado con la peticion anterior.
                Thread.sleep(retryDelayMs * attempt);
            }
        }
        log.warn("Giving up on text='{}' after {} attempts; it will be retried on the next warm-up",
            job.request().text(), maxAttempts);
    }

    /** Pausa toda la cola mientras el TTS no responde; asi no se descartan cientos de textos de golpe. */
    private void awaitTtsAvailable(AudioPort port) throws InterruptedException {
        boolean warned = false;
        while (!port.isAvailable()) {
            if (!warned) {
                log.warn("TTS service not available, pausing synthesis queue ({} pending)", pending.size());
                warned = true;
            }
            Thread.sleep(unavailableRetryDelayMs);
        }
        if (warned) {
            log.info("TTS service available again, resuming synthesis queue");
        }
    }
}
