package es.vargontoc.educational.framework.tracking.infrastructure.persistence;

import es.vargontoc.educational.framework.tracking.model.ChildLearningProgress;
import es.vargontoc.educational.framework.tracking.ports.out.ChildLearningProgressRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ChildLearningProgressPersistenceAdapter implements ChildLearningProgressRepository {

    private final ChildLearningProgressJpaRepository jpaRepository;

    public ChildLearningProgressPersistenceAdapter(ChildLearningProgressJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<ChildLearningProgress> findByChildProfileIdAndLearningPathId(
            Long childProfileId, Long learningPathId) {
        return jpaRepository.findByChildProfileIdAndLearningPathId(childProfileId, learningPathId)
                .map(ChildLearningProgressPersistenceAdapter::toDomain);
    }

    @Override
    public ChildLearningProgress save(ChildLearningProgress progress) {
        return toDomain(jpaRepository.save(toJpa(progress)));
    }

    static ChildLearningProgress toDomain(ChildLearningProgressJpaEntity source) {
        var target = new ChildLearningProgress();
        target.setId(source.getId());
        target.setChildProfileId(source.getChildProfileId());
        target.setLearningPathId(source.getLearningPathId());
        target.setCurrentLearningPathStepId(source.getCurrentLearningPathStepId());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    static ChildLearningProgressJpaEntity toJpa(ChildLearningProgress source) {
        var target = new ChildLearningProgressJpaEntity();
        target.setId(source.getId());
        target.setChildProfileId(source.getChildProfileId());
        target.setLearningPathId(source.getLearningPathId());
        target.setCurrentLearningPathStepId(source.getCurrentLearningPathStepId());
        return target;
    }
}
