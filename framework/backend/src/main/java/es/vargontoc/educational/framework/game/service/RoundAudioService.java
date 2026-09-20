package es.vargontoc.educational.framework.game.service;

import es.vargontoc.educational.framework.audio.application.ports.in.AudioUseCase;
import es.vargontoc.educational.framework.audio.domain.AudioRequest;
import es.vargontoc.educational.framework.audio.domain.enums.TonePreset;
import es.vargontoc.educational.framework.content.model.RecognitionElement;
import es.vargontoc.educational.framework.content.ports.out.RecognitionElementRepository;
import es.vargontoc.educational.framework.family.model.ChildProfile;
import es.vargontoc.educational.framework.family.ports.in.ChildProfileUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.UUID;

/**
 * Service that generates Nubi audio for recognition rounds.
 * Extracts the narration text from RecognitionElement.resourceRefs["nubi-audio"],
 * checks NPC settings on the child profile, and generates audio via TTS.
 */
public class RoundAudioService {

    private static final Logger log = LoggerFactory.getLogger(RoundAudioService.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String NUBI_AUDIO_KEY = "nubi-audio";

    private final RecognitionElementRepository recognitionElementRepository;
    private final ChildProfileUseCase childProfileUseCase;
    private final AudioUseCase audioUseCase;

    public RoundAudioService(
            RecognitionElementRepository recognitionElementRepository,
            ChildProfileUseCase childProfileUseCase,
            AudioUseCase audioUseCase) {
        this.recognitionElementRepository = recognitionElementRepository;
        this.childProfileUseCase = childProfileUseCase;
        this.audioUseCase = audioUseCase;
    }

    /**
     * Generates audio for the target element of a recognition round.
     *
     * @param childProfileId  the child profile to check NPC settings
     * @param targetElementId the element ID (numeric string) to get narration text
     * @return RoundAudioResult with audio data, or noAudio if NPC is disabled or generation fails
     */
    public RoundAudioResult generateRoundAudio(Long childProfileId, String targetElementId) {
        if (childProfileId == null || targetElementId == null || targetElementId.isBlank()) {
            return RoundAudioResult.noAudio(null);
        }

        // Check NPC settings
        if (!isNpcAudioEnabled(childProfileId)) {
            log.debug("NPC audio disabled for childProfileId={}, skipping round audio", childProfileId);
            return RoundAudioResult.noAudio(null);
        }

        // Resolve element and extract text
        String text = resolveNubiAudioText(targetElementId);
        if (text == null || text.isBlank()) {
            log.debug("No nubi-audio text found for elementId={}", targetElementId);
            return RoundAudioResult.noAudio(null);
        }

        // Generate audio
        try {
            byte[] audioData = audioUseCase.getAudio(AudioRequest.withPreset(text, TonePreset.CALM));
            if (audioData == null || audioData.length == 0) {
                log.warn("TTS returned empty audio for text='{}'", text);
                return RoundAudioResult.noAudio(text);
            }
            String audioId = UUID.randomUUID().toString();
            return RoundAudioResult.withAudio(audioId, audioData, text);
        } catch (Exception e) {
            log.warn("Failed to generate round audio for elementId={}: {}", targetElementId, e.getMessage());
            return RoundAudioResult.noAudio(text);
        }
    }

    /**
     * Checks whether NPC audio is enabled for the given child profile.
     * Both npcEnabled and npcVoiceEnabled must be true.
     */
    private boolean isNpcAudioEnabled(Long childProfileId) {
        try {
            ChildProfile profile = childProfileUseCase.getChild(childProfileId);
            return profile != null && profile.isNpcEnabled() && profile.isNpcVoiceEnabled();
        } catch (Exception e) {
            log.warn("Failed to resolve ChildProfile {} for NPC check: {}", childProfileId, e.getMessage());
            return false;
        }
    }

    /**
     * Extracts the "nubi-audio" text from a RecognitionElement's resourceRefs JSON.
     */
    String resolveNubiAudioText(String targetElementId) {
        try {
            Long elementId = Long.valueOf(targetElementId);
            var elements = recognitionElementRepository.findAllById(java.util.List.of(elementId));
            if (elements.isEmpty()) {
                return null;
            }
            RecognitionElement element = elements.get(0);
            return extractNubiAudioFromResourceRefs(element.getResourceRefs());
        } catch (NumberFormatException e) {
            log.debug("targetElementId '{}' is not a numeric ID", targetElementId);
            return null;
        }
    }

    /**
     * Parses the resourceRefs JSON string and extracts the "nubi-audio" value.
     */
    String extractNubiAudioFromResourceRefs(String resourceRefs) {
        if (resourceRefs == null || resourceRefs.isBlank()) {
            return null;
        }
        try {
            Map<String, String> refs = OBJECT_MAPPER.readValue(
                    resourceRefs, new TypeReference<Map<String, String>>() {});
            return refs.get(NUBI_AUDIO_KEY);
        } catch (JacksonException e) {
            log.warn("Failed to parse resourceRefs JSON: {}", e.getMessage());
            return null;
        }
    }
}
