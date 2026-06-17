package es.vargontoc.educational.framework.tracking.ports.out;

import es.vargontoc.educational.framework.tracking.model.ChildAchievement;

import java.util.List;
import java.util.Optional;

public interface ChildAchievementRepository {

    List<ChildAchievement> findByChildProfileId(Long childProfileId);

    List<ChildAchievement> findByChildProfileIdAndActivityId(Long childProfileId, Long activityId);

    List<ChildAchievement> findByChildProfileIdAndTopicId(Long childProfileId, Long topicId);

    List<ChildAchievement> findByChildProfileIdAndActivityIdAndTopicId(Long childProfileId, Long activityId, Long topicId);

    Optional<ChildAchievement> findByChildProfileIdAndAchievementCodeAndActivityIdAndTopicId(
            Long childProfileId, String achievementCode, Long activityId, Long topicId);

    ChildAchievement save(ChildAchievement achievement);
}
