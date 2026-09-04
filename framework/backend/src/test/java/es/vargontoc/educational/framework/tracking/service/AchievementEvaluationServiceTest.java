package es.vargontoc.educational.framework.tracking.service;


import es.vargontoc.educational.framework.tracking.model.ActivityAttempt;
import es.vargontoc.educational.framework.tracking.model.AttemptResult;
import es.vargontoc.educational.framework.tracking.model.UnlockedAchievement;
import es.vargontoc.educational.framework.tracking.ports.in.RegisterChildAchievementUseCase;
import es.vargontoc.educational.framework.tracking.ports.out.ActivityAttemptRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AchievementEvaluationServiceTest {

    @Mock
    private ActivityAttemptRepository attemptRepository;

    @Mock
    private RegisterChildAchievementUseCase achievementService;

    @InjectMocks
    private AchievementEvaluationService service;

    @Test
    void evaluateAttemptAchievements_correctResultFirstInStreak_returnsEmpty() {
        when(attemptRepository.findRecentByChildAndActivity(anyLong(), anyLong(), eq(5)))
                .thenReturn(List.of(
                        createAttempt(1L, AttemptResult.CORRECT),
                        createAttempt(2L, AttemptResult.CORRECT),
                        createAttempt(3L, AttemptResult.CORRECT),
                        createAttempt(4L, AttemptResult.CORRECT)));

        List<UnlockedAchievement> result = service.evaluateAttemptAchievements(
                10L, 20L, 30L, AttemptResult.CORRECT);

        assertTrue(result.isEmpty());
        verify(achievementService, never()).registerAchievement(any(), any(), any(), any());
    }

    @Test
    void evaluateAttemptAchievements_correctResultFifthInStreak_returnsAchievement() {
        when(attemptRepository.findRecentByChildAndActivity(anyLong(), anyLong(), eq(5)))
                .thenReturn(List.of(
                        createAttempt(1L, AttemptResult.CORRECT),
                        createAttempt(2L, AttemptResult.CORRECT),
                        createAttempt(3L, AttemptResult.CORRECT),
                        createAttempt(4L, AttemptResult.CORRECT),
                        createAttempt(5L, AttemptResult.CORRECT)));
        when(achievementService.registerAchievement(10L, "FIRST_CORRECT_STREAK", 20L, 30L)).thenReturn(true);

        List<UnlockedAchievement> result = service.evaluateAttemptAchievements(
                10L, 20L, 30L, AttemptResult.CORRECT);

        assertEquals(1, result.size());
        assertEquals("FIRST_CORRECT_STREAK", result.get(0).achievementCode());
        verify(achievementService).registerAchievement(10L, "FIRST_CORRECT_STREAK", 20L, 30L);
    }

    @Test
    void evaluateAttemptAchievements_incorrectResult_returnsEmpty() {
        List<UnlockedAchievement> result = service.evaluateAttemptAchievements(
                10L, 20L, 30L, AttemptResult.INCORRECT);

        assertTrue(result.isEmpty());
        verify(achievementService, never()).registerAchievement(any(), any(), any(), any());
    }

    @Test
    void evaluateAttemptAchievements_timeoutResult_returnsEmpty() {
        List<UnlockedAchievement> result = service.evaluateAttemptAchievements(
                10L, 20L, 30L, AttemptResult.TIMEOUT);

        assertTrue(result.isEmpty());
        verify(achievementService, never()).registerAchievement(any(), any(), any(), any());
    }

    @Test
    void evaluateDifficultyIncreaseAchievements_returnsAchievement() {
        when(achievementService.registerAchievement(10L, "DIFFICULTY_INCREASED", 20L, 30L)).thenReturn(true);

        List<UnlockedAchievement> result = service.evaluateDifficultyIncreaseAchievements(10L, 20L, 30L);

        assertEquals(1, result.size());
        assertEquals("DIFFICULTY_INCREASED", result.get(0).achievementCode());
        verify(achievementService).registerAchievement(10L, "DIFFICULTY_INCREASED", 20L, 30L);
    }

    @Test
    void evaluateDifficultyIncreaseAchievements_alreadyEarned_returnsEmpty() {
        when(achievementService.registerAchievement(10L, "DIFFICULTY_INCREASED", 20L, 30L)).thenReturn(false);

        List<UnlockedAchievement> result = service.evaluateDifficultyIncreaseAchievements(10L, 20L, 30L);

        assertTrue(result.isEmpty());
    }

    @Test
    void evaluateCompletionAchievements_returnsAchievement() {
        when(achievementService.registerAchievement(10L, "ACTIVITY_COMPLETED", 20L, 30L)).thenReturn(true);

        List<UnlockedAchievement> result = service.evaluateCompletionAchievements(10L, 20L, 30L);

        assertEquals(1, result.size());
        assertEquals("ACTIVITY_COMPLETED", result.get(0).achievementCode());
        verify(achievementService).registerAchievement(10L, "ACTIVITY_COMPLETED", 20L, 30L);
    }

    @Test
    void evaluateCompletionAchievements_alreadyEarned_returnsEmpty() {
        when(achievementService.registerAchievement(10L, "ACTIVITY_COMPLETED", 20L, 30L)).thenReturn(false);

        List<UnlockedAchievement> result = service.evaluateCompletionAchievements(10L, 20L, 30L);

        assertTrue(result.isEmpty());
    }

    private ActivityAttempt createAttempt(Long id, AttemptResult result) {
        var attempt = new ActivityAttempt();
        attempt.setId(id);
        attempt.setResult(result);
        return attempt;
    }
}