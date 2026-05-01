package es.vargontoc.educational.framework.family.ports.out;

import es.vargontoc.educational.framework.family.model.Family;

import java.util.Optional;

public interface FamilyRepository {

    Optional<Family> findFamily();

    boolean exists();

    Family save(Family family);
}
