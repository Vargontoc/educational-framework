package es.vargontoc.educational.framework.game.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import es.vargontoc.educational.framework.audio.application.ports.in.AudioTextSource;
import es.vargontoc.educational.framework.audio.domain.AudioRequest;
import es.vargontoc.educational.framework.audio.domain.enums.TonePreset;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.RecognitionElement;
import es.vargontoc.educational.framework.content.ports.out.RecognitionElementRepository;

/**
 * Indicacion de Nubi de cada elemento de reconocimiento activo (letras, numeros, animales, colores, formas,
 * comparacion y memoria). Usa el mismo texto y tono que RoundAudioService pide en ejecucion, para que la clave
 * de cache coincida y la ronda encuentre el audio ya generado.
 */
@Component
public class RecognitionAudioTextSource implements AudioTextSource {

    private final RecognitionElementRepository elements;

    public RecognitionAudioTextSource(RecognitionElementRepository elements) {
        this.elements = elements;
    }

    @Override
    public String name() {
        return "recognition-elements";
    }

    @Override
    public List<AudioRequest> requests() {
        List<AudioRequest> requests = new ArrayList<>();
        for (RecognitionElement element : elements.findAllByStatus(ContentStatus.ACTIVE)) {
            String text = RecognitionResourceRefs.nubiAudio(element.getResourceRefs());
            if (text != null && !text.isBlank()) {
                requests.add(AudioRequest.withPreset(text, TonePreset.CALM));
            }
        }
        return requests;
    }
}
