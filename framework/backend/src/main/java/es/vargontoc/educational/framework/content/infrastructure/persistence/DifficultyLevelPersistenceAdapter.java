package es.vargontoc.educational.framework.content.infrastructure.persistence;

import es.vargontoc.educational.framework.content.model.DifficultyCode;
import es.vargontoc.educational.framework.content.model.DifficultyLevel;
import es.vargontoc.educational.framework.content.ports.out.DifficultyLevelRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DifficultyLevelPersistenceAdapter implements DifficultyLevelRepository {

    private final DifficultyLevelJpaRepository jpaRepository;

    public DifficultyLevelPersistenceAdapter(DifficultyLevelJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<DifficultyLevel> findById(Long id) {
        return jpaRepository.findById(id)
            .map(DifficultyLevelPersistenceAdapter::toDomain);
    }

    @Override
    public List<DifficultyLevel> findByActivityId(Long activityId) {
        return jpaRepository.findByActivityId(activityId).stream()
            .map(DifficultyLevelPersistenceAdapter::toDomain)
            .toList();
    }

    @Override
    public DifficultyLevel save(DifficultyLevel difficultyLevel) {
        var saved = jpaRepository.save(toJpa(difficultyLevel));
        return toDomain(saved);
    }

    private static DifficultyLevel toDomain(DifficultyLevelJpaEntity source) {
        var target = new DifficultyLevel();
        target.setId(source.getId());
        target.setActivityId(source.getActivityId());
        target.setDifficultyCode(DifficultyCode.valueOf(source.getDifficultyCode()));
        target.setEngineParams(source.getEngineParams());
        target.setAdaptiveThresholdConfig(source.getAdaptiveThresholdConfig());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    private static DifficultyLevelJpaEntity toJpa(DifficultyLevel source) {
        var target = new DifficultyLevelJpaEntity();
        target.setId(source.getId());
        target.setActivityId(source.getActivityId());
        target.setDifficultyCode(source.getDifficultyCode().name());
        target.setEngineParams(source.getEngineParams());
        target.setAdaptiveThresholdConfig(source.getAdaptiveThresholdConfig());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }
}
