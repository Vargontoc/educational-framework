package es.vargontoc.educational.framework.tracking.infrastructure.persistence;

import es.vargontoc.educational.framework.tracking.model.ActivityAttempt;
import es.vargontoc.educational.framework.tracking.model.AttemptResult;
import es.vargontoc.educational.framework.tracking.ports.out.ActivityAttemptRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ActivityAttemptPersistenceAdapter implements ActivityAttemptRepository {

    private final ActivityAttemptJpaRepository jpaRepository;

    public ActivityAttemptPersistenceAdapter(ActivityAttemptJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ActivityAttempt save(ActivityAttempt attempt) {
        return toDomain(jpaRepository.save(toJpa(attempt)));
    }

    @Override
    public List<ActivityAttempt> findRecentByChildAndActivity(Long childProfileId, Long activityId, int limit) {
        return jpaRepository.findRecentByChildAndActivity(childProfileId, activityId, limit)
                .stream()
                .map(ActivityAttemptPersistenceAdapter::toDomain)
                .toList();
    }

    static ActivityAttempt toDomain(ActivityAttemptJpaEntity source) {
        var target = new ActivityAttempt();
        target.setId(source.getId());
        target.setChildProfileId(source.getChildProfileId());
        target.setActivityId(source.getActivityId());
        target.setChildSessionId(source.getChildSessionId());
        target.setTopicId(source.getTopicId());
        target.setDifficultyLevelId(source.getDifficultyLevelId());
        target.setResult(AttemptResult.valueOf(source.getResult()));
        target.setResponseTimeMs(source.getResponseTimeMs());
        target.setAttemptContext(source.getAttemptContext());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    static ActivityAttemptJpaEntity toJpa(ActivityAttempt source) {
        var target = new ActivityAttemptJpaEntity();
        target.setId(source.getId());
        target.setChildProfileId(source.getChildProfileId());
        target.setActivityId(source.getActivityId());
        target.setChildSessionId(source.getChildSessionId());
        target.setTopicId(source.getTopicId());
        target.setDifficultyLevelId(source.getDifficultyLevelId());
        target.setResult(source.getResult().name());
        target.setResponseTimeMs(source.getResponseTimeMs());
        target.setAttemptContext(source.getAttemptContext());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }
}
