package es.vargontoc.educational.framework.family.ports.in;

import es.vargontoc.educational.framework.family.model.AdultProfile;

import java.time.LocalDate;
import java.util.List;

public interface AdultProfileUseCase {

    AdultProfile createAdult(Long familyId, String name, LocalDate birthday, String avatar);

    AdultProfile getAdult(Long id);

    List<AdultProfile> getAllAdults();

    AdultProfile updateAdult(Long id, String name, LocalDate birthday, String avatar);

    void deleteAdult(Long id);
}
