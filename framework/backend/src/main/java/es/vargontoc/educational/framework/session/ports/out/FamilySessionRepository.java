package es.vargontoc.educational.framework.session.ports.out;

import es.vargontoc.educational.framework.session.model.FamilySession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FamilySessionRepository {

    Optional<FamilySession> findByTokenHash(String tokenHash);

    List<FamilySession> findActiveByFamilyId(Long familyId);

    FamilySession save(FamilySession session);

    void saveAll(List<FamilySession> sessions);

    int deleteInactiveUpdatedBefore(LocalDateTime cutoff);
}
