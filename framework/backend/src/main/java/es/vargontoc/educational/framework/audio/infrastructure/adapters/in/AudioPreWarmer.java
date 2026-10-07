package es.vargontoc.educational.framework.audio.infrastructure.adapters.in;

import java.util.List;
import java.util.concurrent.Semaphore;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import es.vargontoc.educational.framework.audio.application.ports.in.AudioUseCase;
import es.vargontoc.educational.framework.audio.domain.AudioRequest;
import es.vargontoc.educational.framework.audio.domain.enums.TonePreset;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AudioPreWarmer {

    private final AudioUseCase audioUseCase;
    private final Semaphore concurrencyLimit;
    private final List<String> fixedTexts;

    public AudioPreWarmer(
            AudioUseCase audioUseCase,
            @Value("${app.audio.prewarm.concurrency:2}") int concurrency,
            @Value("${app.audio.prewarm.texts:}") List<String> configuredTexts) {
        this.audioUseCase = audioUseCase;
        this.concurrencyLimit = new Semaphore(concurrency);
        this.fixedTexts = (configuredTexts != null && !configuredTexts.isEmpty())
            ? configuredTexts
            : defaultFixedTexts();
    }

    private List<String> defaultFixedTexts() {
        return List.of(
            "Hola, soy Nubi. Vamos a jugar.",
            "Bienvenido al mundo de Nubi.",
            "Muy bien, lo has conseguido.",
            "Vamos a por la siguiente.",
            "Has terminado el juego. Hasta pronto."
        );
    }

    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        warmUp();
    }

    public void warmUp() {
        log.info("Starting audio pre-warming for {} fixed texts", fixedTexts.size());
        for (String text : fixedTexts) {
            try {
                concurrencyLimit.acquire();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Audio pre-warming interrupted");
                return;
            }
            try {
                AudioRequest request = AudioRequest.withPreset(text, TonePreset.CALM);
                byte[] result = audioUseCase.getAudio(request);
                if (result != null) {
                    log.debug("Pre-warmed audio for text='{}'", text);
                } else {
                    log.debug("Pre-warm cache miss (async synthesis started) for text='{}'", text);
                }
            } catch (Exception e) {
                log.warn("Pre-warm failed for text='{}': {}", text, e.getMessage());
            } finally {
                concurrencyLimit.release();
            }
        }
        log.info("Audio pre-warming completed");
    }
}
