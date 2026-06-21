package es.vargontoc.educational.framework.content.service;

import es.vargontoc.educational.framework.content.infrastructure.dto.GameCatalogReadiness;
import es.vargontoc.educational.framework.content.model.Activity;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.DifficultyCode;
import es.vargontoc.educational.framework.content.model.DifficultyLevel;
import es.vargontoc.educational.framework.content.ports.in.ActivityUseCase;
import es.vargontoc.educational.framework.content.ports.in.DifficultyLevelUseCase;
import es.vargontoc.educational.framework.tracking.model.ActivitySummary;
import es.vargontoc.educational.framework.tracking.ports.out.ActivitySummaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameCatalogServiceTest {

    @Mock
    private ActivityUseCase activityUseCase;

    @Mock
    private DifficultyLevelUseCase difficultyLevelUseCase;

    @Mock
    private ActivitySummaryRepository activitySummaryRepository;

    private GameCatalogService gameCatalogService;

    @BeforeEach
    void setUp() {
        gameCatalogService = new GameCatalogService(activityUseCase, difficultyLevelUseCase, activitySummaryRepository);
    }

    @Test
    void getGameReadiness_returningChild_getsSavedDifficulty() {
        var activity = new Activity();
        activity.setId(1L);
        activity.setName("Test Activity");
        activity.setStatus(ContentStatus.ACTIVE);

        var difficultyLevel = new DifficultyLevel();
        difficultyLevel.setId(10L);
        difficultyLevel.setActivityId(1L);
        difficultyLevel.setDifficultyCode(DifficultyCode.MEDIUM);
        difficultyLevel.setEngineParams("{\"speed\":2}");

        var summary = new ActivitySummary();
        summary.setChildProfileId(100L);
        summary.setActivityId(1L);
        summary.setCurrentDifficultyLevelId(10L);

        when(activityUseCase.getGameReadyActivity(1L)).thenReturn(activity);
        when(activitySummaryRepository.findByChildProfileIdAndActivityId(100L, 1L)).thenReturn(Optional.of(summary));
        when(difficultyLevelUseCase.getGameReadyDifficultyLevel(10L)).thenReturn(difficultyLevel);

        var result = gameCatalogService.getGameReadiness(100L, 1L);

        assertNotNull(result);
        assertEquals("Test Activity", result.activity().getName());
        assertEquals(DifficultyCode.MEDIUM, result.difficultyLevel().getDifficultyCode());
        assertFalse(result.isNewToActivity());
    }

    @Test
    void getGameReadiness_newChild_getsEasiestDifficulty() {
        var activity = new Activity();
        activity.setId(1L);
        activity.setName("Test Activity");
        activity.setStatus(ContentStatus.ACTIVE);

        var difficultyLevel = new DifficultyLevel();
        difficultyLevel.setId(5L);
        difficultyLevel.setActivityId(1L);
        difficultyLevel.setDifficultyCode(DifficultyCode.EASY);
        difficultyLevel.setEngineParams("{\"speed\":1}");

        when(activityUseCase.getGameReadyActivity(1L)).thenReturn(activity);
        when(activitySummaryRepository.findByChildProfileIdAndActivityId(100L, 1L)).thenReturn(Optional.empty());
        when(difficultyLevelUseCase.getEasiestDifficultyLevel(1L)).thenReturn(difficultyLevel);

        var result = gameCatalogService.getGameReadiness(100L, 1L);

        assertNotNull(result);
        assertEquals("Test Activity", result.activity().getName());
        assertEquals(DifficultyCode.EASY, result.difficultyLevel().getDifficultyCode());
        assertTrue(result.isNewToActivity());
    }

    @Test
    void getGameReadiness_existingSummaryNullDifficultyId_getsEasiestDifficulty() {
        var activity = new Activity();
        activity.setId(1L);
        activity.setName("Test Activity");
        activity.setStatus(ContentStatus.ACTIVE);

        var difficultyLevel = new DifficultyLevel();
        difficultyLevel.setId(5L);
        difficultyLevel.setActivityId(1L);
        difficultyLevel.setDifficultyCode(DifficultyCode.EASY);
        difficultyLevel.setEngineParams("{\"speed\":1}");

        var summary = new ActivitySummary();
        summary.setChildProfileId(100L);
        summary.setActivityId(1L);
        summary.setCurrentDifficultyLevelId(null);

        when(activityUseCase.getGameReadyActivity(1L)).thenReturn(activity);
        when(activitySummaryRepository.findByChildProfileIdAndActivityId(100L, 1L)).thenReturn(Optional.of(summary));
        when(difficultyLevelUseCase.getEasiestDifficultyLevel(1L)).thenReturn(difficultyLevel);

        var result = gameCatalogService.getGameReadiness(100L, 1L);

        assertNotNull(result);
        assertEquals(DifficultyCode.EASY, result.difficultyLevel().getDifficultyCode());
        assertTrue(result.isNewToActivity());
    }
}
