package es.vargontoc.educational.framework.tracking.service;

import es.vargontoc.educational.framework.content.model.LearningPath;
import es.vargontoc.educational.framework.content.model.LearningPathStep;
import es.vargontoc.educational.framework.content.ports.out.LearningPathRepository;
import es.vargontoc.educational.framework.content.ports.out.LearningPathStepRepository;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import es.vargontoc.educational.framework.tracking.model.ChildLearningProgress;
import es.vargontoc.educational.framework.tracking.model.ChildLearningCompletedStep;
import es.vargontoc.educational.framework.tracking.ports.out.ChildLearningCompletedStepRepository;
import es.vargontoc.educational.framework.tracking.ports.out.ChildLearningProgressRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterLearningPathStepProgressUseCaseTest {

    @Mock
    private ChildLearningProgressRepository progressRepository;

    @Mock
    private ChildLearningCompletedStepRepository completedStepRepository;

    @Mock
    private LearningPathRepository learningPathRepository;

    @Mock
    private LearningPathStepRepository learningPathStepRepository;

    @InjectMocks
    private ChildLearningProgressService service;

    @Test
    void registerLearningPathStepProgress_advanceToNewStep() {
        var learningPath = new LearningPath();
        learningPath.setId(1L);

        var step = new LearningPathStep();
        step.setId(10L);

        var savedProgress = new ChildLearningProgress();
        savedProgress.setChildProfileId(1L);
        savedProgress.setLearningPathId(1L);
        savedProgress.setCurrentLearningPathStepId(10L);

        when(learningPathRepository.findById(1L)).thenReturn(Optional.of(learningPath));
        when(learningPathStepRepository.findByLearningPathId(1L)).thenReturn(List.of(step));
        when(progressRepository.findByChildProfileIdAndLearningPathId(1L, 1L)).thenReturn(Optional.of(savedProgress));
        when(completedStepRepository.findByChildProfileIdAndLearningPathIdAndStepId(1L, 1L, 10L)).thenReturn(Optional.empty());
        when(progressRepository.save(any(ChildLearningProgress.class))).thenAnswer(inv -> inv.getArgument(0));
        when(completedStepRepository.save(any(ChildLearningCompletedStep.class))).thenAnswer(inv -> inv.getArgument(0));
        when(completedStepRepository.findByChildProfileIdAndLearningPathId(1L, 1L)).thenReturn(List.of());

        var result = service.registerLearningPathStepProgress(1L, 1L, 10L);

        assertNotNull(result);
        assertEquals(1L, result.getChildProfileId());
        assertEquals(1L, result.getLearningPathId());
        assertEquals(10L, result.getCurrentStepId());
    }

    @Test
    void registerLearningPathStepProgress_duplicateCompletedStepNotInserted() {
        var learningPath = new LearningPath();
        learningPath.setId(1L);

        var step = new LearningPathStep();
        step.setId(10L);

        var existingProgress = new ChildLearningProgress();
        existingProgress.setChildProfileId(1L);
        existingProgress.setLearningPathId(1L);
        existingProgress.setCurrentLearningPathStepId(5L);

        var existingCompletedStep = new ChildLearningCompletedStep();
        existingCompletedStep.setChildProfileId(1L);
        existingCompletedStep.setLearningPathId(1L);
        existingCompletedStep.setLearningPathStepId(10L);

        when(learningPathRepository.findById(1L)).thenReturn(Optional.of(learningPath));
        when(learningPathStepRepository.findByLearningPathId(1L)).thenReturn(List.of(step));
        when(progressRepository.findByChildProfileIdAndLearningPathId(1L, 1L)).thenReturn(Optional.of(existingProgress));
        when(completedStepRepository.findByChildProfileIdAndLearningPathIdAndStepId(1L, 1L, 10L)).thenReturn(Optional.of(existingCompletedStep));
        when(progressRepository.save(any(ChildLearningProgress.class))).thenAnswer(inv -> inv.getArgument(0));
        when(completedStepRepository.findByChildProfileIdAndLearningPathId(1L, 1L)).thenReturn(List.of(existingCompletedStep));

        var result = service.registerLearningPathStepProgress(1L, 1L, 10L);

        verify(completedStepRepository, never()).save(any());
        assertEquals(10L, result.getCurrentStepId());
    }

    @Test
    void registerLearningPathStepProgress_invalidStepForPathRejected() {
        var learningPath = new LearningPath();
        learningPath.setId(1L);

        var stepFromDifferentPath = new LearningPathStep();
        stepFromDifferentPath.setId(999L);

        when(learningPathRepository.findById(1L)).thenReturn(Optional.of(learningPath));
        when(learningPathStepRepository.findByLearningPathId(1L)).thenReturn(List.of(stepFromDifferentPath));

        assertThrows(ValidationException.class, () ->
                service.registerLearningPathStepProgress(1L, 1L, 10L));
    }

    @Test
    void registerLearningPathStepProgress_operationIsIdempotent() {
        var learningPath = new LearningPath();
        learningPath.setId(1L);

        var step = new LearningPathStep();
        step.setId(10L);

        var existingProgress = new ChildLearningProgress();
        existingProgress.setChildProfileId(1L);
        existingProgress.setLearningPathId(1L);
        existingProgress.setCurrentLearningPathStepId(10L);

        var existingCompletedStep = new ChildLearningCompletedStep();
        existingCompletedStep.setChildProfileId(1L);
        existingCompletedStep.setLearningPathId(1L);
        existingCompletedStep.setLearningPathStepId(10L);

        when(learningPathRepository.findById(1L)).thenReturn(Optional.of(learningPath));
        when(learningPathStepRepository.findByLearningPathId(1L)).thenReturn(List.of(step));
        when(progressRepository.findByChildProfileIdAndLearningPathId(1L, 1L)).thenReturn(Optional.of(existingProgress));
        when(completedStepRepository.findByChildProfileIdAndLearningPathIdAndStepId(1L, 1L, 10L)).thenReturn(Optional.of(existingCompletedStep));
        when(progressRepository.save(any(ChildLearningProgress.class))).thenAnswer(inv -> inv.getArgument(0));
        when(completedStepRepository.findByChildProfileIdAndLearningPathId(1L, 1L)).thenReturn(List.of(existingCompletedStep));

        var result = service.registerLearningPathStepProgress(1L, 1L, 10L);

        verify(completedStepRepository, never()).save(any());
        verify(progressRepository).save(any());
        assertEquals(10L, result.getCurrentStepId());
    }

    @Test
    void registerLearningPathStepProgress_createsProgressIfNotExists() {
        var learningPath = new LearningPath();
        learningPath.setId(1L);

        var step = new LearningPathStep();
        step.setId(10L);

        when(learningPathRepository.findById(1L)).thenReturn(Optional.of(learningPath));
        when(learningPathStepRepository.findByLearningPathId(1L)).thenReturn(List.of(step));
        when(progressRepository.findByChildProfileIdAndLearningPathId(1L, 1L)).thenReturn(Optional.empty());
        when(completedStepRepository.findByChildProfileIdAndLearningPathIdAndStepId(1L, 1L, 10L)).thenReturn(Optional.empty());
        when(progressRepository.save(any(ChildLearningProgress.class))).thenAnswer(inv -> {
            var p = inv.getArgument(0, ChildLearningProgress.class);
            p.setId(100L);
            return p;
        });
        when(completedStepRepository.save(any(ChildLearningCompletedStep.class))).thenAnswer(inv -> inv.getArgument(0));
        when(completedStepRepository.findByChildProfileIdAndLearningPathId(1L, 1L)).thenReturn(List.of());

        service.registerLearningPathStepProgress(1L, 1L, 10L);

        ArgumentCaptor<ChildLearningProgress> progressCaptor = ArgumentCaptor.forClass(ChildLearningProgress.class);
        verify(progressRepository).save(progressCaptor.capture());
        assertEquals(1L, progressCaptor.getValue().getChildProfileId());
        assertEquals(1L, progressCaptor.getValue().getLearningPathId());
        assertEquals(10L, progressCaptor.getValue().getCurrentLearningPathStepId());
    }

    @Test
    void registerLearningPathStepProgress_throwsWhenChildProfileIdIsNull() {
        assertThrows(ValidationException.class, () ->
                service.registerLearningPathStepProgress(null, 1L, 10L));
    }

    @Test
    void registerLearningPathStepProgress_throwsWhenLearningPathIdIsNull() {
        assertThrows(ValidationException.class, () ->
                service.registerLearningPathStepProgress(1L, null, 10L));
    }

    @Test
    void registerLearningPathStepProgress_throwsWhenLearningPathStepIdIsNull() {
        assertThrows(ValidationException.class, () ->
                service.registerLearningPathStepProgress(1L, 1L, null));
    }
}
