package es.vargontoc.educational.framework.content.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DifficultyLevelJpaRepository extends JpaRepository<DifficultyLevelJpaEntity, Long> {

    List<DifficultyLevelJpaEntity> findByActivityId(Long activityId);

    Optional<DifficultyLevelJpaEntity> findByIdAndEngineParamsIsNotNull(Long id);

    Optional<DifficultyLevelJpaEntity> findFirstByActivityIdOrderByDifficultyCodeAsc(Long activityId);
}
