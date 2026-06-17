package es.vargontoc.educational.framework.tracking.ports.in;

import es.vargontoc.educational.framework.tracking.model.ChildAchievement;

import java.util.List;

public interface GetChildAchievementsUseCase {

    List<ChildAchievement> getChildAchievements(Long childProfileId);

    List<ChildAchievement> getChildAchievements(Long childProfileId, Long activityId);

    List<ChildAchievement> getChildAchievements(Long childProfileId, Long activityId, Long topicId);
}
