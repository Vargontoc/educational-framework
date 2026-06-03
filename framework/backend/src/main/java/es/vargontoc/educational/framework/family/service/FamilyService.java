package es.vargontoc.educational.framework.family.service;

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
        family.setTtsEnabled(ttsEnabled);
        family.setAgentEnabled(agentEnabled);
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
    public Family updateFamily(String name, String rawPin, boolean ttsEnabled, boolean agentEnabled) {
        var existing = familyRepository.findFamily()
            .orElseThrow(() -> new ResourceNotFoundException("Family not found"));

        familyValidator.validateForUpdate(name, rawPin);

        existing.setName(name);
        boolean pinChanged = rawPin != null && !rawPin.isBlank();
        if (pinChanged) {
            existing.setPinHash(passwordEncoder.encode(rawPin));
            revokeFamilySessions(existing.getId());
        }

        if (!ttsEnabled) {
            disableChildFlags(existing.getId(), true, false);
        }
        if (!agentEnabled) {
            disableChildFlags(existing.getId(), false, true);
        }

        existing.setTtsEnabled(ttsEnabled);
        existing.setAgentEnabled(agentEnabled);
        existing.setUpdatedAt(LocalDateTime.now());

        return familyRepository.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean familyExists() {
        return familyRepository.exists();
    }

    private void disableChildFlags(Long familyId, boolean disableTts, boolean disableAgent) {
        for (ChildProfile child : childProfileRepository.findAll()) {
            if (familyId != null && !familyId.equals(child.getFamilyId())) {
                continue;
            }
            boolean changed = false;
            if (disableTts && child.isTtsEnabled()) {
                child.setTtsEnabled(false);
                changed = true;
            }
            if (disableAgent && child.isAgentEnabled()) {
                child.setAgentEnabled(false);
                changed = true;
            }
            if (changed) {
                child.setUpdatedAt(LocalDateTime.now());
                childProfileRepository.save(child);
            }
        }

        if(disableTts || disableAgent) 
        {
            var childSessions = childSessionRepository.findActiveByFamilyId(familyId);
            if(!childSessions.isEmpty())
            {
                for (ChildSession childSession : childSessions) {

                    if(disableTts) {
                        sessionEventPublisher.notifyChild(childSession.getId(), SessionEvent.of(SessionEventType.CHILD_TTS_DEACTIVATED, childSession.getId()));
                    }

                    if(disableAgent) {
                        sessionEventPublisher.notifyChild(childSession.getId(), SessionEvent.of(SessionEventType.CHILD_AGENT_DEACTIVATED, childSession.getId()));
                    }
                }
            }  
        }
    }


    private void sendTTSConfigChangesForChilds(SessionEventType event)
    {
        
    }

    private void sendAgentChangesFroChilds(SessionEventType event) 
    {

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
