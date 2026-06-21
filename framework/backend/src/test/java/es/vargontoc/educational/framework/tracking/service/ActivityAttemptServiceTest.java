package es.vargontoc.educational.framework.tracking.service;

import es.vargontoc.educational.framework.tracking.model.ActivityAttempt;
import es.vargontoc.educational.framework.tracking.model.AdaptiveDifficultyAction;
import es.vargontoc.educational.framework.tracking.model.AdaptiveDifficultyResult;
import es.vargontoc.educational.framework.tracking.model.AttemptRegistrationResult;
import es.vargontoc.educational.framework.tracking.model.AttemptResult;
import es.vargontoc.educational.framework.tracking.model.UnlockedAchievement;
import es.vargontoc.educational.framework.tracking.ports.out.ActivityAttemptRepository;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityAttemptServiceTest {

    @Mock
    private ActivityAttemptRepository repository;

    @Mock
    private SummaryUpdateService summaryUpdateService;

    @Mock
    private AdaptiveDifficultyService adaptiveDifficultyService;

    @Mock
    private AchievementEvaluationService achievementEvaluationService;

    @InjectMocks
    private ActivityAttemptService service;

    @Test
    void register_withAllRequiredFields_persistsAttempt() {
        var captor = ArgumentCaptor.forClass(ActivityAttempt.class);
        when(repository.save(any())).thenAnswer(inv -> {
            var attempt = inv.getArgument(0, ActivityAttempt.class);
            attempt.setId(1L);
            attempt.setCreatedAt(LocalDateTime.now());
            return attempt;
        });
        when(adaptiveDifficultyService.evaluate(anyLong(), anyLong(), anyLong()))
                .thenReturn(new AdaptiveDifficultyResult(AdaptiveDifficultyAction.MAINTAIN, "no change"));
        lenient().when(achievementEvaluationService.evaluateDifficultyIncreaseAchievements(anyLong(), anyLong(), anyLong()))
                .thenReturn(Collections.emptyList());
        when(achievementEvaluationService.evaluateAttemptAchievements(anyLong(), anyLong(), anyLong(), any(AttemptResult.class)))
                .thenReturn(Collections.emptyList());

        var result = service.register(10L, 20L, 30L, 40L, 50L, AttemptResult.CORRECT, 5000, null);

        assertNotNull(result);
        assertEquals(1L, result.attemptId());
        assertNotNull(result.createdAt());
        assertNotNull(result.unlockedAchievements());
        verify(repository).save(captor.capture());
        verify(summaryUpdateService).updateSummaries(any(ActivityAttempt.class));

        var saved = captor.getValue();
        assertEquals(10L, saved.getChildProfileId());
        assertEquals(20L, saved.getActivityId());
        assertEquals(30L, saved.getChildSessionId());
        assertEquals(40L, saved.getTopicId());
        assertEquals(50L, saved.getDifficultyLevelId());
        assertEquals(AttemptResult.CORRECT, saved.getResult());
    }

    @Test
    void register_withOptionalFields_persistsAttemptContextAndResponseTime() {
        var captor = ArgumentCaptor.forClass(ActivityAttempt.class);
        when(repository.save(any())).thenAnswer(inv -> {
            var attempt = inv.getArgument(0, ActivityAttempt.class);
            attempt.setId(2L);
            attempt.setCreatedAt(LocalDateTime.now());
            return attempt;
        });
        when(adaptiveDifficultyService.evaluate(anyLong(), anyLong(), anyLong()))
                .thenReturn(new AdaptiveDifficultyResult(AdaptiveDifficultyAction.MAINTAIN, "no change"));
        lenient().when(achievementEvaluationService.evaluateDifficultyIncreaseAchievements(anyLong(), anyLong(), anyLong()))
                .thenReturn(Collections.emptyList());
        when(achievementEvaluationService.evaluateAttemptAchievements(anyLong(), anyLong(), anyLong(), any(AttemptResult.class)))
                .thenReturn(Collections.emptyList());

        String context = "{\"engine\":\"memory\",\"level\":3}";
        var result = service.register(10L, 20L, 30L, 40L, 50L, AttemptResult.INCORRECT, 3000, context);

        assertNotNull(result);
        verify(repository).save(captor.capture());

        var saved = captor.getValue();
        assertEquals(3000, saved.getResponseTimeMs());
        assertEquals(context, saved.getAttemptContext());
    }

    @Test
    void register_missingChildProfileId_throwsValidationException() {
        assertThrows(ValidationException.class, () ->
            service.register(null, 20L, 30L, 40L, 50L, AttemptResult.CORRECT, null, null));
    }

    @Test
    void register_missingActivityId_throwsValidationException() {
        assertThrows(ValidationException.class, () ->
            service.register(10L, null, 30L, 40L, 50L, AttemptResult.CORRECT, null, null));
    }

    @Test
    void register_missingResult_throwsValidationException() {
        assertThrows(ValidationException.class, () ->
            service.register(10L, 20L, 30L, 40L, 50L, null, null, null));
    }

    @Test
    void register_withDifficultyIncrease_returnsUnlockedAchievement() {
        var captor = ArgumentCaptor.forClass(ActivityAttempt.class);
        when(repository.save(any())).thenAnswer(inv -> {
            var attempt = inv.getArgument(0, ActivityAttempt.class);
            attempt.setId(1L);
            attempt.setCreatedAt(LocalDateTime.now());
            return attempt;
        });

        UnlockedAchievement difficultyAchievement = new UnlockedAchievement("DIFFICULTY_INCREASED", 20L, 40L);
        when(adaptiveDifficultyService.evaluate(anyLong(), anyLong(), anyLong()))
                .thenReturn(new AdaptiveDifficultyResult(AdaptiveDifficultyAction.INCREASE, "score exceeded threshold"));
        when(achievementEvaluationService.evaluateDifficultyIncreaseAchievements(anyLong(), anyLong(), anyLong()))
                .thenReturn(List.of(difficultyAchievement));
        when(achievementEvaluationService.evaluateAttemptAchievements(anyLong(), anyLong(), anyLong(), any()))
                .thenReturn(Collections.emptyList());

        var result = service.register(10L, 20L, 30L, 40L, 50L, AttemptResult.CORRECT, 5000, null);

        assertNotNull(result);
        assertEquals(1, result.unlockedAchievements().size());
        assertEquals("DIFFICULTY_INCREASED", result.unlockedAchievements().get(0).achievementCode());
    }

    @Test
    void register_withAttemptAchievement_returnsUnlockedAchievement() {
        var captor = ArgumentCaptor.forClass(ActivityAttempt.class);
        when(repository.save(any())).thenAnswer(inv -> {
            var attempt = inv.getArgument(0, ActivityAttempt.class);
            attempt.setId(1L);
            attempt.setCreatedAt(LocalDateTime.now());
            return attempt;
        });

        UnlockedAchievement streakAchievement = new UnlockedAchievement("FIRST_CORRECT_STREAK", 20L, 40L);
        when(adaptiveDifficultyService.evaluate(anyLong(), anyLong(), anyLong()))
                .thenReturn(new AdaptiveDifficultyResult(AdaptiveDifficultyAction.MAINTAIN, "no change"));
        when(achievementEvaluationService.evaluateAttemptAchievements(anyLong(), anyLong(), anyLong(), any(AttemptResult.class)))
                .thenReturn(List.of(streakAchievement));

        var result = service.register(10L, 20L, 30L, 40L, 50L, AttemptResult.CORRECT, 5000, null);

        assertNotNull(result);
        assertEquals(1, result.unlockedAchievements().size());
        assertEquals("FIRST_CORRECT_STREAK", result.unlockedAchievements().get(0).achievementCode());
    }
}
