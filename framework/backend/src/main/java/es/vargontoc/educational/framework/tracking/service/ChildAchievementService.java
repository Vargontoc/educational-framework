package es.vargontoc.educational.framework.tracking.service;

import es.vargontoc.educational.framework.shared.exception.ValidationException;
import es.vargontoc.educational.framework.tracking.model.ChildAchievement;
import es.vargontoc.educational.framework.tracking.ports.in.GetChildAchievementsUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.RegisterChildAchievementUseCase;
import es.vargontoc.educational.framework.tracking.ports.out.ChildAchievementRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Transactional
public class ChildAchievementService implements RegisterChildAchievementUseCase, GetChildAchievementsUseCase {

    private final ChildAchievementRepository repository;

    public ChildAchievementService(ChildAchievementRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean registerAchievement(Long childProfileId, String achievementCode, Long activityId, Long topicId) {
        if (achievementCode == null || achievementCode.isBlank()) {
            throw new ValidationException("Achievement code cannot be blank");
        }

        var existing = repository.findByChildProfileIdAndAchievementCodeAndActivityIdAndTopicId(
                childProfileId, achievementCode, activityId, topicId);

        if (existing.isPresent()) {
            return false;
        }

        var achievement = new ChildAchievement();
        achievement.setChildProfileId(childProfileId);
        achievement.setAchievementCode(achievementCode);
        achievement.setActivityId(activityId);
        achievement.setTopicId(topicId);
        achievement.setEarnedAt(LocalDateTime.now());

        repository.save(achievement);
        return true;
    }

    @Override
    public List<ChildAchievement> getChildAchievements(Long childProfileId) {
        return repository.findByChildProfileId(childProfileId);
    }

    @Override
    public List<ChildAchievement> getChildAchievements(Long childProfileId, Long activityId) {
        if (activityId != null) {
            return repository.findByChildProfileIdAndActivityId(childProfileId, activityId);
        }
        return repository.findByChildProfileId(childProfileId);
    }

    @Override
    public List<ChildAchievement> getChildAchievements(Long childProfileId, Long activityId, Long topicId) {
        if (activityId != null && topicId != null) {
            return repository.findByChildProfileIdAndActivityIdAndTopicId(childProfileId, activityId, topicId);
        }
        if (activityId != null) {
            return repository.findByChildProfileIdAndActivityId(childProfileId, activityId);
        }
        if (topicId != null) {
            return repository.findByChildProfileIdAndTopicId(childProfileId, topicId);
        }
        return repository.findByChildProfileId(childProfileId);
    }
}
