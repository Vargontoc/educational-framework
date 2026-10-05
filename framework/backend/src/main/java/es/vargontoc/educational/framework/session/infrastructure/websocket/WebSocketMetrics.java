package es.vargontoc.educational.framework.session.infrastructure.websocket;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class WebSocketMetrics {

    private static final String MESSAGE_TIMER = "ws.message.duration";
    private static final String PHASE_TIMER = "ws.message.phase.duration";
    private static final String AUDIO_CACHE_COUNTER = "ws.audio.cache";
    private static final String TTS_SYNTHESIS_TIMER = "ws.tts.synthesis.duration";
    private static final String SQL_STATEMENTS_COUNTER = "ws.sql.statements";
    private static final String DATA_CACHE_COUNTER = "ws.data.cache";

    private final MeterRegistry registry;
    private final Map<String, Timer> messageTimers = new ConcurrentHashMap<>();
    private final Map<String, Timer> phaseTimers = new ConcurrentHashMap<>();
    private final Counter audioCacheHit;
    private final Counter audioCacheMiss;
    private final Counter dataCacheHit;
    private final Counter dataCacheMiss;
    private final Timer ttsSynthesisTimer;

    public WebSocketMetrics(MeterRegistry registry) {
        this.registry = registry;
        this.audioCacheHit = Counter.builder(AUDIO_CACHE_COUNTER)
            .tag("result", "hit")
            .description("Audio cache lookup result")
            .register(registry);
        this.audioCacheMiss = Counter.builder(AUDIO_CACHE_COUNTER)
            .tag("result", "miss")
            .description("Audio cache lookup result")
            .register(registry);
        this.dataCacheHit = Counter.builder(DATA_CACHE_COUNTER)
            .tag("result", "hit")
            .description("Game data cache lookup result")
            .register(registry);
        this.dataCacheMiss = Counter.builder(DATA_CACHE_COUNTER)
            .tag("result", "miss")
            .description("Game data cache lookup result")
            .register(registry);
        this.ttsSynthesisTimer = Timer.builder(TTS_SYNTHESIS_TIMER)
            .description("TTS synthesis duration")
            .register(registry);
    }

    public void recordMessageDuration(String type, String outcome, long durationNanos) {
        String key = type + "|" + outcome;
        Timer timer = messageTimers.computeIfAbsent(key, k ->
            Timer.builder(MESSAGE_TIMER)
                .tag("type", type)
                .tag("outcome", outcome)
                .description("WebSocket message processing duration")
                .register(registry));
        timer.record(durationNanos, java.util.concurrent.TimeUnit.NANOSECONDS);
    }

    public void recordPhaseDuration(String messageType, String phase, String outcome, long durationNanos) {
        String key = messageType + "|" + phase + "|" + outcome;
        Timer timer = phaseTimers.computeIfAbsent(key, k ->
            Timer.builder(PHASE_TIMER)
                .tag("type", messageType)
                .tag("phase", phase)
                .tag("outcome", outcome)
                .description("WebSocket message phase duration")
                .register(registry));
        timer.record(durationNanos, java.util.concurrent.TimeUnit.NANOSECONDS);
    }

    public void recordAudioCacheHit() {
        audioCacheHit.increment();
    }

    public void recordAudioCacheMiss() {
        audioCacheMiss.increment();
    }

    public void recordDataCacheHit() {
        dataCacheHit.increment();
    }

    public void recordDataCacheMiss() {
        dataCacheMiss.increment();
    }

    public void recordTtsSynthesis(long durationNanos) {
        ttsSynthesisTimer.record(durationNanos, java.util.concurrent.TimeUnit.NANOSECONDS);
    }

    public void recordSqlStatements(String messageType, long count) {
        Counter.builder(SQL_STATEMENTS_COUNTER)
            .tag("type", messageType)
            .description("SQL statements executed per WS message")
            .register(registry)
            .increment(count);
    }

    public void registerGauge(String name, String tag, Supplier<Number> valueSupplier) {
        AtomicInteger unused = new AtomicInteger();
        registry.gauge(name,
            io.micrometer.core.instrument.Tags.of("kind", tag),
            unused,
            g -> valueSupplier.get().doubleValue());
    }

    public Timer.Sample startTiming() {
        return Timer.start(registry);
    }
}
