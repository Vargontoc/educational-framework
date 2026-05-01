package es.vargontoc.educational.framework.family.service;

import es.vargontoc.educational.framework.family.model.ChildProfile;
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

@Service
@Transactional
public class ChildProfileService implements ChildProfileUseCase {

    private static final String DEFAULT_CHILD_AVATAR = "default-child";

    private final FamilyRepository familyRepository;
    private final ChildProfileRepository childProfileRepository;
    private final ChildProfileValidator childProfileValidator;

    public ChildProfileService(FamilyRepository familyRepository, ChildProfileRepository childProfileRepository) {
        this.familyRepository = familyRepository;
        this.childProfileRepository = childProfileRepository;
        this.childProfileValidator = new ChildProfileValidator();
    }

    @Override
    public ChildProfile createChild(
        Long familyId,
        String name,
        LocalDate birthday,
        String avatar,
        boolean ttsEnabled,
        boolean agentEnabled
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
        boolean agentEnabled
    ) {
        var child = childProfileRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Child profile not found"));
        Family family = getFamilyOrThrow();

        childProfileValidator.validate(new ChildProfileValidator.ChildProfileValidationInput(name, birthday, avatar));

        child.setName(name);
        child.setBirthday(birthday);
        child.setAvatar(resolveAvatar(avatar));
        child.setTtsEnabled(applyFamilyCeiling(ttsEnabled, family.isTtsEnabled()));
        child.setAgentEnabled(applyFamilyCeiling(agentEnabled, family.isAgentEnabled()));
        child.setUpdatedAt(LocalDateTime.now());

        return childProfileRepository.save(child);
    }

    @Override
    public void deactivateChild(Long id) {
        var child = childProfileRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Child profile not found"));
        child.setActive(false);
        child.setUpdatedAt(LocalDateTime.now());
        childProfileRepository.save(child);
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
