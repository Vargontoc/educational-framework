package es.vargontoc.educational.framework.family.service;

import es.vargontoc.educational.framework.family.model.AdultProfile;
import es.vargontoc.educational.framework.family.ports.in.AdultProfileUseCase;
import es.vargontoc.educational.framework.family.ports.out.AdultProfileRepository;
import es.vargontoc.educational.framework.family.validation.AdultProfileValidator;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AdultProfileService implements AdultProfileUseCase {

    private static final String DEFAULT_ADULT_AVATAR = "default-adult";

    private final AdultProfileRepository adultProfileRepository;
    private final AdultProfileValidator adultProfileValidator;

    public AdultProfileService(AdultProfileRepository adultProfileRepository) {
        this.adultProfileRepository = adultProfileRepository;
        this.adultProfileValidator = new AdultProfileValidator();
    }

    @Override
    public AdultProfile createAdult(Long familyId, String name, LocalDate birthday, String avatar) {
        adultProfileValidator.validate(new AdultProfileValidator.AdultProfileValidationInput(name, birthday, avatar));

        var adult = new AdultProfile();
        adult.setFamilyId(familyId);
        adult.setName(name);
        adult.setBirthday(birthday);
        adult.setAvatar(resolveAvatar(avatar));
        adult.setCreatedAt(LocalDateTime.now());
        return adultProfileRepository.save(adult);
    }

    @Override
    @Transactional(readOnly = true)
    public AdultProfile getAdult(Long id) {
        return adultProfileRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Adult profile not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdultProfile> getAllAdults() {
        return adultProfileRepository.findAll();
    }

    @Override
    public AdultProfile updateAdult(Long id, String name, LocalDate birthday, String avatar) {
        var adult = adultProfileRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Adult profile not found"));

        adultProfileValidator.validate(new AdultProfileValidator.AdultProfileValidationInput(name, birthday, avatar));
        adult.setName(name);
        adult.setBirthday(birthday);
        adult.setAvatar(resolveAvatar(avatar));
        adult.setUpdatedAt(LocalDateTime.now());

        return adultProfileRepository.save(adult);
    }

    @Override
    public void deleteAdult(Long id) {
        adultProfileRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Adult profile not found"));
        adultProfileRepository.deleteById(id);
    }

    private String resolveAvatar(String avatar) {
        return (avatar == null || avatar.isBlank()) ? DEFAULT_ADULT_AVATAR : avatar;
    }
}
