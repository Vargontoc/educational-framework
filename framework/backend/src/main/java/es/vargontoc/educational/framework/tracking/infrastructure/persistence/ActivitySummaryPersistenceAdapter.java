package es.vargontoc.educational.framework.tracking.infrastructure.persistence;

import es.vargontoc.educational.framework.tracking.model.ActivitySummary;
import es.vargontoc.educational.framework.tracking.ports.out.ActivitySummaryRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ActivitySummaryPersistenceAdapter implements ActivitySummaryRepository {

    private final ActivitySummaryJpaRepository jpaRepository;

    public ActivitySummaryPersistenceAdapter(ActivitySummaryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<ActivitySummary> findByChildProfileIdAndActivityId(Long childProfileId, Long activityId) {
        return jpaRepository.findByChildProfileIdAndActivityId(childProfileId, activityId)
            .map(ActivitySummaryPersistenceAdapter::toDomain);
    }

    @Override
    public List<ActivitySummary> findByChildProfileId(Long childProfileId) {
        return jpaRepository.findByChildProfileId(childProfileId)
                .stream()
                .map(ActivitySummaryPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public ActivitySummary save(ActivitySummary summary) {
        return toDomain(jpaRepository.save(toJpa(summary)));
    }

    static ActivitySummary toDomain(ActivitySummaryJpaEntity source) {
        var target = new ActivitySummary();
        target.setId(source.getId());
        target.setChildProfileId(source.getChildProfileId());
        target.setActivityId(source.getActivityId());
        target.setTotalAttempts(source.getTotalAttempts());
        target.setTotalCorrect(source.getTotalCorrect());
        target.setTotalIncorrect(source.getTotalIncorrect());
        target.setTotalTimeouts(source.getTotalTimeouts());
        target.setSuccessRatePercent(source.getSuccessRatePercent());
        target.setAverageResponseTimeMs(source.getAverageResponseTimeMs());
        target.setCurrentDifficultyLevelId(source.getCurrentDifficultyLevelId());
        target.setAttemptsSinceLastDifficultyChange(source.getAttemptsSinceLastDifficultyChange());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    static ActivitySummaryJpaEntity toJpa(ActivitySummary source) {
        var target = new ActivitySummaryJpaEntity();
        target.setId(source.getId());
        target.setChildProfileId(source.getChildProfileId());
        target.setActivityId(source.getActivityId());
        target.setTotalAttempts(source.getTotalAttempts());
        target.setTotalCorrect(source.getTotalCorrect());
        target.setTotalIncorrect(source.getTotalIncorrect());
        target.setTotalTimeouts(source.getTotalTimeouts());
        target.setSuccessRatePercent(source.getSuccessRatePercent());
        target.setAverageResponseTimeMs(source.getAverageResponseTimeMs());
        target.setCurrentDifficultyLevelId(source.getCurrentDifficultyLevelId());
        target.setAttemptsSinceLastDifficultyChange(source.getAttemptsSinceLastDifficultyChange());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }
}
