package es.vargontoc.educational.framework.audio.infrastructure.adapters.in;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import es.vargontoc.educational.framework.audio.application.ports.in.AudioTextSource;
import es.vargontoc.educational.framework.audio.application.ports.in.AudioUseCase;
import es.vargontoc.educational.framework.audio.domain.AudioRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Genera al arrancar los audios de todo el texto curado de la aplicacion (frases de Nubi, indicaciones de las
 * rondas, curiosidades...), para que ningun niño espere ni se quede sin audio la primera vez.
 *
 * Recorre en CADA arranque todas las fuentes de texto con los datos vigentes y encola lo que falte en cache;
 * lo ya generado (cache en disco) se salta. Asi un fallo anterior, un texto nuevo o un cambio de contenido se
 * recuperan solos. La sintesis real la hace la cola de AudioAsync, de una en una.
 *
 * Los textos que dependen del nombre del niño se generan aqui para los perfiles existentes y, en ejecucion,
 * cuando se crea o renombra un perfil (AvatarService.generateEventWithName).
 */
@Slf4j
@Component
public class AudioWarmupService {

    private final AudioUseCase audioUseCase;
    private final ObjectProvider<AudioTextSource> sources;
    private final boolean enabled;

    public AudioWarmupService(
            AudioUseCase audioUseCase,
            ObjectProvider<AudioTextSource> sources,
            @Value("${app.audio.warmup.enabled:true}") boolean enabled) {
        this.audioUseCase = audioUseCase;
        this.sources = sources;
        this.enabled = enabled;
    }

    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        warmUp();
    }

    /** @return numero de textos distintos encolados para calentar (los ya cacheados los descarta AudioUseCase). */
    public int warmUp() {
        if (!enabled) {
            log.info("Audio warm-up disabled (app.audio.warmup.enabled=false)");
            return 0;
        }

        // Se deduplica por texto+tono: varias fuentes pueden compartir una frase.
        Map<String, AudioRequest> unique = new LinkedHashMap<>();
        sources.orderedStream().forEach(source -> {
            try {
                List<AudioRequest> requests = source.requests();
                int before = unique.size();
                for (AudioRequest request : requests) {
                    if (request == null || request.text() == null || request.text().isBlank()) continue;
                    unique.putIfAbsent(request.text().strip() + "|" + request.exageration() + "|" + request.cfg()
                        + "|" + request.temperature(), request);
                }
                log.info("Audio warm-up source '{}': {} texts ({} new)", source.name(), requests.size(),
                    unique.size() - before);
            } catch (Exception e) {
                // Una fuente que falla no debe impedir calentar las demas.
                log.warn("Audio warm-up source '{}' failed: {}", source.name(), e.getMessage(), e);
            }
        });

        log.info("Audio warm-up: queuing {} curated texts for synthesis (already cached ones are skipped)", unique.size());
        int queued = 0;
        for (AudioRequest request : unique.values()) {
            try {
                audioUseCase.warm(request);
                queued++;
            } catch (Exception e) {
                log.warn("Audio warm-up could not queue text='{}': {}", request.text(), e.getMessage());
            }
        }
        return queued;
    }
}
