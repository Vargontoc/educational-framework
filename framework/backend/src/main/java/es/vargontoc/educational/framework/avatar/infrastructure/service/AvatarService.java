package es.vargontoc.educational.framework.avatar.infrastructure.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import es.vargontoc.educational.framework.audio.application.ports.in.AudioUseCase;
import es.vargontoc.educational.framework.audio.domain.AudioRequest;
import es.vargontoc.educational.framework.avatar.application.ports.in.AvatarUseCase;
import es.vargontoc.educational.framework.avatar.domain.AvatarEventRequest;
import es.vargontoc.educational.framework.avatar.domain.AvatarLifecycleResult;
import es.vargontoc.educational.framework.avatar.domain.GameAvatarEvent;
import es.vargontoc.educational.framework.avatar.domain.enums.AvatarEventType;
import es.vargontoc.educational.framework.avatar.infrastructure.validation.AvatarValidator;
import es.vargontoc.educational.framework.content.model.AvatarEventCatalog;
import es.vargontoc.educational.framework.content.ports.out.AvatarEventCatalogRepository;
import es.vargontoc.educational.framework.family.model.ChildProfile;
import es.vargontoc.educational.framework.family.ports.out.ChildProfileRepository;
import es.vargontoc.educational.framework.session.infrastructure.websocket.SessionEventType;
import es.vargontoc.educational.framework.session.model.ChildSession;
import es.vargontoc.educational.framework.session.model.ChildSessionStatus;
import es.vargontoc.educational.framework.session.ports.out.ChildSessionRepository;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
import es.vargontoc.educational.framework.shared.exception.SessionException;
import jakarta.transaction.Transactional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
public class AvatarService implements AvatarUseCase {

    private final AvatarValidator validator;

    private final ChildSessionRepository childSessionRepository;
    private final ChildProfileRepository childProfileRepository;
    private final AvatarEventCatalogRepository avatarEventCatalogRepository;

    private final AudioUseCase audio;

    private static final int MAX_LAST_SHOWN_ENTRIES = 512;

    private final Map<String, Long> lastShownCatalogIdByChild = new LinkedHashMap<>(64, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Long> eldest) {
            return size() > MAX_LAST_SHOWN_ENTRIES;
        }
    };

    public AvatarService(
            ChildSessionRepository childSessionRepository,
            ChildProfileRepository childProfileRepository,
            AvatarEventCatalogRepository avatarEventCatalogRepository,
            AudioUseCase audio
        ) {
        this.childSessionRepository = childSessionRepository;
        this.childProfileRepository = childProfileRepository;
        this.avatarEventCatalogRepository = avatarEventCatalogRepository;
        this.validator = new AvatarValidator();
        this.audio = audio;
    }

    @Override
    public AvatarLifecycleResult processEvent(AvatarEventRequest request) {
        validator.validateForProcess(request);

        ChildSession session = request.eventType() == AvatarEventType.FAREWELL
            ? findSession(request.childSessionId())
            : findActiveSession(request.childSessionId());
        ChildProfile childProfile = findChildProfile(session.getChildProfileId());

        String biome = null;
        if (request.eventType() == AvatarEventType.BIOME_TRANSITION && request.context() != null) {
            Object biomeValue = request.context().get("biome");
            if (biomeValue != null) {
                biome = biomeValue.toString();
            }
        }

        if(!childProfile.isNpcEnabled()) {
            return createFallbackResult(session.getId(), request.eventType(), "");
        }

        AvatarEventCatalog event = resolveCatalog(request.eventType(), childProfile, biome);
        String messageText = (event != null && event.getMessageText() != null) ? event.getMessageText() : "";

        if(childProfile.isNpcVoiceEnabled() && event != null && event.getMessageText() != null && !event.getMessageText().trim().isBlank()) {
            
            try {
                
                byte[] data = audio.getAudio(AudioRequest.withPreset(event.getMessageText().replace("<name>", childProfile.getName()), event.getTone()));
                return createResult(session.getId(), request.eventType(), data, messageText);
            }catch(Exception e) {
                log.error("Error generate audio: {}", e.getMessage(), e);
                return createFallbackResult(session.getId(), request.eventType(), messageText);
            }
        }

        return createFallbackResult(session.getId(), request.eventType(), messageText);
    }

    AvatarEventCatalog resolveCatalog(AvatarEventType type, ChildProfile profile, String biome) {
        List<AvatarEventCatalog> catalog;
        if (type == AvatarEventType.BIOME_TRANSITION) {
            catalog = avatarEventCatalogRepository.findActiveByEventTypeAndBiome(type, biome);
        } else {
            catalog = avatarEventCatalogRepository.findByEventType(type);
        }
        if(catalog.isEmpty())
            return null;

        String key = profile.getId() + ":" + type.name() + (biome != null ? ":" + biome : "");
        Long lastShownId;
        synchronized (lastShownCatalogIdByChild) {
            lastShownId = lastShownCatalogIdByChild.get(key);
        }

        List<AvatarEventCatalog> candidates = catalog;
        if(catalog.size() > 1 && lastShownId != null) {
            candidates = catalog.stream()
                .filter(c -> !lastShownId.equals(c.getId()))
                .toList();
        }

        AvatarEventCatalog selected = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        synchronized (lastShownCatalogIdByChild) {
            lastShownCatalogIdByChild.put(key, selected.getId());
        }
        return selected;
    }


    private ChildSession findActiveSession(Long sessionId) {
        ChildSession session = findSession(sessionId);

        if (!ChildSessionStatus.ACTIVE.equals(session.getStatus())) {
            throw new SessionException("Child session is not active");
        }

        return session;
    }

    private ChildSession findSession(Long sessionId) {
        return childSessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Child session not found: " + sessionId));
    }

    private ChildProfile findChildProfile(Long childProfileId) {
        return childProfileRepository.findById(childProfileId)
            .orElseThrow(() -> new ResourceNotFoundException("Child profile not found: " + childProfileId));
    }

    private AvatarLifecycleResult createResult(Long sessionId, AvatarEventType type, byte[] audio, String text) 
    {
        if(audio == null || audio.length <= 0)
            return createFallbackResult(sessionId, type, text);
        return new AvatarLifecycleResult(new GameAvatarEvent(SessionEventType.GAME_AVATAR_EVENT, sessionId, type.name(), true, UUID.randomUUID().toString(), text), audio);
    }

    private AvatarLifecycleResult createFallbackResult(Long sessionId, AvatarEventType type, String text) {
        return new AvatarLifecycleResult(new GameAvatarEvent(SessionEventType.GAME_AVATAR_EVENT, sessionId, type != null ? type.name() : null, false, null, text), null);
    }
    
    
}
