package es.vargontoc.educational.framework.content.ports.out;

import es.vargontoc.educational.framework.content.model.AccessibleColor;

import java.util.List;
import java.util.Optional;

public interface AccessibleColorRepository {

    Optional<AccessibleColor> findById(Long id);

    List<AccessibleColor> findAll();

    List<AccessibleColor> findByStatus(String status);

    AccessibleColor save(AccessibleColor accessibleColor);

    boolean existsByConceptualIdentity(String conceptualIdentity);
}
