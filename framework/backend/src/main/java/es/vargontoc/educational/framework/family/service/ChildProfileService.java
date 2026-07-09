package es.vargontoc.educational.framework.family.service;

import es.vargontoc.educational.framework.family.model.ChildProfile;
import es.vargontoc.educational.framework.family.model.ColorVisionMode;
import es.vargontoc.educational.framework.family.model.Family;
import es.vargontoc.educational.framework.family.ports.in.ChildProfileUseCase;
import es.vargontoc.educational.framework.family.ports.out.ChildProfileRepository;
import es.vargontoc.educational.framework.family.ports.out.FamilyRepository;
import es.vargontoc.educational.framework.family.validation.ChildProfileValidator;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import es.vargontoc.educational.framework.session.infrastructure.websocket.SessionEvent;
import es.vargontoc.educational.framework.session.infrastructure.websocket.SessionEventPublisher;
import es.vargontoc.educational.framework.session.infrastructure.websocket.SessionEventType;
import es.vargontoc.educational.framework.session.ports.in.ChildSessionUseCase;
import es.vargontoc.educational.framework.session.ports.out.ChildSessionRepository;

@Service
@Transactional
public class ChildProfileService implements ChildProfileUseCase {

    private static final String DEFAULT_CHILD_AVATAR = "default-child";

    private final FamilyRepository familyRepository;
    private final ChildSessionRepository childSessionRepository;
    private final SessionEventPublisher sessionEventPublisher;
    private final ChildProfileRepository childProfileRepository;
    private final ChildProfileValidator childProfileValidator;

    private final ChildSessionUseCase sessions;

    public ChildProfileService(FamilyRepository familyRepository, ChildSessionUseCase sessions, ChildSessionRepository childSessionRepository, SessionEventPublisher sessionEventPublisher, ChildProfileRepository childProfileRepository) {
        this.familyRepository = familyRepository;
        this.childProfileRepository = childProfileRepository;
        this.childSessionRepository = childSessionRepository;
        this.sessionEventPublisher = sessionEventPublisher;
        this.childProfileValidator = new ChildProfileValidator();
        this.sessions = sessions;
    }

    @Override
    public ChildProfile createChild(
        Long familyId,
        String name,
        LocalDate birthday,
        String avatar,
        boolean ttsEnabled,
        boolean agentEnabled,
        ColorVisionMode colorVisionMode
    ) {
        Family family = getFamilyOrThrow();
        childProfileValidator.validate(new ChildProfileValidator.ChildProfileValidationInput(name, birthday, avatar));

        var child = new ChildProfile();
        child.setFamilyId(familyId);
        child.setName(name);
        child.setBirthday(birthday);
        child.setAvatar(resolveAvatar(avatar));
        child.setActive(true);
        child.setTtsEnabled(applyFamilyCeiling(ttsEnabled, family.isTtsEnabled()));
        child.setAgentEnabled(applyFamilyCeiling(agentEnabled, family.isAgentEnabled()));
        child.setColorVisionMode(colorVisionMode != null ? colorVisionMode : ColorVisionMode.NONE);
        child.setCreatedAt(LocalDateTime.now());

        return childProfileRepository.save(child);
    }

    @Override
    @Transactional(readOnly = true)
    public ChildProfile getChild(Long id) {
        return childProfileRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Child profile not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChildProfile> getAllChildren() {
        return childProfileRepository.findAll();
    }

    @Override
    public ChildProfile updateChild(
        Long id,
        String name,
        LocalDate birthday,
        String avatar,
        boolean ttsEnabled,
        boolean agentEnabled,
        ColorVisionMode colorVisionMode
    ) {
        var child = childProfileRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Child profile not found"));
        Family family = getFamilyOrThrow();

        childProfileValidator.validate(new ChildProfileValidator.ChildProfileValidationInput(name, birthday, avatar));

        boolean stateTts = child.isTtsEnabled();
        boolean stateAgent = child.isAgentEnabled();

        child.setName(name);
        child.setBirthday(birthday);
        child.setAvatar(resolveAvatar(avatar));
        child.setTtsEnabled(applyFamilyCeiling(ttsEnabled, family.isTtsEnabled()));
        child.setAgentEnabled(applyFamilyCeiling(agentEnabled, family.isAgentEnabled()));
        if (colorVisionMode != null) {
            child.setColorVisionMode(colorVisionMode);
        }
        child.setUpdatedAt(LocalDateTime.now());

        try {
            var stored = childProfileRepository.save(child);
            boolean ttsChanges = stateTts != stored.isTtsEnabled();
            boolean agentChanges = stateAgent != stored.isAgentEnabled();
            
            if(ttsChanges || agentChanges)  
            {
                var session = childSessionRepository.findActiveByChildProfileId(stored.getId());
                if(session.isPresent()) 
                {
                    var s = session.get();
                    if(ttsChanges) 
                    {
                        sessionEventPublisher.notifyChild(s.getId(), 
                            SessionEvent.of(stored.isTtsEnabled() ? SessionEventType.CHILD_TTS_ACTIVATED : SessionEventType.CHILD_TTS_DEACTIVATED,
                             s.getId()));
                    }

                    if(agentChanges) 
                    {
                        sessionEventPublisher.notifyChild(s.getId(), 
                            SessionEvent.of(stored.isAgentEnabled() ? SessionEventType.CHILD_AGENT_ACTIVATED : SessionEventType.CHILD_AGENT_DEACTIVATED,
                             s.getId()));
                    }
                }
            }

            return stored;
        }catch(Exception e) {
            throw e;
        }
    }

    @Override
    public ChildProfile changeActiveState(Long id) {
        var child = childProfileRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Child profile not found"));
        child.setActive(!child.isActive());
        child.setUpdatedAt(LocalDateTime.now());
        childProfileRepository.save(child);

        if(!child.isActive()) {
            sessions.getActiveSessions(child.getFamilyId()).stream().filter(x -> x.getChildProfileId().equals(id)).toList().forEach(e -> sessions.expelChild(e.getId()));
        }

        return child;
    }
    
    @Override
    public void deleteChild(Long id) {
        var child = childProfileRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Child profile not found"));

        sessions.getActiveSessions(child.getFamilyId()).stream().filter(x -> x.getChildProfileId().equals(id)).toList().forEach(e -> sessions.closeSession(e.getId()));
        childProfileRepository.deleteById(id);
    }


    private Family getFamilyOrThrow() {
        return familyRepository.findFamily()
            .orElseThrow(() -> new ResourceNotFoundException("Family not found"));
    }

    private boolean applyFamilyCeiling(boolean childValue, boolean familyEnabled) {
        return familyEnabled && childValue;
    }

    private String resolveAvatar(String avatar) {
        return (avatar == null || avatar.isBlank()) ? DEFAULT_CHILD_AVATAR : avatar;
    }
}
