package es.vargontoc.educational.framework.tracking.infrastructure.persistence;

import es.vargontoc.educational.framework.tracking.model.ChildAchievement;
import es.vargontoc.educational.framework.tracking.ports.out.ChildAchievementRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ChildAchievementPersistenceAdapter implements ChildAchievementRepository {

    private final ChildAchievementJpaRepository jpaRepository;

    public ChildAchievementPersistenceAdapter(ChildAchievementJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<ChildAchievement> findByChildProfileId(Long childProfileId) {
        return jpaRepository.findByChildProfileId(childProfileId)
                .stream()
                .map(ChildAchievementPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public List<ChildAchievement> findByChildProfileIdAndActivityId(Long childProfileId, Long activityId) {
        return jpaRepository.findByChildProfileIdAndActivityId(childProfileId, activityId)
                .stream()
                .map(ChildAchievementPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public List<ChildAchievement> findByChildProfileIdAndTopicId(Long childProfileId, Long topicId) {
        return jpaRepository.findByChildProfileIdAndTopicId(childProfileId, topicId)
                .stream()
                .map(ChildAchievementPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public List<ChildAchievement> findByChildProfileIdAndActivityIdAndTopicId(
            Long childProfileId, Long activityId, Long topicId) {
        return jpaRepository.findByChildProfileIdAndActivityIdAndTopicId(childProfileId, activityId, topicId)
                .stream()
                .map(ChildAchievementPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public Optional<ChildAchievement> findByChildProfileIdAndAchievementCodeAndActivityIdAndTopicId(
            Long childProfileId, String achievementCode, Long activityId, Long topicId) {
        return jpaRepository.findByChildProfileIdAndAchievementCodeAndActivityIdAndTopicId(
                        childProfileId, achievementCode, activityId, topicId)
                .map(ChildAchievementPersistenceAdapter::toDomain);
    }

    @Override
    public ChildAchievement save(ChildAchievement achievement) {
        return toDomain(jpaRepository.save(toJpa(achievement)));
    }

    static ChildAchievement toDomain(ChildAchievementJpaEntity source) {
        var target = new ChildAchievement();
        target.setId(source.getId());
        target.setChildProfileId(source.getChildProfileId());
        target.setAchievementCode(source.getAchievementCode());
        target.setActivityId(source.getActivityId());
        target.setTopicId(source.getTopicId());
        target.setEarnedAt(source.getEarnedAt());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    static ChildAchievementJpaEntity toJpa(ChildAchievement source) {
        var target = new ChildAchievementJpaEntity();
        target.setId(source.getId());
        target.setChildProfileId(source.getChildProfileId());
        target.setAchievementCode(source.getAchievementCode());
        target.setActivityId(source.getActivityId());
        target.setTopicId(source.getTopicId());
        target.setEarnedAt(source.getEarnedAt());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }
}
