package es.vargontoc.educational.framework.content.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityTopicJpaRepository extends JpaRepository<ActivityTopicJpaEntity, ActivityTopicJpaEntity.ActivityTopicId> {

    List<ActivityTopicJpaEntity> findByActivityId(Long activityId);

    List<ActivityTopicJpaEntity> findByTopicId(Long topicId);

    void deleteByActivityId(Long activityId);
}
