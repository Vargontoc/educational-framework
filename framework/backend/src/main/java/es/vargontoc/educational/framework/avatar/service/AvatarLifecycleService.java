package es.vargontoc.educational.framework.avatar.service;

import es.vargontoc.educational.framework.avatar.infrastructure.dto.GameAvatarEvent;
import es.vargontoc.educational.framework.avatar.infrastructure.tts.TtsException;
import es.vargontoc.educational.framework.avatar.ports.out.TtsClient;
import es.vargontoc.educational.framework.content.model.AvatarTone;
import es.vargontoc.educational.framework.family.model.ChildProfile;
import es.vargontoc.educational.framework.family.ports.out.ChildProfileRepository;
import es.vargontoc.educational.framework.session.model.ChildSession;
import es.vargontoc.educational.framework.session.ports.out.ChildSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;

public class AvatarLifecycleService {

    private static final Logger log = LoggerFactory.getLogger(AvatarLifecycleService.class);
    private static final String WELCOME_TEXT = "Hola, vamos a jugar!";
    private static final String FAREWELL_TEXT = "Vaya, parece que es hora de despedirnos. Hasta la proxima.";
    private static final String CONTEXT_NPC = "npc";
    private static final AvatarTone DEFAULT_TONE = AvatarTone.NEUTRAL;
    private static final String DEFAULT_LOCALE = "es";

    private final TtsClient ttsClient;
    private final ChildProfileRepository profileRepository;
    private final ChildSessionRepository sessionRepository;

    public AvatarLifecycleService(
            TtsClient ttsClient,
            ChildProfileRepository profileRepository,
            ChildSessionRepository sessionRepository) {
        this.ttsClient = ttsClient;
        this.profileRepository = profileRepository;
        this.sessionRepository = sessionRepository;
    }

    public AvatarLifecycleResult welcome(Long childSessionId) {
        Optional<ChildSession> sessionOpt = sessionRepository.findById(childSessionId);
        if (sessionOpt.isEmpty()) {
            log.debug("Welcome: session not found {}", childSessionId);
            return AvatarLifecycleResult.noSession(childSessionId);
        }

        ChildSession session = sessionOpt.get();
        Optional<ChildProfile> profileOpt = profileRepository.findById(session.getChildProfileId());
        if (profileOpt.isEmpty()) {
            log.debug("Welcome: profile not found for session {}", childSessionId);
            return AvatarLifecycleResult.noSession(childSessionId);
        }

        ChildProfile profile = profileOpt.get();
        if (!profile.isAgentEnabled()) {
            log.debug("Welcome: agent disabled for session {}", childSessionId);
            return AvatarLifecycleResult.suppressed(childSessionId);
        }

        return buildAvatarEvent(childSessionId, WELCOME_TEXT, true, profile);
    }

    public AvatarLifecycleResult farewell(Long childSessionId) {
        Optional<ChildSession> sessionOpt = sessionRepository.findById(childSessionId);
        if (sessionOpt.isEmpty()) {
            log.debug("Farewell: session not found {}", childSessionId);
            return AvatarLifecycleResult.noSession(childSessionId);
        }

        ChildSession session = sessionOpt.get();
        Optional<ChildProfile> profileOpt = profileRepository.findById(session.getChildProfileId());
        if (profileOpt.isEmpty()) {
            log.debug("Farewell: profile not found for session {}", childSessionId);
            return AvatarLifecycleResult.noSession(childSessionId);
        }

        ChildProfile profile = profileOpt.get();
        if (!profile.isAgentEnabled()) {
            log.debug("Farewell: agent disabled for session {}", childSessionId);
            return AvatarLifecycleResult.suppressed(childSessionId);
        }

        return buildAvatarEvent(childSessionId, FAREWELL_TEXT, false, profile);
    }

    private AvatarLifecycleResult buildAvatarEvent(Long childSessionId, String text, boolean isWelcome, ChildProfile profile) {
        boolean ttsEnabled = profile.isTtsEnabled();

        if (!ttsEnabled) {
            GameAvatarEvent event = isWelcome
                ? GameAvatarEvent.welcome(childSessionId, false, null, text)
                : GameAvatarEvent.farewell(childSessionId, false, null, text);
            return new AvatarLifecycleResult(event, null);
        }

        try {
            byte[] audioData = ttsClient.synthesize(text, DEFAULT_LOCALE, DEFAULT_TONE, CONTEXT_NPC);
            if (audioData != null && audioData.length > 0) {
                String audioId = UUID.randomUUID().toString();
                GameAvatarEvent event = isWelcome
                    ? GameAvatarEvent.welcome(childSessionId, true, audioId, text)
                    : GameAvatarEvent.farewell(childSessionId, true, audioId, text);
                return new AvatarLifecycleResult(event, audioData);
            }
        } catch (TtsException e) {
            log.warn("Avatar lifecycle TTS failed for session {}: errorCode={}, message={}",
                childSessionId, e.getErrorCode(), e.getMessage());
        }

        GameAvatarEvent event = isWelcome
            ? GameAvatarEvent.welcome(childSessionId, false, null, text)
            : GameAvatarEvent.farewell(childSessionId, false, null, text);
        return new AvatarLifecycleResult(event, null);
    }

    public record AvatarLifecycleResult(
        GameAvatarEvent event,
        byte[] audioData
    ) {
        public static AvatarLifecycleResult noSession(Long sessionId) {
            return new AvatarLifecycleResult(null, null);
        }

        public static AvatarLifecycleResult suppressed(Long sessionId) {
            return new AvatarLifecycleResult(null, null);
        }

        public boolean isPresent() {
            return event != null;
        }
    }
}