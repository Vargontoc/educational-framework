package es.vargontoc.educational.framework.content.ports.out;

import es.vargontoc.educational.framework.content.model.DifficultyLevel;

import java.util.List;
import java.util.Optional;

public interface DifficultyLevelRepository {

    Optional<DifficultyLevel> findById(Long id);

    Optional<DifficultyLevel> findByIdAndEngineParamsIsNotNull(Long id);

    List<DifficultyLevel> findByActivityId(Long activityId);

    Optional<DifficultyLevel> findFirstByActivityIdOrderByDifficultyCodeAsc(Long activityId);

    DifficultyLevel save(DifficultyLevel difficultyLevel);
}
