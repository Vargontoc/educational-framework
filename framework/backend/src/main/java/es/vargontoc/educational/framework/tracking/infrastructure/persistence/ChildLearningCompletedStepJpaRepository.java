package es.vargontoc.educational.framework.tracking.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChildLearningCompletedStepJpaRepository extends JpaRepository<ChildLearningCompletedStepJpaEntity, Long> {

    List<ChildLearningCompletedStepJpaEntity> findByChildProfileIdAndLearningPathId(
            Long childProfileId, Long learningPathId);

    List<ChildLearningCompletedStepJpaEntity> findByChildProfileId(Long childProfileId);

    @Query("SELECT e FROM ChildLearningCompletedStepJpaEntity e " +
           "WHERE e.childProfileId = :childProfileId " +
           "AND e.learningPathId = :learningPathId " +
           "AND e.learningPathStepId = :stepId")
    Optional<ChildLearningCompletedStepJpaEntity> findByChildProfileIdAndLearningPathIdAndStepId(
            @Param("childProfileId") Long childProfileId,
            @Param("learningPathId") Long learningPathId,
            @Param("stepId") Long stepId);
}
