package es.vargontoc.educational.framework.tracking.service;

import es.vargontoc.educational.framework.tracking.model.UnlockedAchievement;
import es.vargontoc.educational.framework.tracking.ports.in.EvaluateGameCompletionAchievementsUseCase;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public class GameCompletionAchievementService implements EvaluateGameCompletionAchievementsUseCase {

    private final AchievementEvaluationService achievementEvaluationService;

    public GameCompletionAchievementService(AchievementEvaluationService achievementEvaluationService) {
        this.achievementEvaluationService = achievementEvaluationService;
    }

    @Override
    public List<UnlockedAchievement> evaluate(Long childProfileId, Long activityId, Long topicId) {
        return achievementEvaluationService.evaluateCompletionAchievements(childProfileId, activityId, topicId);
    }
}