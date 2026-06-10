package es.vargontoc.educational.framework.avatar.service;

import es.vargontoc.educational.framework.avatar.infrastructure.dto.NarrateStorytellerRequest;
import es.vargontoc.educational.framework.avatar.infrastructure.tts.TtsException;
import es.vargontoc.educational.framework.avatar.model.StorytellerResult;
import es.vargontoc.educational.framework.avatar.ports.in.NarrateStorytellerUseCase;
import es.vargontoc.educational.framework.avatar.ports.out.TtsClient;
import es.vargontoc.educational.framework.avatar.validation.NarrateStorytellerValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class NarrateStorytellerService implements NarrateStorytellerUseCase {

    private static final Logger log = LoggerFactory.getLogger(NarrateStorytellerService.class);
    private static final String VOICE_PROFILE_STORYTELLER = "storyteller";

    private final TtsClient ttsClient;
    private final NarrateStorytellerValidator validator;

    public NarrateStorytellerService(TtsClient ttsClient) {
        this.ttsClient = ttsClient;
        this.validator = new NarrateStorytellerValidator();
    }

    @Override
    public StorytellerResult narrate(NarrateStorytellerRequest request) {
        validator.validateForNarrate(request);

        String text = request.text();
        String locale = request.localeOrDefault();
        var tone = request.toneOrDefault();

        try {
            byte[] audioData = ttsClient.synthesize(text, locale, tone, VOICE_PROFILE_STORYTELLER);
            boolean audioAvailable = audioData != null && audioData.length > 0;
            return createResult(text, audioData, audioAvailable);
        } catch (TtsException e) {
            log.warn("Storyteller TTS failed: errorCode={}, message={}", e.getErrorCode(), e.getMessage());
            return createResult(text, null, false);
        }
    }

    private StorytellerResult createResult(String text, byte[] audioData, boolean audioAvailable) {
        StorytellerResult result = new StorytellerResult();
        result.setText(text);
        result.setAudioAvailable(audioAvailable);
        result.setAudioMetadata(audioAvailable && audioData != null
            ? Map.of("format", "mp3", "size", audioData.length)
            : null);
        result.setAudioData(audioData);
        return result;
    }
}