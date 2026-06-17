package es.vargontoc.educational.framework.tracking.service;

import es.vargontoc.educational.framework.shared.exception.ValidationException;
import es.vargontoc.educational.framework.tracking.config.AdaptiveDifficultyProperties;
import es.vargontoc.educational.framework.tracking.model.ActivityAttempt;
import es.vargontoc.educational.framework.tracking.model.ActivitySummary;
import es.vargontoc.educational.framework.tracking.model.AdaptiveDifficultyAction;
import es.vargontoc.educational.framework.tracking.model.AdaptiveDifficultyResult;
import es.vargontoc.educational.framework.tracking.model.AttemptResult;
import es.vargontoc.educational.framework.tracking.ports.out.ActivityAttemptRepository;
import es.vargontoc.educational.framework.tracking.ports.out.ActivitySummaryRepository;
import es.vargontoc.educational.framework.tracking.ports.out.DifficultyLevelConfigPort;
import es.vargontoc.educational.framework.tracking.ports.out.DifficultyLevelNavigationPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdaptiveDifficultyServiceTest {

    @Mock
    private ActivityAttemptRepository attemptRepository;

    @Mock
    private ActivitySummaryRepository summaryRepository;

    @Mock
    private DifficultyLevelConfigPort difficultyLevelConfigPort;

    @Mock
    private DifficultyLevelNavigationPort difficultyNavigationPort;

    private AdaptiveDifficultyProperties properties;
    private AdaptiveDifficultyService service;

    @BeforeEach
    void setUp() {
        properties = new AdaptiveDifficultyProperties();
        service = new AdaptiveDifficultyService(
                attemptRepository,
                summaryRepository,
                difficultyLevelConfigPort,
                difficultyNavigationPort,
                properties);
    }

    @Test
    void noChange_belowMinAttempts() {
        properties.setMinAttemptsBeforeChange(5);
        when(attemptRepository.findRecentByChildAndActivity(eq(1L), eq(2L), anyInt()))
                .thenReturn(List.of(createAttempt(AttemptResult.CORRECT, 4000)));

        AdaptiveDifficultyResult result = service.evaluate(1L, 2L, 10L);

        assertEquals(AdaptiveDifficultyAction.MAINTAIN, result.action());
        assertEquals("insufficient attempts", result.reason());
    }

    @Test
    void difficultyIncreases_highAccuracyFastResponse() {
        properties.setMinAttemptsBeforeChange(3);
        properties.setCooldownAttempts(0);
        properties.setIncreaseThresholdPercent(75);
        properties.setTargetResponseTimeMs(5000);
        properties.setAccuracyWeight(0.7);
        properties.setSpeedWeight(0.3);

        List<ActivityAttempt> attempts = List.of(
                createAttempt(AttemptResult.CORRECT, 3000),
                createAttempt(AttemptResult.CORRECT, 3000),
                createAttempt(AttemptResult.CORRECT, 3000));

        when(attemptRepository.findRecentByChildAndActivity(eq(1L), eq(2L), anyInt()))
                .thenReturn(attempts);

        ActivitySummary summary = createSummary(3, 0);
        when(summaryRepository.findByChildProfileIdAndActivityId(1L, 2L))
                .thenReturn(Optional.of(summary));

        when(difficultyLevelConfigPort.getAdaptiveThresholdConfig(anyLong()))
                .thenReturn(null);

        when(difficultyNavigationPort.findOrderedDifficultyLevelIds(2L))
                .thenReturn(new ArrayList<>(List.of(1L, 2L, 3L)));

        AdaptiveDifficultyResult result = service.evaluate(1L, 2L, 2L);

        assertEquals(AdaptiveDifficultyAction.INCREASE, result.action());
        assertEquals(3L, summary.getCurrentDifficultyLevelId());
    }

    @Test
    void difficultyMaintained_highAccuracySlowResponse() {
        properties.setMinAttemptsBeforeChange(3);
        properties.setCooldownAttempts(0);
        properties.setIncreaseThresholdPercent(75);
        properties.setDecreaseThresholdPercent(40);
        properties.setTargetResponseTimeMs(5000);
        properties.setAccuracyWeight(0.7);
        properties.setSpeedWeight(0.3);

        List<ActivityAttempt> attempts = List.of(
                createAttempt(AttemptResult.CORRECT, 8000),
                createAttempt(AttemptResult.CORRECT, 8000),
                createAttempt(AttemptResult.CORRECT, 8000));

        when(attemptRepository.findRecentByChildAndActivity(eq(1L), eq(2L), anyInt()))
                .thenReturn(attempts);

        ActivitySummary summary = createSummary(3, 0);
        when(summaryRepository.findByChildProfileIdAndActivityId(1L, 2L))
                .thenReturn(Optional.of(summary));

        when(difficultyLevelConfigPort.getAdaptiveThresholdConfig(anyLong()))
                .thenReturn(null);

        AdaptiveDifficultyResult result = service.evaluate(1L, 2L, 10L);

        assertEquals(AdaptiveDifficultyAction.MAINTAIN, result.action());
    }

    @Test
    void difficultyDecreases_lowAccuracy() {
        properties.setMinAttemptsBeforeChange(3);
        properties.setCooldownAttempts(0);
        properties.setDecreaseThresholdPercent(40);
        properties.setAccuracyWeight(0.7);
        properties.setSpeedWeight(0.3);
        properties.setTargetResponseTimeMs(5000);

        List<ActivityAttempt> attempts = List.of(
                createAttempt(AttemptResult.INCORRECT, 3000),
                createAttempt(AttemptResult.INCORRECT, 3000),
                createAttempt(AttemptResult.INCORRECT, 3000));

        when(attemptRepository.findRecentByChildAndActivity(eq(1L), eq(2L), anyInt()))
                .thenReturn(attempts);

        ActivitySummary summary = createSummary(0, 3);
        when(summaryRepository.findByChildProfileIdAndActivityId(1L, 2L))
                .thenReturn(Optional.of(summary));

        when(difficultyLevelConfigPort.getAdaptiveThresholdConfig(anyLong()))
                .thenReturn(null);

        when(difficultyNavigationPort.findOrderedDifficultyLevelIds(2L))
                .thenReturn(new ArrayList<>(List.of(1L, 2L)));

        AdaptiveDifficultyResult result = service.evaluate(1L, 2L, 2L);

        assertEquals(AdaptiveDifficultyAction.DECREASE, result.action());
        assertEquals(1L, summary.getCurrentDifficultyLevelId());
    }

    @Test
    void difficultyDecreases_frequentTimeouts() {
        properties.setMinAttemptsBeforeChange(3);
        properties.setCooldownAttempts(0);
        properties.setDecreaseThresholdPercent(40);
        properties.setAccuracyWeight(0.7);
        properties.setSpeedWeight(0.3);
        properties.setTargetResponseTimeMs(5000);
        properties.setTimeoutRateThresholdPercent(30);
        properties.setTimeoutPenaltyWeight(0.3);

        List<ActivityAttempt> attempts = List.of(
                createAttempt(AttemptResult.TIMEOUT, null),
                createAttempt(AttemptResult.TIMEOUT, null),
                createAttempt(AttemptResult.INCORRECT, 3000));

        when(attemptRepository.findRecentByChildAndActivity(eq(1L), eq(2L), anyInt()))
                .thenReturn(attempts);

        ActivitySummary summary = createSummary(0, 3);
        when(summaryRepository.findByChildProfileIdAndActivityId(1L, 2L))
                .thenReturn(Optional.of(summary));

        when(difficultyLevelConfigPort.getAdaptiveThresholdConfig(anyLong()))
                .thenReturn(null);

        when(difficultyNavigationPort.findOrderedDifficultyLevelIds(2L))
                .thenReturn(new ArrayList<>(List.of(1L, 2L)));

        AdaptiveDifficultyResult result = service.evaluate(1L, 2L, 2L);

        assertEquals(AdaptiveDifficultyAction.DECREASE, result.action());
    }

    @Test
    void cooldownPreventsRepeatedChanges() {
        properties.setMinAttemptsBeforeChange(3);
        properties.setCooldownAttempts(5);
        properties.setIncreaseThresholdPercent(75);
        properties.setAccuracyWeight(0.7);
        properties.setSpeedWeight(0.3);
        properties.setTargetResponseTimeMs(5000);

        List<ActivityAttempt> attempts = List.of(
                createAttempt(AttemptResult.CORRECT, 3000),
                createAttempt(AttemptResult.CORRECT, 3000),
                createAttempt(AttemptResult.CORRECT, 3000));

        when(attemptRepository.findRecentByChildAndActivity(eq(1L), eq(2L), anyInt()))
                .thenReturn(attempts);

        ActivitySummary summary = createSummary(3, 0);
        summary.setAttemptsSinceLastDifficultyChange(2);
        when(summaryRepository.findByChildProfileIdAndActivityId(1L, 2L))
                .thenReturn(Optional.of(summary));

        AdaptiveDifficultyResult result = service.evaluate(1L, 2L, 10L);

        assertEquals(AdaptiveDifficultyAction.MAINTAIN, result.action());
        assertEquals("cooldown active", result.reason());
    }

    @Test
    void malformedConfig_throwsValidationException() {
        properties.setMinAttemptsBeforeChange(3);
        properties.setCooldownAttempts(0);

        when(attemptRepository.findRecentByChildAndActivity(eq(1L), eq(2L), anyInt()))
                .thenReturn(List.of(
                        createAttempt(AttemptResult.CORRECT, 3000),
                        createAttempt(AttemptResult.CORRECT, 3000),
                        createAttempt(AttemptResult.CORRECT, 3000)));

        ActivitySummary summary = createSummary(3, 0);
        when(summaryRepository.findByChildProfileIdAndActivityId(1L, 2L))
                .thenReturn(Optional.of(summary));

        when(difficultyLevelConfigPort.getAdaptiveThresholdConfig(anyLong()))
                .thenReturn("{\"increaseThresholdPercent\": \"not-a-number\"}");

        assertThrows(ValidationException.class, () -> service.evaluate(1L, 2L, 10L));
    }

    private ActivityAttempt createAttempt(AttemptResult result, Integer responseTimeMs) {
        var attempt = new ActivityAttempt();
        attempt.setChildProfileId(1L);
        attempt.setActivityId(2L);
        attempt.setChildSessionId(3L);
        attempt.setTopicId(4L);
        attempt.setDifficultyLevelId(10L);
        attempt.setResult(result);
        attempt.setResponseTimeMs(responseTimeMs);
        return attempt;
    }

    private ActivitySummary createSummary(int correct, int incorrect) {
        var summary = new ActivitySummary();
        summary.setChildProfileId(1L);
        summary.setActivityId(2L);
        summary.setTotalAttempts(correct + incorrect);
        summary.setTotalCorrect(correct);
        summary.setTotalIncorrect(incorrect);
        summary.setTotalTimeouts(0);
        summary.setAttemptsSinceLastDifficultyChange(0);
        return summary;
    }
}
