package es.vargontoc.educational.framework.audio.infrastructure.adapters.in;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import es.vargontoc.educational.framework.audio.application.ports.in.AudioUseCase;
import es.vargontoc.educational.framework.audio.domain.AudioRequest;

class AudioPreWarmerTest {

    @Test
    void warmUp_synthesizesAllFixedTexts() {
        AudioUseCase audioUseCase = org.mockito.Mockito.mock(AudioUseCase.class);
        when(audioUseCase.getAudio(any(AudioRequest.class))).thenReturn(new byte[]{1, 2, 3});

        List<String> texts = List.of("Hola", "Adios", "Bienvenido");
        AudioPreWarmer warmer = new AudioPreWarmer(audioUseCase, 2, texts);

        warmer.warmUp();

        verify(audioUseCase, atLeast(3)).getAudio(any(AudioRequest.class));
    }

    @Test
    void warmUp_continuesOnFailure() {
        AudioUseCase audioUseCase = org.mockito.Mockito.mock(AudioUseCase.class);
        when(audioUseCase.getAudio(any(AudioRequest.class)))
            .thenThrow(new RuntimeException("TTS down"))
            .thenReturn(new byte[]{1})
            .thenReturn(new byte[]{2});

        List<String> texts = List.of("text1", "text2", "text3");
        AudioPreWarmer warmer = new AudioPreWarmer(audioUseCase, 2, texts);

        warmer.warmUp();

        verify(audioUseCase, atLeast(3)).getAudio(any(AudioRequest.class));
    }

    @Test
    void warmUp_usesDefaultTextsWhenConfigEmpty() {
        AudioUseCase audioUseCase = org.mockito.Mockito.mock(AudioUseCase.class);
        when(audioUseCase.getAudio(any(AudioRequest.class))).thenReturn(new byte[]{1});

        AudioPreWarmer warmer = new AudioPreWarmer(audioUseCase, 2, List.of());

        warmer.warmUp();

        verify(audioUseCase, atLeast(1)).getAudio(any(AudioRequest.class));
    }
}
