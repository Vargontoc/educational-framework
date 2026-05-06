package es.vargontoc.educational.framework.session.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChildSessionJpaRepository extends JpaRepository<ChildSessionJpaEntity, Long> {

    Optional<ChildSessionJpaEntity> findByChildProfileIdAndStatus(Long childProfileId, String status);

    List<ChildSessionJpaEntity> findByFamilyIdAndStatus(Long familyId, String status);

    List<ChildSessionJpaEntity> findByStatusAndLastActivityAtBefore(String status, LocalDateTime cutoff);

    @Modifying
    int deleteByEndedAtBeforeAndStatusNot(LocalDateTime cutoff, String status);
}
