package es.vargontoc.educational.framework.tracking.service;

import es.vargontoc.educational.framework.tracking.model.AchievementCode;
import es.vargontoc.educational.framework.tracking.model.AttemptResult;
import es.vargontoc.educational.framework.tracking.model.UnlockedAchievement;
import es.vargontoc.educational.framework.tracking.ports.in.RegisterChildAchievementUseCase;
import es.vargontoc.educational.framework.tracking.ports.out.ActivityAttemptRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Transactional
public class AchievementEvaluationService {

    private static final int STREAK_LENGTH_FOR_ACHIEVEMENT = 5;

    private final ActivityAttemptRepository attemptRepository;
    private final RegisterChildAchievementUseCase achievementService;

    public AchievementEvaluationService(
            ActivityAttemptRepository attemptRepository,
            RegisterChildAchievementUseCase achievementService) {
        this.attemptRepository = attemptRepository;
        this.achievementService = achievementService;
    }

    public List<UnlockedAchievement> evaluateAttemptAchievements(
            Long childProfileId,
            Long activityId,
            Long topicId,
            AttemptResult result) {

        List<UnlockedAchievement> unlocked = new ArrayList<>();

        if (result == AttemptResult.CORRECT) {
            int recentCorrect = countRecentCorrectAttempts(childProfileId, activityId);
            if (recentCorrect == STREAK_LENGTH_FOR_ACHIEVEMENT) {
                boolean registered = achievementService.registerAchievement(
                        childProfileId,
                        AchievementCode.FIRST_CORRECT_STREAK.name(),
                        activityId,
                        topicId);
                if (registered) {
                    unlocked.add(new UnlockedAchievement(
                            AchievementCode.FIRST_CORRECT_STREAK.name(),
                            activityId,
                            topicId));
                }
            }
        }

        return unlocked;
    }

    public List<UnlockedAchievement> evaluateDifficultyIncreaseAchievements(
            Long childProfileId,
            Long activityId,
            Long topicId) {

        List<UnlockedAchievement> unlocked = new ArrayList<>();

        boolean registered = achievementService.registerAchievement(
                childProfileId,
                AchievementCode.DIFFICULTY_INCREASED.name(),
                activityId,
                topicId);
        if (registered) {
            unlocked.add(new UnlockedAchievement(
                    AchievementCode.DIFFICULTY_INCREASED.name(),
                    activityId,
                    topicId));
        }

        return unlocked;
    }

    public List<UnlockedAchievement> evaluateCompletionAchievements(
            Long childProfileId,
            Long activityId,
            Long topicId) {

        List<UnlockedAchievement> unlocked = new ArrayList<>();

        boolean registered = achievementService.registerAchievement(
                childProfileId,
                AchievementCode.ACTIVITY_COMPLETED.name(),
                activityId,
                topicId);
        if (registered) {
            unlocked.add(new UnlockedAchievement(
                    AchievementCode.ACTIVITY_COMPLETED.name(),
                    activityId,
                    topicId));
        }

        return unlocked;
    }

    private int countRecentCorrectAttempts(Long childProfileId, Long activityId) {
        var recentAttempts = attemptRepository.findRecentByChildAndActivity(childProfileId, activityId, STREAK_LENGTH_FOR_ACHIEVEMENT);
        int correctCount = 0;
        for (var attempt : recentAttempts) {
            if (attempt.getResult() == AttemptResult.CORRECT) {
                correctCount++;
            } else {
                correctCount = 0;
            }
        }
        return correctCount;
    }
}