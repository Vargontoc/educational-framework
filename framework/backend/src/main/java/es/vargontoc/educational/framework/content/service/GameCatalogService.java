package es.vargontoc.educational.framework.content.service;

import es.vargontoc.educational.framework.content.model.Activity;
import es.vargontoc.educational.framework.content.model.DifficultyLevel;
import es.vargontoc.educational.framework.content.model.GameCatalogReadiness;
import es.vargontoc.educational.framework.content.ports.in.ActivityUseCase;
import es.vargontoc.educational.framework.content.ports.in.DifficultyLevelUseCase;
import es.vargontoc.educational.framework.content.ports.in.GameCatalogUseCase;
import es.vargontoc.educational.framework.tracking.ports.out.ActivitySummaryRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public class GameCatalogService implements GameCatalogUseCase {

    private final ActivityUseCase activityUseCase;
    private final DifficultyLevelUseCase difficultyLevelUseCase;
    private final ActivitySummaryRepository activitySummaryRepository;

    public GameCatalogService(
            ActivityUseCase activityUseCase,
            DifficultyLevelUseCase difficultyLevelUseCase,
            ActivitySummaryRepository activitySummaryRepository) {
        this.activityUseCase = activityUseCase;
        this.difficultyLevelUseCase = difficultyLevelUseCase;
        this.activitySummaryRepository = activitySummaryRepository;
    }

    @Override
    public GameCatalogReadiness getGameReadiness(Long childProfileId, Long activityId) {
        Activity activity = activityUseCase.getGameReadyActivity(activityId);

        var summaryOpt = activitySummaryRepository.findByChildProfileIdAndActivityId(childProfileId, activityId);

        DifficultyLevel difficultyLevel;
        boolean isNewToActivity;

        if (summaryOpt.isPresent() && summaryOpt.get().getCurrentDifficultyLevelId() != null) {
            difficultyLevel = difficultyLevelUseCase.getGameReadyDifficultyLevel(summaryOpt.get().getCurrentDifficultyLevelId());
            isNewToActivity = false;
        } else {
            difficultyLevel = difficultyLevelUseCase.getEasiestDifficultyLevel(activityId);
            isNewToActivity = true;
        }

        return new GameCatalogReadiness(activity, difficultyLevel, isNewToActivity);
    }
}