package es.vargontoc.educational.framework.tracking.ports.out;

import es.vargontoc.educational.framework.tracking.model.DifficultyEvolution;

import java.util.List;
import java.util.Optional;

public interface DifficultyEvolutionRepository {

    List<DifficultyEvolution> findByChildProfileId(Long childProfileId);

    List<DifficultyEvolution> findByChildProfileIdAndActivityId(Long childProfileId, Long activityId);

    Optional<DifficultyEvolution> findLatestByChildProfileIdAndActivityId(Long childProfileId, Long activityId);
}
