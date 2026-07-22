package es.vargontoc.educational.framework.avatar.service;

import es.vargontoc.educational.framework.avatar.infrastructure.dto.NarrateStorytellerRequest;
import es.vargontoc.educational.framework.avatar.infrastructure.tts.TtsException;
import es.vargontoc.educational.framework.avatar.model.StorytellerResult;
import es.vargontoc.educational.framework.avatar.ports.out.TtsClient;
import es.vargontoc.educational.framework.content.model.AvatarTone;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NarrateStorytellerServiceTest {

    @Mock
    private TtsClient ttsClient;

    private NarrateStorytellerService service;

    @BeforeEach
    void setUp() {
        service = new NarrateStorytellerService(ttsClient);
    }

    @Test
    void narrate_usesNarrationContext() {
        byte[] audioData = "mp3-data".getBytes();
        when(ttsClient.synthesize(anyString(), anyString(), any(), eq("narration")))
            .thenReturn(audioData);

        NarrateStorytellerRequest request = new NarrateStorytellerRequest("Hello world", "es", AvatarTone.CALM);
        StorytellerResult result = service.narrate(request);

        assertNotNull(result);
        assertTrue(result.isAudioAvailable());
        assertEquals("Hello world", result.getText());
        verify(ttsClient).synthesize("Hello world", "es", AvatarTone.CALM, "narration");
    }

    @Test
    void narrate_doesNotRequireAvatarEventCatalog() {
        byte[] audioData = "mp3-data".getBytes();
        when(ttsClient.synthesize(anyString(), anyString(), any(), anyString()))
            .thenReturn(audioData);

        NarrateStorytellerRequest request = new NarrateStorytellerRequest("Direct narration text", "en", AvatarTone.NEUTRAL);
        StorytellerResult result = service.narrate(request);

        assertNotNull(result);
        assertEquals("Direct narration text", result.getText());
        assertTrue(result.isAudioAvailable());
    }

    @Test
    void narrationCacheKeyDiffersFromNpc() {
        byte[] audioData = "mp3-data".getBytes();
        when(ttsClient.synthesize(anyString(), anyString(), any(), anyString()))
            .thenReturn(audioData);

        ArgumentCaptor<String> contextCaptor = ArgumentCaptor.forClass(String.class);

        service.narrate(new NarrateStorytellerRequest("Same text", "es", AvatarTone.CALM));
        verify(ttsClient).synthesize(anyString(), anyString(), any(), contextCaptor.capture());

        assertEquals("narration", contextCaptor.getValue());
    }

    @Test
    void narrate_blankText_throwsValidationException() {
        assertThrows(ValidationException.class, () ->
            service.narrate(new NarrateStorytellerRequest("", "es", AvatarTone.CALM)));

        assertThrows(ValidationException.class, () ->
            service.narrate(new NarrateStorytellerRequest("   ", "es", AvatarTone.CALM)));

        assertThrows(ValidationException.class, () ->
            service.narrate(new NarrateStorytellerRequest(null, "es", AvatarTone.CALM)));
    }

    @Test
    void narrate_ttsFailure_returnsTextFallback() {
        when(ttsClient.synthesize(anyString(), anyString(), any(), anyString()))
            .thenThrow(new TtsException("TTS timeout", "CONNECTION_ERROR", true));

        NarrateStorytellerRequest request = new NarrateStorytellerRequest("Storytelling text", "es", AvatarTone.JOYFUL);
        StorytellerResult result = service.narrate(request);

        assertNotNull(result);
        assertEquals("Storytelling text", result.getText());
        assertFalse(result.isAudioAvailable());
        assertNull(result.getAudioData());
        assertNull(result.getAudioMetadata());
    }

    @Test
    void narrate_ttsDisabled_returnsTextOnly() {
        when(ttsClient.synthesize(anyString(), anyString(), any(), anyString()))
            .thenThrow(new TtsException("TTS is disabled", "TTS_DISABLED", false));

        NarrateStorytellerRequest request = new NarrateStorytellerRequest("Text only narration", "es", AvatarTone.NEUTRAL);
        StorytellerResult result = service.narrate(request);

        assertNotNull(result);
        assertEquals("Text only narration", result.getText());
        assertFalse(result.isAudioAvailable());
        assertNull(result.getAudioData());
        verify(ttsClient, times(1)).synthesize(anyString(), anyString(), any(), anyString());
    }
}