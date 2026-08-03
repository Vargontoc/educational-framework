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

import es.vargontoc.educational.framework.family.infrastructure.dto.UpdateChildProfileRequest;
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
        boolean npcVoiceEnabled,
        boolean npcEnabled,
        int npcVoiceVolume,
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
        int clampedVolume = Math.max(0, Math.min(100, npcVoiceVolume));
        boolean resolvedNpcVoice = applyFamilyCeiling(npcVoiceEnabled, family.isNpcVoiceEnabled());
        child.setNpcVoiceEnabled(resolvedNpcVoice);
        child.setNpcEnabled(applyFamilyCeiling(npcEnabled, family.isNpcEnabled()));
        child.setNpcVoiceVolume(resolvedNpcVoice ? Math.min(clampedVolume, family.getNpcVoiceVolume()) : 0);
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
        UpdateChildProfileRequest request
    ) {
        var child = childProfileRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Child profile not found"));
        
        // Get family for global settings
        Family family = getFamilyOrThrow();

        // Validate request 
        childProfileValidator.validateForUpdate(request);

        // Get global settings for npc - agent
        boolean npcState = family.isNpcEnabled();
        boolean npcVoiceState = family.isNpcVoiceEnabled();

        if(request.name() != null)
            child.setName(request.name());
        if(request.birthday() != null)
            child.setBirthday(request.birthday());
        if(request.avatar() != null)
            child.setAvatar(request.avatar());
        if(request.colorVisionMode() != null)
            child.setColorVisionMode(request.colorVisionMode());
        if(request.npcVoiceVolume() != null)
        {
            child.setNpcVoiceVolume(Math.max(0, Math.min(100, request.npcVoiceVolume())));
        }

        // Get current NPC State for child
        boolean currentNpc = child.isNpcEnabled();
        boolean currentVoiceNpc = child.isNpcVoiceEnabled();
        int currentVolumeNpc = child.getNpcVoiceVolume();

        // Get forward NPC State for child
        boolean forwardNpc = request.npcEnabled() != null ? request.npcEnabled() : currentNpc;
        boolean forwardVoiceNpc = request.npcVoiceEnabled() != null ? request.npcVoiceEnabled() : currentVoiceNpc;
        
        // NPC disabled familiar level
        if(!npcState)
        {
            child.setNpcEnabled(false);
            child.setNpcVoiceEnabled(false);
        // NPC enable familiar level but voice
        }else
        {
            child.setNpcEnabled(forwardNpc);
            if(!npcVoiceState) {
                child.setNpcVoiceEnabled(false);
            }else
            {
                child.setNpcVoiceEnabled(!forwardNpc ? false : forwardVoiceNpc);
            }
        }
        child.setUpdatedAt(LocalDateTime.now());

        try {
            var stored = childProfileRepository.save(child);
            boolean npcVoiceChanges = currentNpc != forwardNpc;
            boolean agentChanges = currentVoiceNpc != forwardVoiceNpc;
            boolean volumeChanges = stored.getNpcVoiceVolume() != currentVolumeNpc;
            if(npcVoiceChanges || agentChanges || volumeChanges)
            {
                var session = childSessionRepository.findActiveByChildProfileId(stored.getId());
                if(session.isPresent())
                {
                    var s = session.get();
                    if(npcVoiceChanges)
                    {
                        sessionEventPublisher.notifyChild(s.getId(),
                            SessionEvent.of(stored.isNpcVoiceEnabled() ? SessionEventType.CHILD_NPC_VOICE_ACTIVATED : SessionEventType.CHILD_NPC_VOICE_DEACTIVATED, s.getId()));
                    }

                    if(agentChanges)
                    {
                        sessionEventPublisher.notifyChild(s.getId(), 
                            SessionEvent.of(stored.isNpcEnabled() ? SessionEventType.CHILD_NPC_ACTIVATED : SessionEventType.CHILD_NPC_DEACTIVATED, s.getId()));
                    }

                    if(volumeChanges)
                    {
                        sessionEventPublisher.notifyChild(s.getId(), 
                            SessionEvent.of(SessionEventType.CHILD_NPC_VOICE_VOLUME_CHANGED,s.getId()));
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
