package es.vargontoc.educational.framework.content.service;

import es.vargontoc.educational.framework.content.model.Activity;
import es.vargontoc.educational.framework.content.model.LearningPath;
import es.vargontoc.educational.framework.content.model.LearningPathStep;
import es.vargontoc.educational.framework.content.ports.out.ActivityRepository;
import es.vargontoc.educational.framework.content.ports.out.LearningPathRepository;
import es.vargontoc.educational.framework.content.ports.out.LearningPathStepRepository;
import es.vargontoc.educational.framework.shared.exception.ConflictException;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
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
class LearningPathStepServiceTest {

    @Mock
    private LearningPathStepRepository stepRepository;

    @Mock
    private LearningPathRepository learningPathRepository;

    @Mock
    private ActivityRepository activityRepository;

    private LearningPathStepService stepService;

    @BeforeEach
    void setUp() {
        stepService = new LearningPathStepService(stepRepository, learningPathRepository, activityRepository);
    }

    @Test
    void createStep_happyPath() {
        when(learningPathRepository.findById(1L)).thenReturn(Optional.of(new LearningPath()));
        when(activityRepository.findById(1L)).thenReturn(Optional.of(new Activity()));
        when(stepRepository.existsByLearningPathIdAndStepOrder(1L, 1)).thenReturn(false);
        when(stepRepository.save(any(LearningPathStep.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = stepService.createStep(1L, 1L, 1, "PREVIOUS_COMPLETED", null);

        assertEquals(1L, result.getLearningPathId());
        assertEquals(1L, result.getActivityId());
        assertEquals(1, result.getStepOrder());
        assertEquals("PREVIOUS_COMPLETED", result.getUnlockCondition());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void createStep_learningPathNotFound_throwsResourceNotFound() {
        when(learningPathRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            stepService.createStep(99L, 1L, 1, null, null));
    }

    @Test
    void createStep_activityNotFound_throwsResourceNotFound() {
        when(learningPathRepository.findById(1L)).thenReturn(Optional.of(new LearningPath()));
        when(activityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            stepService.createStep(1L, 99L, 1, null, null));
    }

    @Test
    void createStep_duplicateOrder_throwsConflict() {
        when(learningPathRepository.findById(1L)).thenReturn(Optional.of(new LearningPath()));
        when(activityRepository.findById(1L)).thenReturn(Optional.of(new Activity()));
        when(stepRepository.existsByLearningPathIdAndStepOrder(1L, 1)).thenReturn(true);

        assertThrows(ConflictException.class, () ->
            stepService.createStep(1L, 1L, 1, null, null));
    }

    @Test
    void createStep_nullLearningPathId_throwsValidation() {
        assertThrows(ValidationException.class, () ->
            stepService.createStep(null, 1L, 1, null, null));
    }

    @Test
    void createStep_stepOrderZero_throwsValidation() {
        assertThrows(ValidationException.class, () ->
            stepService.createStep(1L, 1L, 0, null, null));
    }

    @Test
    void getStep_notFound_throwsResourceNotFound() {
        when(stepRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> stepService.getStep(99L));
    }

    @Test
    void listStepsByLearningPath_returnsFiltered() {
        when(stepRepository.findByLearningPathId(1L)).thenReturn(List.of(new LearningPathStep(), new LearningPathStep()));

        var result = stepService.listStepsByLearningPath(1L);

        assertEquals(2, result.size());
    }
}
