package es.vargontoc.educational.framework.tracking.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TopicSummaryJpaRepository extends JpaRepository<TopicSummaryJpaEntity, Long> {

    Optional<TopicSummaryJpaEntity> findByChildProfileIdAndTopicId(Long childProfileId, Long topicId);

    List<TopicSummaryJpaEntity> findByChildProfileId(Long childProfileId);
}
