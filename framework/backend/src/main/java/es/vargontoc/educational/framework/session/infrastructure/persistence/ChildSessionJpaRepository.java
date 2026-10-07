package es.vargontoc.educational.framework.session.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ChildSessionJpaRepository extends JpaRepository<ChildSessionJpaEntity, Long> {

    List<ChildSessionJpaEntity> findByChildProfileIdAndStatusOrderByStartedAtDesc(Long childProfileId, String status);

    List<ChildSessionJpaEntity> findByFamilyIdAndStatus(Long familyId, String status);

    List<ChildSessionJpaEntity> findByStatusAndLastActivityAtBefore(String status, LocalDateTime cutoff);

    @Modifying
    int deleteByEndedAtBeforeAndStatusNot(LocalDateTime cutoff, String status);

    @Modifying
    @Query("UPDATE ChildSessionJpaEntity e SET e.lastActivityAt = :lastActivityAt WHERE e.id = :id")
    int updateLastActivityAt(@Param("id") Long id, @Param("lastActivityAt") LocalDateTime lastActivityAt);
}
