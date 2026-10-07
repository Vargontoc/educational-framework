package es.vargontoc.educational.framework.game.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import es.vargontoc.educational.framework.audio.domain.AudioRequest;
import es.vargontoc.educational.framework.audio.domain.enums.TonePreset;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.RecognitionElement;
import es.vargontoc.educational.framework.content.ports.out.RecognitionElementRepository;

class RecognitionAudioTextSourceTest {

    private static RecognitionElement element(String resourceRefs) {
        RecognitionElement element = new RecognitionElement();
        element.setResourceRefs(resourceRefs);
        return element;
    }

    @Test
    void usesTheSameTextAndToneAsTheRuntimeRoundPrompt() {
        RecognitionElementRepository repository = mock(RecognitionElementRepository.class);
        when(repository.findAllByStatus(ContentStatus.ACTIVE)).thenReturn(List.of(
            element("{\"nubi-audio\":\"Toca el gato\"}"),
            element("{\"image\":\"cat\"}"),
            element(null),
            element("{\"nubi-audio\":\"Toca el perro\"}")));

        List<AudioRequest> requests = new RecognitionAudioTextSource(repository).requests();

        assertEquals(List.of("Toca el gato", "Toca el perro"), requests.stream().map(AudioRequest::text).toList());
        assertEquals(AudioRequest.withPreset("Toca el gato", TonePreset.CALM), requests.get(0));
    }
}
