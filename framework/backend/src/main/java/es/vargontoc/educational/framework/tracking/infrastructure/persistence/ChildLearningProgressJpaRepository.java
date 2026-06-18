package es.vargontoc.educational.framework.tracking.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChildLearningProgressJpaRepository extends JpaRepository<ChildLearningProgressJpaEntity, Long> {

    Optional<ChildLearningProgressJpaEntity> findByChildProfileIdAndLearningPathId(
            Long childProfileId, Long learningPathId);

    List<ChildLearningProgressJpaEntity> findByChildProfileId(Long childProfileId);
}
