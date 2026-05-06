package es.vargontoc.educational.framework.session.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FamilySessionJpaRepository extends JpaRepository<FamilySessionJpaEntity, Long> {

    Optional<FamilySessionJpaEntity> findByTokenHash(String tokenHash);

    List<FamilySessionJpaEntity> findByFamilyIdAndStatus(Long familyId, String status);

    @Modifying
    @Query("delete from FamilySessionJpaEntity session where session.status <> 'ACTIVE' and session.updatedAt < :cutoff")
    int deleteInactiveUpdatedBefore(LocalDateTime cutoff);
}
