package es.vargontoc.educational.framework.tracking.infrastructure.persistence;

import es.vargontoc.educational.framework.tracking.model.DifficultyEvolution;
import es.vargontoc.educational.framework.tracking.ports.out.DifficultyEvolutionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DifficultyEvolutionPersistenceAdapter implements DifficultyEvolutionRepository {

    private final DifficultyEvolutionJpaRepository jpaRepository;

    public DifficultyEvolutionPersistenceAdapter(DifficultyEvolutionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    public List<DifficultyEvolution> findByChildProfileId(Long childProfileId) {
        return jpaRepository.findByChildProfileIdOrderByChangedAtDesc(childProfileId)
                .stream()
                .map(DifficultyEvolutionPersistenceAdapter::toDomain)
                .toList();
    }

    public List<DifficultyEvolution> findByChildProfileIdAndActivityId(Long childProfileId, Long activityId) {
        return jpaRepository.findByChildProfileIdAndActivityIdOrderByChangedAtDesc(childProfileId, activityId)
                .stream()
                .map(DifficultyEvolutionPersistenceAdapter::toDomain)
                .toList();
    }

    public Optional<DifficultyEvolution> findLatestByChildProfileIdAndActivityId(Long childProfileId, Long activityId) {
        return jpaRepository.findLatestByChildProfileIdAndActivityId(childProfileId, activityId)
                .map(DifficultyEvolutionPersistenceAdapter::toDomain);
    }

    static DifficultyEvolution toDomain(DifficultyEvolutionJpaEntity source) {
        var target = new DifficultyEvolution();
        target.setId(source.getId());
        target.setChildProfileId(source.getChildProfileId());
        target.setActivityId(source.getActivityId());
        target.setDifficultyLevelId(source.getDifficultyLevelId());
        target.setDirection(source.getDirection());
        target.setChangedAt(source.getChangedAt());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }
}
