package es.vargontoc.educational.framework.tracking.service;

import es.vargontoc.educational.framework.content.ports.out.RecognitionElementRepository;
import es.vargontoc.educational.framework.tracking.config.ElementMasteryProperties;
import es.vargontoc.educational.framework.tracking.model.ActivityAttempt;
import es.vargontoc.educational.framework.tracking.model.AttemptResult;
import es.vargontoc.educational.framework.tracking.model.ElementMasteryState;
import es.vargontoc.educational.framework.tracking.model.ElementSummary;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ElementSummaryUpdateTest {

    @Mock
    private ActivitySummaryRepository activitySummaryRepository;

    @Mock
    private TopicSummaryRepository topicSummaryRepository;

    @Mock
    private ElementSummaryRepository elementSummaryRepository;

    @Mock
    private RecognitionElementRepository recognitionElementRepository;

    private SummaryUpdateService service;

    @BeforeEach
    void setUp() {
        ElementMasteryProperties props = new ElementMasteryProperties();
        props.setMasteredSuccessRatePercent(80);
        props.setMinAttemptsForMastery(3);
        service = new SummaryUpdateService(activitySummaryRepository, topicSummaryRepository, elementSummaryRepository, props, recognitionElementRepository);
        lenient().when(recognitionElementRepository.existsById(anyLong())).thenReturn(true);
    }

    @Test
    void elementSummary_aggregatesPerChildAndElement() {
        var existing = new ElementSummary();
        existing.setChildProfileId(10L);
        existing.setElementId(100L);
        existing.setTotalAttempts(2);
        existing.setTotalCorrect(1);
        existing.setTotalIncorrect(1);
        existing.setSuccessRatePercent(new BigDecimal("50.00"));
        existing.setAverageResponseTimeMs(4000);
        existing.setMasteryState(ElementMasteryState.LEARNING);

        when(elementSummaryRepository.findByChildProfileIdAndElementId(10L, 100L))
                .thenReturn(Optional.of(existing));
        when(elementSummaryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var attempt = buildAttempt(10L, 100L, AttemptResult.CORRECT, 3000);
        service.updateSummaries(attempt);

        var captor = ArgumentCaptor.forClass(ElementSummary.class);
        verify(elementSummaryRepository).save(captor.capture());
        var saved = captor.getValue();
        assertEquals(3, saved.getTotalAttempts());
        assertEquals(2, saved.getTotalCorrect());
        assertEquals(1, saved.getTotalIncorrect());
    }

    @Test
    void elementSummary_independentForDifferentElements() {
        when(elementSummaryRepository.findByChildProfileIdAndElementId(10L, 100L))
                .thenReturn(Optional.empty());
        when(elementSummaryRepository.findByChildProfileIdAndElementId(10L, 200L))
                .thenReturn(Optional.empty());
        when(elementSummaryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.updateSummaries(buildAttempt(10L, 100L, AttemptResult.CORRECT, 3000));
        service.updateSummaries(buildAttempt(10L, 200L, AttemptResult.INCORRECT, 4000));

        var captor = ArgumentCaptor.forClass(ElementSummary.class);
        verify(elementSummaryRepository, times(2)).save(captor.capture());

        var first = captor.getAllValues().get(0);
        assertEquals(100L, first.getElementId());
        assertEquals(1, first.getTotalCorrect());

        var second = captor.getAllValues().get(1);
        assertEquals(200L, second.getElementId());
        assertEquals(1, second.getTotalIncorrect());
    }

    @Test
    void masteryState_transitionsToMastered() {
        var existing = new ElementSummary();
        existing.setChildProfileId(10L);
        existing.setElementId(100L);
        existing.setTotalAttempts(2);
        existing.setTotalCorrect(2);
        existing.setTotalIncorrect(0);
        existing.setSuccessRatePercent(new BigDecimal("100.00"));
        existing.setAverageResponseTimeMs(3000);
        existing.setMasteryState(ElementMasteryState.LEARNING);

        when(elementSummaryRepository.findByChildProfileIdAndElementId(10L, 100L))
                .thenReturn(Optional.of(existing));
        when(elementSummaryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.updateSummaries(buildAttempt(10L, 100L, AttemptResult.CORRECT, 3000));

        var captor = ArgumentCaptor.forClass(ElementSummary.class);
        verify(elementSummaryRepository).save(captor.capture());
        assertEquals(ElementMasteryState.MASTERED, captor.getValue().getMasteryState());
    }

    @Test
    void masteryState_staysLearningBelowMinAttempts() {
        var existing = new ElementSummary();
        existing.setChildProfileId(10L);
        existing.setElementId(100L);
        existing.setTotalAttempts(1);
        existing.setTotalCorrect(1);
        existing.setTotalIncorrect(0);
        existing.setSuccessRatePercent(new BigDecimal("100.00"));
        existing.setAverageResponseTimeMs(3000);
        existing.setMasteryState(ElementMasteryState.LEARNING);

        when(elementSummaryRepository.findByChildProfileIdAndElementId(10L, 100L))
                .thenReturn(Optional.of(existing));
        when(elementSummaryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.updateSummaries(buildAttempt(10L, 100L, AttemptResult.CORRECT, 3000));

        var captor = ArgumentCaptor.forClass(ElementSummary.class);
        verify(elementSummaryRepository).save(captor.capture());
        assertEquals(ElementMasteryState.LEARNING, captor.getValue().getMasteryState());
    }

    @Test
    void masteryState_staysLearningWhenSuccessRateBelowThreshold() {
        var existing = new ElementSummary();
        existing.setChildProfileId(10L);
        existing.setElementId(100L);
        existing.setTotalAttempts(2);
        existing.setTotalCorrect(1);
        existing.setTotalIncorrect(1);
        existing.setSuccessRatePercent(new BigDecimal("50.00"));
        existing.setAverageResponseTimeMs(4000);
        existing.setMasteryState(ElementMasteryState.LEARNING);

        when(elementSummaryRepository.findByChildProfileIdAndElementId(10L, 100L))
                .thenReturn(Optional.of(existing));
        when(elementSummaryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.updateSummaries(buildAttempt(10L, 100L, AttemptResult.INCORRECT, 5000));

        var captor = ArgumentCaptor.forClass(ElementSummary.class);
        verify(elementSummaryRepository).save(captor.capture());
        assertEquals(ElementMasteryState.LEARNING, captor.getValue().getMasteryState());
    }

    @Test
    void noElementSummaryUpdate_whenElementIdIsNull() {
        var attempt = buildAttempt(10L, null, AttemptResult.CORRECT, 3000);
        service.updateSummaries(attempt);

        verify(elementSummaryRepository, times(0)).save(any());
    }

    private ActivityAttempt buildAttempt(Long childProfileId, Long elementId, AttemptResult result, Integer responseTimeMs) {
        var attempt = new ActivityAttempt();
        attempt.setChildProfileId(childProfileId);
        attempt.setActivityId(20L);
        attempt.setChildSessionId(30L);
        attempt.setTopicId(40L);
        attempt.setElementId(elementId);
        attempt.setDifficultyLevelId(50L);
        attempt.setResult(result);
        attempt.setResponseTimeMs(responseTimeMs);
        return attempt;
    }
}
