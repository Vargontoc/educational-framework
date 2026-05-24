package es.vargontoc.educational.framework.content.infrastructure.persistence;

import es.vargontoc.educational.framework.content.model.Activity;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.ports.out.ActivityRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class ActivityPersistenceAdapter implements ActivityRepository {

    private final ActivityJpaRepository jpaRepository;
    private final ActivityTopicJpaRepository activityTopicJpaRepository;

    public ActivityPersistenceAdapter(
        ActivityJpaRepository jpaRepository,
        ActivityTopicJpaRepository activityTopicJpaRepository
    ) {
        this.jpaRepository = jpaRepository;
        this.activityTopicJpaRepository = activityTopicJpaRepository;
    }

    @Override
    public Optional<Activity> findById(Long id) {
        return jpaRepository.findById(id)
            .map(entity -> toDomain(entity, getActivityTopicIds(entity.getId())));
    }

    @Override
    public List<Activity> findAll() {
        return jpaRepository.findAll().stream()
            .map(entity -> toDomain(entity, getActivityTopicIds(entity.getId())))
            .toList();
    }

    @Override
    public List<Activity> findByTopicId(Long topicId) {
        var activityIds = activityTopicJpaRepository.findByTopicId(topicId).stream()
            .map(ActivityTopicJpaEntity::getActivityId)
            .toList();

        return jpaRepository.findAllById(activityIds).stream()
            .map(entity -> toDomain(entity, getActivityTopicIds(entity.getId())))
            .toList();
    }

    @Override
    @Transactional
    public Activity save(Activity activity) {
        var saved = jpaRepository.save(toJpa(activity));

        activityTopicJpaRepository.deleteByActivityId(saved.getId());

        if (activity.getTopicIds() != null) {
            for (Long topicId : activity.getTopicIds()) {
                var activityTopic = new ActivityTopicJpaEntity();
                activityTopic.setActivityId(saved.getId());
                activityTopic.setTopicId(topicId);
                activityTopicJpaRepository.save(activityTopic);
            }
        }

        return toDomain(saved, activity.getTopicIds());
    }

    private List<Long> getActivityTopicIds(Long activityId) {
        return activityTopicJpaRepository.findByActivityId(activityId).stream()
            .map(ActivityTopicJpaEntity::getTopicId)
            .toList();
    }

    private static Activity toDomain(ActivityJpaEntity source, List<Long> topicIds) {
        var target = new Activity();
        target.setId(source.getId());
        target.setName(source.getName());
        target.setDescription(source.getDescription());
        target.setGameEngineType(source.getGameEngineType());
        target.setMinAge(source.getMinAge());
        target.setMaxAge(source.getMaxAge());
        target.setStatus(ContentStatus.valueOf(source.getStatus()));
        target.setTopicIds(topicIds);
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    private static ActivityJpaEntity toJpa(Activity source) {
        var target = new ActivityJpaEntity();
        target.setId(source.getId());
        target.setName(source.getName());
        target.setDescription(source.getDescription());
        target.setGameEngineType(source.getGameEngineType());
        target.setMinAge(source.getMinAge());
        target.setMaxAge(source.getMaxAge());
        target.setStatus(source.getStatus().name());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }
}
