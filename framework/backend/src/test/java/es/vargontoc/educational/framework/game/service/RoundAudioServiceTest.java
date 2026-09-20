package es.vargontoc.educational.framework.game.service;

import es.vargontoc.educational.framework.audio.application.ports.in.AudioUseCase;
import es.vargontoc.educational.framework.audio.domain.AudioRequest;
import es.vargontoc.educational.framework.content.model.RecognitionElement;
import es.vargontoc.educational.framework.content.ports.out.RecognitionElementRepository;
import es.vargontoc.educational.framework.family.model.ChildProfile;
import es.vargontoc.educational.framework.family.ports.in.ChildProfileUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RoundAudioServiceTest {

    private RecognitionElementRepository recognitionElementRepository;
    private ChildProfileUseCase childProfileUseCase;
    private AudioUseCase audioUseCase;
    private RoundAudioService service;

    @BeforeEach
    void setUp() {
        recognitionElementRepository = mock(RecognitionElementRepository.class);
        childProfileUseCase = mock(ChildProfileUseCase.class);
        audioUseCase = mock(AudioUseCase.class);
        service = new RoundAudioService(recognitionElementRepository, childProfileUseCase, audioUseCase);
    }

    private ChildProfile buildProfile(boolean npcEnabled, boolean npcVoiceEnabled) {
        ChildProfile profile = new ChildProfile();
        profile.setId(1L);
        profile.setNpcEnabled(npcEnabled);
        profile.setNpcVoiceEnabled(npcVoiceEnabled);
        return profile;
    }

    private RecognitionElement buildElement(Long id, String code, String resourceRefs) {
        RecognitionElement element = new RecognitionElement();
        element.setId(id);
        element.setCode(code);
        element.setResourceRefs(resourceRefs);
        return element;
    }

    @Test
    void generateRoundAudio_extractsCorrectTextFromResourceRefs() {
        when(childProfileUseCase.getChild(1L)).thenReturn(buildProfile(true, true));
        when(recognitionElementRepository.findAllById(List.of(42L)))
                .thenReturn(List.of(buildElement(42L, "letter_a", "{\"nubi-audio\": \"¿Dónde está la letra A?\"}")));
        when(audioUseCase.getAudio(any(AudioRequest.class))).thenReturn(new byte[]{1, 2, 3});

        RoundAudioResult result = service.generateRoundAudio(1L, "42");

        assertTrue(result.audioAvailable());
        assertNotNull(result.audioId());
        assertEquals("¿Dónde está la letra A?", result.text());
        assertNotNull(result.audioData());
        verify(audioUseCase).getAudio(any(AudioRequest.class));
    }

    @Test
    void generateRoundAudio_noAudioIfNpcDisabled() {
        when(childProfileUseCase.getChild(1L)).thenReturn(buildProfile(false, true));

        RoundAudioResult result = service.generateRoundAudio(1L, "42");

        assertFalse(result.audioAvailable());
        verify(audioUseCase, never()).getAudio(any());
    }

    @Test
    void generateRoundAudio_noAudioIfNpcVoiceDisabled() {
        when(childProfileUseCase.getChild(1L)).thenReturn(buildProfile(true, false));

        RoundAudioResult result = service.generateRoundAudio(1L, "42");

        assertFalse(result.audioAvailable());
        verify(audioUseCase, never()).getAudio(any());
    }

    @Test
    void generateRoundAudio_noAudioIfElementNotFound() {
        when(childProfileUseCase.getChild(1L)).thenReturn(buildProfile(true, true));
        when(recognitionElementRepository.findAllById(List.of(99L))).thenReturn(List.of());

        RoundAudioResult result = service.generateRoundAudio(1L, "99");

        assertFalse(result.audioAvailable());
        verify(audioUseCase, never()).getAudio(any());
    }

    @Test
    void generateRoundAudio_noAudioIfResourceRefsMissing() {
        when(childProfileUseCase.getChild(1L)).thenReturn(buildProfile(true, true));
        when(recognitionElementRepository.findAllById(List.of(42L)))
                .thenReturn(List.of(buildElement(42L, "letter_a", null)));

        RoundAudioResult result = service.generateRoundAudio(1L, "42");

        assertFalse(result.audioAvailable());
        verify(audioUseCase, never()).getAudio(any());
    }

    @Test
    void generateRoundAudio_fallbackWhenAudioGenerationFails() {
        when(childProfileUseCase.getChild(1L)).thenReturn(buildProfile(true, true));
        when(recognitionElementRepository.findAllById(List.of(42L)))
                .thenReturn(List.of(buildElement(42L, "letter_a", "{\"nubi-audio\": \"test\"}")));
        when(audioUseCase.getAudio(any(AudioRequest.class))).thenThrow(new RuntimeException("TTS unavailable"));

        RoundAudioResult result = service.generateRoundAudio(1L, "42");

        assertFalse(result.audioAvailable());
        assertEquals("test", result.text());
    }

    @Test
    void generateRoundAudio_noAudioIfChildProfileNotFound() {
        when(childProfileUseCase.getChild(1L)).thenThrow(new RuntimeException("Not found"));

        RoundAudioResult result = service.generateRoundAudio(1L, "42");

        assertFalse(result.audioAvailable());
        verify(audioUseCase, never()).getAudio(any());
    }

    @Test
    void generateRoundAudio_noAudioForNullInputs() {
        RoundAudioResult result1 = service.generateRoundAudio(null, "42");
        RoundAudioResult result2 = service.generateRoundAudio(1L, null);
        RoundAudioResult result3 = service.generateRoundAudio(1L, "");

        assertFalse(result1.audioAvailable());
        assertFalse(result2.audioAvailable());
        assertFalse(result3.audioAvailable());
    }

    @Test
    void extractNubiAudioFromResourceRefs_parsesJson() {
        String text = service.extractNubiAudioFromResourceRefs("{\"nubi-audio\": \"Hello World\"}");
        assertEquals("Hello World", text);
    }

    @Test
    void extractNubiAudioFromResourceRefs_returnsNullForMissingKey() {
        String text = service.extractNubiAudioFromResourceRefs("{\"other-key\": \"value\"}");
        assertNull(text);
    }

    @Test
    void extractNubiAudioFromResourceRefs_returnsNullForNullInput() {
        assertNull(service.extractNubiAudioFromResourceRefs(null));
        assertNull(service.extractNubiAudioFromResourceRefs(""));
    }

    @Test
    void extractNubiAudioFromResourceRefs_returnsNullForInvalidJson() {
        assertNull(service.extractNubiAudioFromResourceRefs("not json"));
    }
}
