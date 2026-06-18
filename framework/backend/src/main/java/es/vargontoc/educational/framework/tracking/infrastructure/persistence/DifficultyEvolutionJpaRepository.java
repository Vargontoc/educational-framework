package es.vargontoc.educational.framework.tracking.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DifficultyEvolutionJpaRepository extends JpaRepository<DifficultyEvolutionJpaEntity, Long> {

    List<DifficultyEvolutionJpaEntity> findByChildProfileIdOrderByChangedAtDesc(Long childProfileId);

    @Query("SELECT e FROM DifficultyEvolutionJpaEntity e " +
           "WHERE e.childProfileId = :childProfileId AND e.activityId = :activityId " +
           "ORDER BY e.changedAt DESC")
    List<DifficultyEvolutionJpaEntity> findByChildProfileIdAndActivityIdOrderByChangedAtDesc(
            @Param("childProfileId") Long childProfileId,
            @Param("activityId") Long activityId);

    @Query("SELECT e FROM DifficultyEvolutionJpaEntity e " +
           "WHERE e.childProfileId = :childProfileId AND e.activityId = :activityId " +
           "ORDER BY e.changedAt DESC LIMIT 1")
    Optional<DifficultyEvolutionJpaEntity> findLatestByChildProfileIdAndActivityId(
            @Param("childProfileId") Long childProfileId,
            @Param("activityId") Long activityId);
}
