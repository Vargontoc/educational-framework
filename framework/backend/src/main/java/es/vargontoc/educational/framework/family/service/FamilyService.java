package es.vargontoc.educational.framework.family.service;

import es.vargontoc.educational.framework.family.infrastructure.dto.UpdateFamilyRequest;
import es.vargontoc.educational.framework.family.model.ChildProfile;
import es.vargontoc.educational.framework.family.model.Family;
import es.vargontoc.educational.framework.family.ports.in.FamilyUseCase;
import es.vargontoc.educational.framework.family.ports.out.ChildProfileRepository;
import es.vargontoc.educational.framework.family.ports.out.FamilyRepository;
import es.vargontoc.educational.framework.family.validation.FamilyValidator;
import es.vargontoc.educational.framework.session.model.ChildSession;
import es.vargontoc.educational.framework.session.model.FamilySession;
import es.vargontoc.educational.framework.session.model.FamilySessionStatus;
import es.vargontoc.educational.framework.session.ports.out.FamilySessionRepository;
import es.vargontoc.educational.framework.session.infrastructure.websocket.SessionEvent;
import es.vargontoc.educational.framework.session.infrastructure.websocket.SessionEventPublisher;
import es.vargontoc.educational.framework.session.infrastructure.websocket.SessionEventType;
import es.vargontoc.educational.framework.shared.exception.ConflictException;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import es.vargontoc.educational.framework.session.ports.out.ChildSessionRepository;

@Service
@Transactional
public class FamilyService implements FamilyUseCase {

    private final FamilyRepository familyRepository;
    private final ChildProfileRepository childProfileRepository;
    private final ChildSessionRepository childSessionRepository;
    private final FamilySessionRepository familySessionRepository;
    private final SessionEventPublisher sessionEventPublisher;
    private final FamilyValidator familyValidator;
    private final BCryptPasswordEncoder passwordEncoder;

    public FamilyService(
        FamilyRepository familyRepository,
        ChildProfileRepository childProfileRepository,
        ChildSessionRepository childSessionRepository,
        FamilySessionRepository familySessionRepository,
        SessionEventPublisher sessionEventPublisher
    ) {
        this.familyRepository = familyRepository;
        this.childProfileRepository = childProfileRepository;
        this.familySessionRepository = familySessionRepository;
        this.sessionEventPublisher = sessionEventPublisher;
        this.childSessionRepository = childSessionRepository;
        this.familyValidator = new FamilyValidator();
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public Family createFamily(String name, String rawPin, boolean ttsEnabled, boolean agentEnabled) {
        if (familyRepository.exists()) {
            throw new ConflictException("Family already exists");
        }
        familyValidator.validateForCreate(name, rawPin);

        var family = new Family();
        family.setName(name);
        family.setPinHash(passwordEncoder.encode(rawPin));
        family.setCreatedAt(LocalDateTime.now());

        return familyRepository.save(family);
    }

    @Override
    @Transactional(readOnly = true)
    public Family getFamily() {
        return familyRepository.findFamily()
            .orElseThrow(() -> new ResourceNotFoundException("Family not found"));
    }

    @Override
    public Family updateFamily(UpdateFamilyRequest request) {
        var existing = familyRepository.findFamily()
            .orElseThrow(() -> new ResourceNotFoundException("Family not found"));

        // Validate name and pin if provided
        familyValidator.validateForUpdate(request.name(), request.pin());

        // Update name if provided
        if (request.name() != null) {
            existing.setName(request.name());
        }

        // Update pin if provided and non-blank
        boolean pinChanged = request.pin() != null && !request.pin().isBlank();
        if (pinChanged) {
            existing.setPinHash(passwordEncoder.encode(request.pin()));
            revokeFamilySessions(existing.getId());
        }

        // Update global config fields (only if provided)
        if (request.audioGeneralEnabled() != null) {
            existing.setAudioGeneralEnabled(request.audioGeneralEnabled());
        }
        if (request.audioGeneralVolume() != null) {
            existing.setAudioGeneralVolume(clampVolume(request.audioGeneralVolume()));
        }
        if (request.npcEnabled() != null) {
            existing.setNpcEnabled(request.npcEnabled());
        }
        if (request.npcVoiceEnabled() != null) {
            existing.setNpcVoiceEnabled(request.npcVoiceEnabled());
        }
        if (request.npcVoiceVolume() != null) {
            existing.setNpcVoiceVolume(clampVolume(request.npcVoiceVolume()));
        }
        if (request.narrativeVoiceEnabled() != null) {
            existing.setNarrativeVoiceEnabled(request.narrativeVoiceEnabled());
        }
        if (request.narrativeVoiceVolume() != null) {
            existing.setNarrativeVoiceVolume(clampVolume(request.narrativeVoiceVolume()));
        }

        // NPC Voice deactive if NPC is deactive
        if(!existing.isNpcEnabled())
        {
            existing.setNpcVoiceEnabled(false);
            disableChildFlags(existing.getId(), true, true);
        }else if(!existing.isNpcVoiceEnabled())
        {
            disableChildFlags(existing.getId(), true, false);
        }

        



        existing.setUpdatedAt(LocalDateTime.now());

        return familyRepository.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean familyExists() {
        return familyRepository.exists();
    }

    static int clampVolume(int value) {
        if (value <= 0) return 0;
        if (value >= 100) return 100;
        return value;
    }

    private void disableChildFlags(Long familyId, boolean disableNpcVoice, boolean disableNpc) {
        for (ChildProfile child : childProfileRepository.findAll()) {
            if (familyId != null && !familyId.equals(child.getFamilyId())) {
                continue;
            }
            boolean changed = false;
            if (disableNpcVoice && child.isNpcVoiceEnabled()) {
                child.setNpcVoiceEnabled(false);
                changed = true;
            }
            if (disableNpc && child.isNpcEnabled()) {
                child.setNpcEnabled(false);
                changed = true;
            }
            if (changed) {
                child.setUpdatedAt(LocalDateTime.now());
                childProfileRepository.save(child);
            }
        }

        if(disableNpcVoice || disableNpc) 
        {
            var childSessions = childSessionRepository.findActiveByFamilyId(familyId);
            if(!childSessions.isEmpty())
            {
                for (ChildSession childSession : childSessions) {

                    if(disableNpcVoice) {
                        sessionEventPublisher.notifyChild(childSession.getId(), SessionEvent.of(SessionEventType.CHILD_NPC_VOICE_DEACTIVATED, childSession.getId()));
                    }

                    if(disableNpc) {
                        sessionEventPublisher.notifyChild(childSession.getId(), SessionEvent.of(SessionEventType.CHILD_NPC_DEACTIVATED, childSession.getId()));
                    }
                }
            }
        }
    }

    private void revokeFamilySessions(Long familyId) {
        var activeSessions = familySessionRepository.findActiveByFamilyId(familyId);
        if (activeSessions.isEmpty()) {
            return;
        }

        var now = LocalDateTime.now();
        for (FamilySession session : activeSessions) {
            session.setStatus(FamilySessionStatus.REVOKED);
            session.setRevoked(true);
            session.setUpdatedAt(now);
        }
        familySessionRepository.saveAll(activeSessions);

        for (FamilySession session : activeSessions) {
            sessionEventPublisher.notifyParent(
                familyId,
                SessionEvent.of(SessionEventType.SESSION_INVALIDATED, session.getId())
            );
        }
    }
}
