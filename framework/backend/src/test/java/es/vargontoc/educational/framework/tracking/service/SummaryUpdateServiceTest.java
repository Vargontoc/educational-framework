package es.vargontoc.educational.framework.tracking.service;

import es.vargontoc.educational.framework.content.ports.out.RecognitionElementRepository;
import es.vargontoc.educational.framework.tracking.config.ElementMasteryProperties;
import es.vargontoc.educational.framework.tracking.model.ActivityAttempt;
import es.vargontoc.educational.framework.tracking.model.ActivitySummary;
import es.vargontoc.educational.framework.tracking.model.AttemptResult;
import es.vargontoc.educational.framework.tracking.model.TopicPerformanceBand;
import es.vargontoc.educational.framework.tracking.model.TopicSummary;
import es.vargontoc.educational.framework.tracking.ports.out.ActivitySummaryRepository;
import es.vargontoc.educational.framework.tracking.ports.out.ElementSummaryRepository;
import es.vargontoc.educational.framework.tracking.ports.out.TopicSummaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SummaryUpdateServiceTest {

    @Mock
    private ActivitySummaryRepository activitySummaryRepository;

    @Mock
    private TopicSummaryRepository topicSummaryRepository;

    @Mock
    private ElementSummaryRepository elementSummaryRepository;

    @Mock
    private RecognitionElementRepository recognitionElementRepository;

    private SummaryUpdateService service;

    private ActivityAttempt correctAttempt;
    private ActivityAttempt incorrectAttempt;
    private ActivityAttempt timeoutAttempt;

    @BeforeEach
    void setUp() {
        ElementMasteryProperties masteryProperties = new ElementMasteryProperties();
        service = new SummaryUpdateService(activitySummaryRepository, topicSummaryRepository, elementSummaryRepository, masteryProperties, recognitionElementRepository);

        correctAttempt = new ActivityAttempt();
        correctAttempt.setChildProfileId(10L);
        correctAttempt.setActivityId(20L);
        correctAttempt.setChildSessionId(30L);
        correctAttempt.setTopicId(40L);
        correctAttempt.setDifficultyLevelId(50L);
        correctAttempt.setResult(AttemptResult.CORRECT);
        correctAttempt.setResponseTimeMs(5000);

        incorrectAttempt = new ActivityAttempt();
        incorrectAttempt.setChildProfileId(10L);
        incorrectAttempt.setActivityId(20L);
        incorrectAttempt.setChildSessionId(30L);
        incorrectAttempt.setTopicId(40L);
        incorrectAttempt.setDifficultyLevelId(50L);
        incorrectAttempt.setResult(AttemptResult.INCORRECT);
        incorrectAttempt.setResponseTimeMs(3000);

        timeoutAttempt = new ActivityAttempt();
        timeoutAttempt.setChildProfileId(10L);
        timeoutAttempt.setActivityId(20L);
        timeoutAttempt.setChildSessionId(30L);
        timeoutAttempt.setTopicId(40L);
        timeoutAttempt.setDifficultyLevelId(50L);
        timeoutAttempt.setResult(AttemptResult.TIMEOUT);
        timeoutAttempt.setResponseTimeMs(null);
    }

    @Test
    void firstAttempt_createsActivitySummary() {
        when(activitySummaryRepository.findByChildProfileIdAndActivityId(10L, 20L))
            .thenReturn(Optional.empty());
        when(activitySummaryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.updateSummaries(correctAttempt);

        var captor = ArgumentCaptor.forClass(ActivitySummary.class);
        verify(activitySummaryRepository).save(captor.capture());
        var saved = captor.getValue();
        assertEquals(10L, saved.getChildProfileId());
        assertEquals(20L, saved.getActivityId());
        assertEquals(1, saved.getTotalAttempts());
        assertEquals(1, saved.getTotalCorrect());
        assertEquals(0, saved.getTotalIncorrect());
        assertEquals(0, saved.getTotalTimeouts());
        assertEquals(0, saved.getSuccessRatePercent().compareTo(new BigDecimal("100.00")));
    }

    @Test
    void firstAttempt_createsTopicSummary() {
        when(topicSummaryRepository.findByChildProfileIdAndTopicId(10L, 40L))
            .thenReturn(Optional.empty());
        when(topicSummaryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.updateSummaries(correctAttempt);

        var captor = ArgumentCaptor.forClass(TopicSummary.class);
        verify(topicSummaryRepository).save(captor.capture());
        var saved = captor.getValue();
        assertEquals(10L, saved.getChildProfileId());
        assertEquals(40L, saved.getTopicId());
        assertEquals(1, saved.getTotalAttempts());
        assertEquals(1, saved.getTotalCorrect());
        assertEquals(0, saved.getFailureRatePercent().compareTo(BigDecimal.ZERO));
        assertEquals(TopicPerformanceBand.STRONG, saved.getPerformanceBand());
    }

    @Test
    void secondAttempt_updatesExistingSummaries() {
        var existingActivitySummary = new ActivitySummary();
        existingActivitySummary.setChildProfileId(10L);
        existingActivitySummary.setActivityId(20L);
        existingActivitySummary.setTotalAttempts(1);
        existingActivitySummary.setTotalCorrect(1);
        existingActivitySummary.setTotalIncorrect(0);
        existingActivitySummary.setTotalTimeouts(0);
        existingActivitySummary.setSuccessRatePercent(new BigDecimal("100.00"));
        existingActivitySummary.setAverageResponseTimeMs(5000);

        var existingTopicSummary = new TopicSummary();
        existingTopicSummary.setChildProfileId(10L);
        existingTopicSummary.setTopicId(40L);
        existingTopicSummary.setTotalAttempts(1);
        existingTopicSummary.setTotalCorrect(1);
        existingTopicSummary.setTotalIncorrect(0);
        existingTopicSummary.setTotalTimeouts(0);
        existingTopicSummary.setSuccessRatePercent(new BigDecimal("100.00"));
        existingTopicSummary.setFailureRatePercent(BigDecimal.ZERO);
        existingTopicSummary.setPerformanceBand(TopicPerformanceBand.STRONG);
        existingTopicSummary.setAverageResponseTimeMs(5000);

        when(activitySummaryRepository.findByChildProfileIdAndActivityId(10L, 20L))
            .thenReturn(Optional.of(existingActivitySummary));
        when(topicSummaryRepository.findByChildProfileIdAndTopicId(10L, 40L))
            .thenReturn(Optional.of(existingTopicSummary));
        when(activitySummaryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(topicSummaryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.updateSummaries(incorrectAttempt);

        var activityCaptor = ArgumentCaptor.forClass(ActivitySummary.class);
        verify(activitySummaryRepository).save(activityCaptor.capture());
        var updatedActivity = activityCaptor.getValue();
        assertEquals(2, updatedActivity.getTotalAttempts());
        assertEquals(1, updatedActivity.getTotalCorrect());
        assertEquals(1, updatedActivity.getTotalIncorrect());
        assertEquals(0, updatedActivity.getSuccessRatePercent().compareTo(new BigDecimal("50.00")));

        var topicCaptor = ArgumentCaptor.forClass(TopicSummary.class);
        verify(topicSummaryRepository).save(topicCaptor.capture());
        var updatedTopic = topicCaptor.getValue();
        assertEquals(2, updatedTopic.getTotalAttempts());
        assertEquals(1, updatedTopic.getTotalIncorrect());
        assertEquals(0, updatedTopic.getFailureRatePercent().compareTo(new BigDecimal("50.00")));
        assertEquals(TopicPerformanceBand.WEAK, updatedTopic.getPerformanceBand());
    }

    @Test
    void timeout_incrementsTimeoutCounters() {
        when(activitySummaryRepository.findByChildProfileIdAndActivityId(10L, 20L))
            .thenReturn(Optional.empty());
        when(topicSummaryRepository.findByChildProfileIdAndTopicId(10L, 40L))
            .thenReturn(Optional.empty());
        when(activitySummaryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(topicSummaryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.updateSummaries(timeoutAttempt);

        var activityCaptor = ArgumentCaptor.forClass(ActivitySummary.class);
        verify(activitySummaryRepository).save(activityCaptor.capture());
        assertEquals(1, activityCaptor.getValue().getTotalTimeouts());

        var topicCaptor = ArgumentCaptor.forClass(TopicSummary.class);
        verify(topicSummaryRepository).save(topicCaptor.capture());
        assertEquals(1, topicCaptor.getValue().getTotalTimeouts());
        assertEquals(0, topicCaptor.getValue().getFailureRatePercent().compareTo(new BigDecimal("100.00")));
        assertEquals(TopicPerformanceBand.WEAK, topicCaptor.getValue().getPerformanceBand());
    }

    @Test
    void averageResponseTime_isRecalculated() {
        var existingActivitySummary = new ActivitySummary();
        existingActivitySummary.setChildProfileId(10L);
        existingActivitySummary.setActivityId(20L);
        existingActivitySummary.setTotalAttempts(1);
        existingActivitySummary.setTotalCorrect(1);
        existingActivitySummary.setTotalIncorrect(0);
        existingActivitySummary.setTotalTimeouts(0);
        existingActivitySummary.setSuccessRatePercent(new BigDecimal("100.00"));
        existingActivitySummary.setAverageResponseTimeMs(5000);

        var existingTopicSummary = new TopicSummary();
        existingTopicSummary.setChildProfileId(10L);
        existingTopicSummary.setTopicId(40L);
        existingTopicSummary.setTotalAttempts(1);
        existingTopicSummary.setTotalCorrect(1);
        existingTopicSummary.setTotalIncorrect(0);
        existingTopicSummary.setTotalTimeouts(0);
        existingTopicSummary.setSuccessRatePercent(new BigDecimal("100.00"));
        existingTopicSummary.setFailureRatePercent(BigDecimal.ZERO);
        existingTopicSummary.setPerformanceBand(TopicPerformanceBand.STRONG);
        existingTopicSummary.setAverageResponseTimeMs(5000);

        when(activitySummaryRepository.findByChildProfileIdAndActivityId(10L, 20L))
            .thenReturn(Optional.of(existingActivitySummary));
        when(topicSummaryRepository.findByChildProfileIdAndTopicId(10L, 40L))
            .thenReturn(Optional.of(existingTopicSummary));
        when(activitySummaryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(topicSummaryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.updateSummaries(incorrectAttempt);

        var activityCaptor = ArgumentCaptor.forClass(ActivitySummary.class);
        verify(activitySummaryRepository).save(activityCaptor.capture());
        assertEquals(4000, activityCaptor.getValue().getAverageResponseTimeMs());

        var topicCaptor = ArgumentCaptor.forClass(TopicSummary.class);
        verify(topicSummaryRepository).save(topicCaptor.capture());
        assertEquals(4000, topicCaptor.getValue().getAverageResponseTimeMs());
    }

    @Test
    void averageResponseTime_excludesTimeoutsFromDivisor() {
        var existingActivitySummary = new ActivitySummary();
        existingActivitySummary.setChildProfileId(10L);
        existingActivitySummary.setActivityId(20L);
        existingActivitySummary.setTotalAttempts(1);
        existingActivitySummary.setTotalCorrect(1);
        existingActivitySummary.setTotalIncorrect(0);
        existingActivitySummary.setTotalTimeouts(0);
        existingActivitySummary.setSuccessRatePercent(new BigDecimal("100.00"));
        existingActivitySummary.setAverageResponseTimeMs(6000);

        var existingTopicSummary = new TopicSummary();
        existingTopicSummary.setChildProfileId(10L);
        existingTopicSummary.setTopicId(40L);
        existingTopicSummary.setTotalAttempts(1);
        existingTopicSummary.setTotalCorrect(1);
        existingTopicSummary.setTotalIncorrect(0);
        existingTopicSummary.setTotalTimeouts(0);
        existingTopicSummary.setSuccessRatePercent(new BigDecimal("100.00"));
        existingTopicSummary.setFailureRatePercent(BigDecimal.ZERO);
        existingTopicSummary.setPerformanceBand(TopicPerformanceBand.STRONG);
        existingTopicSummary.setAverageResponseTimeMs(6000);

        when(activitySummaryRepository.findByChildProfileIdAndActivityId(10L, 20L))
            .thenReturn(Optional.of(existingActivitySummary));
        when(topicSummaryRepository.findByChildProfileIdAndTopicId(10L, 40L))
            .thenReturn(Optional.of(existingTopicSummary));
        when(activitySummaryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(topicSummaryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Timeout (null responseTimeMs) — should not affect the average or its divisor
        service.updateSummaries(timeoutAttempt);

        // Correct attempt with rt=4000 — average of [6000, 4000] must be 5000, not 5333
        var correctWithRt = new ActivityAttempt();
        correctWithRt.setChildProfileId(10L);
        correctWithRt.setActivityId(20L);
        correctWithRt.setChildSessionId(30L);
        correctWithRt.setTopicId(40L);
        correctWithRt.setDifficultyLevelId(50L);
        correctWithRt.setResult(AttemptResult.CORRECT);
        correctWithRt.setResponseTimeMs(4000);

        service.updateSummaries(correctWithRt);

        var activityCaptor = ArgumentCaptor.forClass(ActivitySummary.class);
        verify(activitySummaryRepository, org.mockito.Mockito.times(2)).save(activityCaptor.capture());
        assertEquals(5000, activityCaptor.getAllValues().get(1).getAverageResponseTimeMs());

        var topicCaptor = ArgumentCaptor.forClass(TopicSummary.class);
        verify(topicSummaryRepository, org.mockito.Mockito.times(2)).save(topicCaptor.capture());
        assertEquals(5000, topicCaptor.getAllValues().get(1).getAverageResponseTimeMs());
    }

    @Test
    void performanceBand_isWEAK_whenFailuresAbove40Percent() {
        var existingTopicSummary = createTopicSummaryWithFailures(4, 2);
        when(topicSummaryRepository.findByChildProfileIdAndTopicId(10L, 40L))
            .thenReturn(Optional.of(existingTopicSummary));
        when(topicSummaryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.updateSummaries(incorrectAttempt);

        var topicCaptor = ArgumentCaptor.forClass(TopicSummary.class);
        verify(topicSummaryRepository).save(topicCaptor.capture());
        var saved = topicCaptor.getValue();
        assertEquals(0, saved.getFailureRatePercent().compareTo(new BigDecimal("60.00")));
        assertEquals(TopicPerformanceBand.WEAK, saved.getPerformanceBand());
    }

    @Test
    void performanceBand_isMEDIUM_whenFailuresBetween20and40Percent() {
        var existingTopicSummary = createTopicSummaryWithFailures(5, 1);
        when(topicSummaryRepository.findByChildProfileIdAndTopicId(10L, 40L))
            .thenReturn(Optional.of(existingTopicSummary));
        when(topicSummaryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.updateSummaries(incorrectAttempt);

        var topicCaptor = ArgumentCaptor.forClass(TopicSummary.class);
        verify(topicSummaryRepository).save(topicCaptor.capture());
        var saved = topicCaptor.getValue();
        assertEquals(0, saved.getFailureRatePercent().compareTo(new BigDecimal("33.33")));
        assertEquals(TopicPerformanceBand.MEDIUM, saved.getPerformanceBand());
    }

    @Test
    void performanceBand_isSTRONG_whenFailuresBelow20Percent() {
        var existingTopicSummary = createTopicSummaryWithFailures(10, 1);
        when(topicSummaryRepository.findByChildProfileIdAndTopicId(10L, 40L))
            .thenReturn(Optional.of(existingTopicSummary));
        when(topicSummaryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.updateSummaries(correctAttempt);

        var topicCaptor = ArgumentCaptor.forClass(TopicSummary.class);
        verify(topicSummaryRepository).save(topicCaptor.capture());
        var saved = topicCaptor.getValue();
        assertEquals(0, saved.getFailureRatePercent().compareTo(new BigDecimal("9.09")));
        assertEquals(TopicPerformanceBand.STRONG, saved.getPerformanceBand());
    }

    @Test
    void elementSummary_skippedWhenElementIdDoesNotExist() {
        correctAttempt.setElementId(999L);

        when(activitySummaryRepository.findByChildProfileIdAndActivityId(10L, 20L))
            .thenReturn(Optional.empty());
        when(activitySummaryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(topicSummaryRepository.findByChildProfileIdAndTopicId(10L, 40L))
            .thenReturn(Optional.empty());
        when(topicSummaryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(recognitionElementRepository.existsById(999L)).thenReturn(false);

        service.updateSummaries(correctAttempt);

        verify(elementSummaryRepository, never()).save(any());
        verify(activitySummaryRepository).save(any());
        verify(topicSummaryRepository).save(any());
    }

    @Test
    void elementSummary_updatedWhenElementIdExists() {
        correctAttempt.setElementId(100L);

        when(activitySummaryRepository.findByChildProfileIdAndActivityId(10L, 20L))
            .thenReturn(Optional.empty());
        when(activitySummaryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(topicSummaryRepository.findByChildProfileIdAndTopicId(10L, 40L))
            .thenReturn(Optional.empty());
        when(topicSummaryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(recognitionElementRepository.existsById(100L)).thenReturn(true);
        when(elementSummaryRepository.findByChildProfileIdAndElementId(10L, 100L))
            .thenReturn(Optional.empty());
        when(elementSummaryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.updateSummaries(correctAttempt);

        verify(elementSummaryRepository).save(any());
    }

    private TopicSummary createTopicSummaryWithFailures(int total, int failures) {
        var summary = new TopicSummary();
        summary.setChildProfileId(10L);
        summary.setTopicId(40L);
        summary.setTotalAttempts(total);
        summary.setTotalCorrect(total - failures);
        summary.setTotalIncorrect(failures);
        summary.setTotalTimeouts(0);
        double failureRate = (failures * 100.0) / total;
        summary.setFailureRatePercent(BigDecimal.valueOf(failureRate).setScale(2, java.math.RoundingMode.HALF_UP));
        summary.setPerformanceBand(TopicPerformanceBand.MEDIUM);
        summary.setAverageResponseTimeMs(5000);
        return summary;
    }
}
