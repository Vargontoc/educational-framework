package es.vargontoc.educational.framework.tracking.ports.in;

import es.vargontoc.educational.framework.tracking.model.UnlockedAchievement;

import java.util.List;

public interface EvaluateGameCompletionAchievementsUseCase {

    List<UnlockedAchievement> evaluate(Long childProfileId, Long activityId, Long topicId);
}