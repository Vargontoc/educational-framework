package es.vargontoc.educational.framework.family.ports.out;

import es.vargontoc.educational.framework.family.model.AdultProfile;

import java.util.List;
import java.util.Optional;

public interface AdultProfileRepository {

    Optional<AdultProfile> findById(Long id);

    List<AdultProfile> findAll();

    AdultProfile save(AdultProfile adult);

    void deleteById(Long id);
}
