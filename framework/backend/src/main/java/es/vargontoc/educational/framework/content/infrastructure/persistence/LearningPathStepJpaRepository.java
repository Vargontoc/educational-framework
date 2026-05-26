package es.vargontoc.educational.framework.content.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LearningPathStepJpaRepository extends JpaRepository<LearningPathStepJpaEntity, Long> {

    List<LearningPathStepJpaEntity> findByLearningPathIdOrderByStepOrderAsc(Long learningPathId);

    boolean existsByLearningPathIdAndStepOrder(Long learningPathId, Integer stepOrder);
}
