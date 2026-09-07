package es.vargontoc.educational.framework.avatar.infrastructure.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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

    private final Map<String, Long> lastShownCatalogIdByChild = new ConcurrentHashMap<>();

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

        ChildSession session = findActiveSession(request.childSessionId());
        ChildProfile childProfile = findChildProfile(session.getChildProfileId());

        if(!childProfile.isNpcEnabled()) {
            return createFallbackResult(session.getId(), request.eventType());
        }

        AvatarEventCatalog event = resolveCatalog(request.eventType(), childProfile);

        if(childProfile.isNpcVoiceEnabled() && event != null && event.getMessageText() != null && !event.getMessageText().trim().isBlank()) {
            
            try {
                
                byte[] data = audio.getAudio(AudioRequest.withPreset(event.getMessageText().replace("<name>", childProfile.getName()), event.getTone()));
                return createResult(session.getId(), request.eventType(), data);
            }catch(Exception e) {
                log.error("Error generate audio: {}", e.getMessage(), e);
                return createFallbackResult(session.getId(), request.eventType());
            }
        }

        return createFallbackResult(session.getId(), request.eventType());
    }

    AvatarEventCatalog resolveCatalog(AvatarEventType type, ChildProfile profile) {
        List<AvatarEventCatalog> catalog = avatarEventCatalogRepository.findByEventType(type);
        if(catalog.isEmpty())
            return null;

        String key = profile.getId() + ":" + type.name();
        Long lastShownId = lastShownCatalogIdByChild.get(key);

        List<AvatarEventCatalog> candidates = catalog;
        if(catalog.size() > 1 && lastShownId != null) {
            candidates = catalog.stream()
                .filter(c -> !lastShownId.equals(c.getId()))
                .toList();
        }

        AvatarEventCatalog selected = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        lastShownCatalogIdByChild.put(key, selected.getId());
        return selected;
    }


    private ChildSession findActiveSession(Long sessionId) {
        ChildSession session = childSessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Child session not found: " + sessionId));

        if (!ChildSessionStatus.ACTIVE.equals(session.getStatus())) {
            throw new SessionException("Child session is not active");
        }

        return session;
    }

    private ChildProfile findChildProfile(Long childProfileId) {
        return childProfileRepository.findById(childProfileId)
            .orElseThrow(() -> new ResourceNotFoundException("Child profile not found: " + childProfileId));
    }

    private AvatarLifecycleResult createResult(Long sessionId, AvatarEventType type, byte[] audio) 
    {
        if(audio == null || audio.length <= 0)
            return createFallbackResult(sessionId, type);
        return new AvatarLifecycleResult(new GameAvatarEvent(null, sessionId, type.name(), true, UUID.randomUUID().toString(), ""), audio);
    }

    private AvatarLifecycleResult createFallbackResult(Long sessionId, AvatarEventType type) {
        return new AvatarLifecycleResult(new GameAvatarEvent(SessionEventType.GAME_AVATAR_EVENT, sessionId, type.name(), false, null, null), null);
    }
    
    
}
