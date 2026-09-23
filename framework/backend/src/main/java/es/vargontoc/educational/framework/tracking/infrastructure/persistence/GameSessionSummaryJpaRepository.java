package es.vargontoc.educational.framework.tracking.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface GameSessionSummaryJpaRepository extends JpaRepository<GameSessionSummaryJpaEntity, Long> {

    List<GameSessionSummaryJpaEntity> findByChildProfileId(Long childProfileId);

    List<GameSessionSummaryJpaEntity> findByChildProfileIdAndStartedAtBetween(Long childProfileId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT g FROM GameSessionSummaryJpaEntity g WHERE g.childProfileId = :childProfileId AND g.activityId = :activityId AND g.finalStatus = 'ABANDONED' AND g.abandonReason = 'CLIENT_REQUESTED' ORDER BY g.startedAt DESC LIMIT :limit")
    List<GameSessionSummaryJpaEntity> findRecentInitialAbandonmentsByChildAndActivity(
            @Param("childProfileId") Long childProfileId,
            @Param("activityId") Long activityId,
            @Param("limit") int limit);
}