package es.vargontoc.educational.framework.family.ports.out;

import es.vargontoc.educational.framework.family.model.ChildProfile;

import java.util.List;
import java.util.Optional;

public interface ChildProfileRepository {

    Optional<ChildProfile> findById(Long id);

    List<ChildProfile> findAll();

    ChildProfile save(ChildProfile child);

    void deleteById(Long id);
}
