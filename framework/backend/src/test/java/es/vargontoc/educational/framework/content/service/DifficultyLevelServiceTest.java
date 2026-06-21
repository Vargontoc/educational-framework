package es.vargontoc.educational.framework.content.service;

import es.vargontoc.educational.framework.content.model.Activity;
import es.vargontoc.educational.framework.content.model.DifficultyCode;
import es.vargontoc.educational.framework.content.model.DifficultyLevel;
import es.vargontoc.educational.framework.content.ports.out.ActivityRepository;
import es.vargontoc.educational.framework.content.ports.out.DifficultyLevelRepository;
import es.vargontoc.educational.framework.shared.exception.ContentNotReadyException;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DifficultyLevelServiceTest {

    @Mock
    private DifficultyLevelRepository difficultyLevelRepository;

    @Mock
    private ActivityRepository activityRepository;

    private DifficultyLevelService difficultyLevelService;

    @BeforeEach
    void setUp() {
        difficultyLevelService = new DifficultyLevelService(difficultyLevelRepository, activityRepository);
    }

    @Test
    void createDifficultyLevel_happyPath() {
        var activity = new Activity();
        activity.setId(1L);

        when(activityRepository.findById(1L)).thenReturn(Optional.of(activity));
        when(difficultyLevelRepository.save(any(DifficultyLevel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = difficultyLevelService.createDifficultyLevel(1L, DifficultyCode.EASY, "{\"speed\":1}", "{\"threshold\":0.8}");

        assertEquals(1L, result.getActivityId());
        assertEquals(DifficultyCode.EASY, result.getDifficultyCode());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void createDifficultyLevel_activityNotFound_throwsResourceNotFound() {
        when(activityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            difficultyLevelService.createDifficultyLevel(99L, DifficultyCode.EASY, null, null));
    }

    @Test
    void listByActivity_returnsFiltered() {
        when(difficultyLevelRepository.findByActivityId(1L)).thenReturn(List.of(new DifficultyLevel(), new DifficultyLevel()));

        var result = difficultyLevelService.listByActivity(1L);

        assertEquals(2, result.size());
    }

    @Test
    void updateDifficultyLevel_happyPath() {
        var existing = new DifficultyLevel();
        existing.setId(1L);
        existing.setActivityId(1L);
        existing.setDifficultyCode(DifficultyCode.EASY);

        when(difficultyLevelRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(difficultyLevelRepository.save(any(DifficultyLevel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = difficultyLevelService.updateDifficultyLevel(1L, DifficultyCode.HARD, "{\"speed\":3}", null);

        assertEquals(DifficultyCode.HARD, result.getDifficultyCode());
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    void updateDifficultyLevel_notFound_throwsResourceNotFound() {
        when(difficultyLevelRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            difficultyLevelService.updateDifficultyLevel(99L, DifficultyCode.HARD, null, null));
    }

    @Test
    void getGameReadyDifficultyLevel_happyPath() {
        var difficultyLevel = new DifficultyLevel();
        difficultyLevel.setId(1L);
        difficultyLevel.setActivityId(1L);
        difficultyLevel.setDifficultyCode(DifficultyCode.MEDIUM);
        difficultyLevel.setEngineParams("{\"speed\":2}");

        when(difficultyLevelRepository.findByIdAndEngineParamsIsNotNull(1L)).thenReturn(Optional.of(difficultyLevel));

        var result = difficultyLevelService.getGameReadyDifficultyLevel(1L);

        assertEquals(DifficultyCode.MEDIUM, result.getDifficultyCode());
        assertNotNull(result.getEngineParams());
    }

    @Test
    void getGameReadyDifficultyLevel_noEngineParams_throwsContentNotReady() {
        when(difficultyLevelRepository.findByIdAndEngineParamsIsNotNull(1L)).thenReturn(Optional.empty());

        assertThrows(ContentNotReadyException.class, () -> difficultyLevelService.getGameReadyDifficultyLevel(1L));
    }

    @Test
    void getEasiestDifficultyLevel_happyPath() {
        var difficultyLevel = new DifficultyLevel();
        difficultyLevel.setId(1L);
        difficultyLevel.setActivityId(1L);
        difficultyLevel.setDifficultyCode(DifficultyCode.EASY);
        difficultyLevel.setEngineParams("{\"speed\":1}");

        when(difficultyLevelRepository.findFirstByActivityIdOrderByDifficultyCodeAsc(1L)).thenReturn(Optional.of(difficultyLevel));

        var result = difficultyLevelService.getEasiestDifficultyLevel(1L);

        assertEquals(DifficultyCode.EASY, result.getDifficultyCode());
    }

    @Test
    void getEasiestDifficultyLevel_noDifficultyLevels_throwsContentNotReady() {
        when(difficultyLevelRepository.findFirstByActivityIdOrderByDifficultyCodeAsc(1L)).thenReturn(Optional.empty());

        assertThrows(ContentNotReadyException.class, () -> difficultyLevelService.getEasiestDifficultyLevel(1L));
    }
}
