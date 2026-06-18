package es.vargontoc.educational.framework.tracking.infrastructure.persistence;

import es.vargontoc.educational.framework.tracking.model.ChildLearningCompletedStep;
import es.vargontoc.educational.framework.tracking.ports.out.ChildLearningCompletedStepRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ChildLearningCompletedStepPersistenceAdapter implements ChildLearningCompletedStepRepository {

    private final ChildLearningCompletedStepJpaRepository jpaRepository;

    public ChildLearningCompletedStepPersistenceAdapter(ChildLearningCompletedStepJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<ChildLearningCompletedStep> findByChildProfileIdAndLearningPathId(
            Long childProfileId, Long learningPathId) {
        return jpaRepository.findByChildProfileIdAndLearningPathId(childProfileId, learningPathId)
                .stream()
                .map(ChildLearningCompletedStepPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public List<ChildLearningCompletedStep> findByChildProfileId(Long childProfileId) {
        return jpaRepository.findByChildProfileId(childProfileId)
                .stream()
                .map(ChildLearningCompletedStepPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public Optional<ChildLearningCompletedStep> findByChildProfileIdAndLearningPathIdAndStepId(
            Long childProfileId, Long learningPathId, Long stepId) {
        return jpaRepository.findByChildProfileIdAndLearningPathIdAndStepId(childProfileId, learningPathId, stepId)
                .map(ChildLearningCompletedStepPersistenceAdapter::toDomain);
    }

    @Override
    public ChildLearningCompletedStep save(ChildLearningCompletedStep step) {
        return toDomain(jpaRepository.save(toJpa(step)));
    }

    static ChildLearningCompletedStep toDomain(ChildLearningCompletedStepJpaEntity source) {
        var target = new ChildLearningCompletedStep();
        target.setId(source.getId());
        target.setChildProfileId(source.getChildProfileId());
        target.setLearningPathId(source.getLearningPathId());
        target.setLearningPathStepId(source.getLearningPathStepId());
        target.setCompletedAt(source.getCompletedAt());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    static ChildLearningCompletedStepJpaEntity toJpa(ChildLearningCompletedStep source) {
        var target = new ChildLearningCompletedStepJpaEntity();
        target.setId(source.getId());
        target.setChildProfileId(source.getChildProfileId());
        target.setLearningPathId(source.getLearningPathId());
        target.setLearningPathStepId(source.getLearningPathStepId());
        target.setCompletedAt(source.getCompletedAt());
        return target;
    }
}
