package es.vargontoc.educational.framework.tracking.service;

import es.vargontoc.educational.framework.tracking.model.ActivityAttempt;
import es.vargontoc.educational.framework.tracking.model.AdaptiveDifficultyAction;
import es.vargontoc.educational.framework.tracking.model.AdaptiveDifficultyResult;
import es.vargontoc.educational.framework.tracking.model.AttemptRegistrationResult;
import es.vargontoc.educational.framework.tracking.model.AttemptResult;
import es.vargontoc.educational.framework.tracking.model.UnlockedAchievement;
import es.vargontoc.educational.framework.tracking.ports.in.RegisterActivityAttemptUseCase;
import es.vargontoc.educational.framework.tracking.ports.out.ActivityAttemptRepository;
import es.vargontoc.educational.framework.tracking.validation.ActivityAttemptValidator;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Transactional
public class ActivityAttemptService implements RegisterActivityAttemptUseCase {

    private final ActivityAttemptRepository repository;
    private final ActivityAttemptValidator validator;
    private final SummaryUpdateService summaryUpdateService;
    private final AdaptiveDifficultyService adaptiveDifficultyService;
    private final AchievementEvaluationService achievementEvaluationService;

    public ActivityAttemptService(
            ActivityAttemptRepository repository,
            SummaryUpdateService summaryUpdateService,
            AdaptiveDifficultyService adaptiveDifficultyService,
            AchievementEvaluationService achievementEvaluationService) {
        this.repository = repository;
        this.validator = new ActivityAttemptValidator();
        this.summaryUpdateService = summaryUpdateService;
        this.adaptiveDifficultyService = adaptiveDifficultyService;
        this.achievementEvaluationService = achievementEvaluationService;
    }

    @Override
    public AttemptRegistrationResult register(
            Long childProfileId,
            Long activityId,
            Long childSessionId,
            Long topicId,
            Long elementId,
            Long difficultyLevelId,
            AttemptResult result,
            Integer responseTimeMs,
            String attemptContext) {

        validator.validate(childProfileId, activityId, childSessionId, topicId, difficultyLevelId, result);

        var attempt = new ActivityAttempt();
        attempt.setChildProfileId(childProfileId);
        attempt.setActivityId(activityId);
        attempt.setChildSessionId(childSessionId);
        attempt.setTopicId(topicId);
        attempt.setElementId(elementId);
        attempt.setDifficultyLevelId(difficultyLevelId);
        attempt.setResult(result);
        attempt.setResponseTimeMs(responseTimeMs);
        attempt.setAttemptContext(attemptContext);

        var saved = repository.save(attempt);

        summaryUpdateService.updateSummaries(saved);

        List<UnlockedAchievement> allUnlocked = new ArrayList<>();

        AdaptiveDifficultyResult difficultyResult = adaptiveDifficultyService.evaluate(childProfileId, activityId, difficultyLevelId);
        if (difficultyResult != null && difficultyResult.action() == AdaptiveDifficultyAction.INCREASE) {
            List<UnlockedAchievement> difficultyAchievements =
                    achievementEvaluationService.evaluateDifficultyIncreaseAchievements(childProfileId, activityId, topicId);
            allUnlocked.addAll(difficultyAchievements);
        }

        List<UnlockedAchievement> attemptAchievements =
                achievementEvaluationService.evaluateAttemptAchievements(childProfileId, activityId, topicId, result);
        allUnlocked.addAll(attemptAchievements);

        boolean difficultyChanged = false;
        Long newDifficultyLevelId = null;
        if (difficultyResult != null && difficultyResult.action() != AdaptiveDifficultyAction.MAINTAIN
                && difficultyResult.newDifficultyLevelId() != null) {
            difficultyChanged = true;
            newDifficultyLevelId = difficultyResult.newDifficultyLevelId();
        }

        return new AttemptRegistrationResult(saved.getId(), saved.getCreatedAt(), allUnlocked, difficultyChanged, newDifficultyLevelId);
    }
}
