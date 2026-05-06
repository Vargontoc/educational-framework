package es.vargontoc.educational.framework.session.ports.out;

import es.vargontoc.educational.framework.session.model.ChildSession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChildSessionRepository {

    Optional<ChildSession> findById(Long id);

    Optional<ChildSession> findActiveByChildProfileId(Long childProfileId);

    List<ChildSession> findActiveByFamilyId(Long familyId);

    List<ChildSession> findExpirableSessions(LocalDateTime cutoff);

    ChildSession save(ChildSession session);

    void saveAll(List<ChildSession> sessions);
}
