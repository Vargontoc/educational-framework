package es.vargontoc.educational.framework.content.infrastructure.persistence;

import es.vargontoc.educational.framework.content.model.LearningPathStep;
import es.vargontoc.educational.framework.content.ports.out.LearningPathStepRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class LearningPathStepPersistenceAdapter implements LearningPathStepRepository {

    private final LearningPathStepJpaRepository jpaRepository;

    public LearningPathStepPersistenceAdapter(LearningPathStepJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<LearningPathStep> findById(Long id) {
        return jpaRepository.findById(id)
            .map(LearningPathStepPersistenceAdapter::toDomain);
    }

    @Override
    public List<LearningPathStep> findByLearningPathId(Long learningPathId) {
        return jpaRepository.findByLearningPathIdOrderByStepOrderAsc(learningPathId).stream()
            .map(LearningPathStepPersistenceAdapter::toDomain)
            .toList();
    }

    @Override
    public boolean existsByLearningPathIdAndStepOrder(Long learningPathId, Integer stepOrder) {
        return jpaRepository.existsByLearningPathIdAndStepOrder(learningPathId, stepOrder);
    }

    @Override
    public LearningPathStep save(LearningPathStep step) {
        var saved = jpaRepository.save(toJpa(step));
        return toDomain(saved);
    }

    private static LearningPathStep toDomain(LearningPathStepJpaEntity source) {
        var target = new LearningPathStep();
        target.setId(source.getId());
        target.setLearningPathId(source.getLearningPathId());
        target.setActivityId(source.getActivityId());
        target.setStepOrder(source.getStepOrder());
        target.setUnlockCondition(source.getUnlockCondition());
        target.setVisualMetadata(source.getVisualMetadata());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    private static LearningPathStepJpaEntity toJpa(LearningPathStep source) {
        var target = new LearningPathStepJpaEntity();
        target.setId(source.getId());
        target.setLearningPathId(source.getLearningPathId());
        target.setActivityId(source.getActivityId());
        target.setStepOrder(source.getStepOrder());
        target.setUnlockCondition(source.getUnlockCondition());
        target.setVisualMetadata(source.getVisualMetadata());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }
}
