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

import java.util.UUID;

/**
 * Service that generates Nubi audio for recognition rounds.
 * Extracts the narration text from RecognitionElement.resourceRefs["nubi-audio"],
 * checks NPC settings on the child profile, and generates audio via TTS.
 */
public class RoundAudioService {

    private static final Logger log = LoggerFactory.getLogger(RoundAudioService.class);

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
        return generateRoundAudio(childProfileId, targetElementId, null, null);
    }

    /**
     * Generates audio for the target element of a recognition round, reusing pre-loaded data.
     *
     * @param childProfileId  the child profile to check NPC settings
     * @param targetElementId the element ID (numeric string) to get narration text
     * @param preloadedProfile the already-loaded ChildProfile (may be null to fetch from DB)
     * @param preloadedElement the already-loaded RecognitionElement (may be null to fetch from DB)
     * @return RoundAudioResult with audio data, or noAudio if NPC is disabled or generation fails
     */
    public RoundAudioResult generateRoundAudio(Long childProfileId, String targetElementId,
                                                ChildProfile preloadedProfile,
                                                RecognitionElement preloadedElement) {
        if (childProfileId == null || targetElementId == null || targetElementId.isBlank()) {
            return RoundAudioResult.noAudio(null);
        }

        if (!isNpcAudioEnabled(childProfileId, preloadedProfile)) {
            log.debug("NPC audio disabled for childProfileId={}, skipping round audio", childProfileId);
            return RoundAudioResult.noAudio(null);
        }

        String text = resolveNubiAudioText(targetElementId, preloadedElement);
        if (text == null || text.isBlank()) {
            log.debug("No nubi-audio text found for elementId={}", targetElementId);
            return RoundAudioResult.noAudio(null);
        }

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

    private boolean isNpcAudioEnabled(Long childProfileId) {
        return isNpcAudioEnabled(childProfileId, null);
    }

    private boolean isNpcAudioEnabled(Long childProfileId, ChildProfile preloadedProfile) {
        try {
            ChildProfile profile = preloadedProfile;
            if (profile == null) {
                profile = childProfileUseCase.getChild(childProfileId);
            }
            return profile != null && profile.isNpcEnabled() && profile.isNpcVoiceEnabled();
        } catch (Exception e) {
            log.warn("Failed to resolve ChildProfile {} for NPC check: {}", childProfileId, e.getMessage());
            return false;
        }
    }

    String resolveNubiAudioText(String targetElementId) {
        return resolveNubiAudioText(targetElementId, null);
    }

    String resolveNubiAudioText(String targetElementId, RecognitionElement preloadedElement) {
        try {
            RecognitionElement element = preloadedElement;
            if (element == null) {
                Long elementId = Long.valueOf(targetElementId);
                var elements = recognitionElementRepository.findAllById(java.util.List.of(elementId));
                if (elements.isEmpty()) {
                    return null;
                }
                element = elements.get(0);
            }
            return extractNubiAudioFromResourceRefs(element.getResourceRefs());
        } catch (NumberFormatException e) {
            log.debug("targetElementId '{}' is not a numeric ID", targetElementId);
            return null;
        }
    }

    String extractNubiAudioFromResourceRefs(String resourceRefs) {
        return RecognitionResourceRefs.nubiAudio(resourceRefs);
    }
}
