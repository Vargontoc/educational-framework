package es.vargontoc.educational.framework.tracking.service;

import es.vargontoc.educational.framework.content.model.LearningPath;
import es.vargontoc.educational.framework.content.ports.out.LearningPathRepository;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import es.vargontoc.educational.framework.tracking.model.ChildLearningProgressResponse;
import es.vargontoc.educational.framework.tracking.model.ChildLearningCompletedStep;
import es.vargontoc.educational.framework.tracking.model.ChildLearningProgress;
import es.vargontoc.educational.framework.tracking.ports.out.ChildLearningCompletedStepRepository;
import es.vargontoc.educational.framework.tracking.ports.out.ChildLearningProgressRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChildLearningProgressServiceTest {

    @Mock
    private ChildLearningProgressRepository progressRepository;

    @Mock
    private ChildLearningCompletedStepRepository completedStepRepository;

    @Mock
    private LearningPathRepository learningPathRepository;

    @InjectMocks
    private ChildLearningProgressService service;

    @Test
    void updateCurrentStep_createsNewProgressWhenNotExists() {
        var learningPath = new LearningPath();
        learningPath.setId(1L);

        when(learningPathRepository.findById(1L)).thenReturn(Optional.of(learningPath));
        when(progressRepository.findByChildProfileIdAndLearningPathId(1L, 1L)).thenReturn(Optional.empty());
        when(progressRepository.save(any(ChildLearningProgress.class))).thenAnswer(inv -> inv.getArgument(0));

        service.updateCurrentStep(1L, 1L, 10L);

        var captor = ArgumentCaptor.forClass(ChildLearningProgress.class);
        verify(progressRepository).save(captor.capture());
        var saved = captor.getValue();
        assertEquals(1L, saved.getChildProfileId());
        assertEquals(1L, saved.getLearningPathId());
        assertEquals(10L, saved.getCurrentLearningPathStepId());
    }

    @Test
    void updateCurrentStep_updatesExistingProgress() {
        var learningPath = new LearningPath();
        learningPath.setId(1L);

        var existing = new ChildLearningProgress();
        existing.setChildProfileId(1L);
        existing.setLearningPathId(1L);
        existing.setCurrentLearningPathStepId(5L);

        when(learningPathRepository.findById(1L)).thenReturn(Optional.of(learningPath));
        when(progressRepository.findByChildProfileIdAndLearningPathId(1L, 1L)).thenReturn(Optional.of(existing));
        when(progressRepository.save(any(ChildLearningProgress.class))).thenAnswer(inv -> inv.getArgument(0));

        service.updateCurrentStep(1L, 1L, 10L);

        var captor = ArgumentCaptor.forClass(ChildLearningProgress.class);
        verify(progressRepository).save(captor.capture());
        assertEquals(10L, captor.getValue().getCurrentLearningPathStepId());
    }

    @Test
    void updateCurrentStep_throwsWhenLearningPathNotFound() {
        when(learningPathRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () ->
                service.updateCurrentStep(1L, 999L, 10L));
    }

    @Test
    void updateCurrentStep_throwsWhenLearningPathIdIsNull() {
        assertThrows(ValidationException.class, () ->
                service.updateCurrentStep(1L, null, 10L));
    }

    @Test
    void registerCompletedStep_createsNewRecord() {
        when(completedStepRepository.findByChildProfileIdAndLearningPathIdAndStepId(1L, 1L, 10L))
                .thenReturn(Optional.empty());
        when(completedStepRepository.save(any(ChildLearningCompletedStep.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        service.registerCompletedStep(1L, 1L, 10L);

        var captor = ArgumentCaptor.forClass(ChildLearningCompletedStep.class);
        verify(completedStepRepository).save(captor.capture());
        var saved = captor.getValue();
        assertEquals(1L, saved.getChildProfileId());
        assertEquals(1L, saved.getLearningPathId());
        assertEquals(10L, saved.getLearningPathStepId());
        assertNotNull(saved.getCompletedAt());
    }

    @Test
    void registerCompletedStep_ignoresDuplicate() {
        var existing = new ChildLearningCompletedStep();
        existing.setChildProfileId(1L);
        existing.setLearningPathId(1L);
        existing.setLearningPathStepId(10L);

        when(completedStepRepository.findByChildProfileIdAndLearningPathIdAndStepId(1L, 1L, 10L))
                .thenReturn(Optional.of(existing));

        service.registerCompletedStep(1L, 1L, 10L);

        verify(completedStepRepository, never()).save(any());
    }

    @Test
    void getChildLearningProgress_returnsProgressWithCompletedSteps() {
        var progress = new ChildLearningProgress();
        progress.setChildProfileId(1L);
        progress.setLearningPathId(1L);
        progress.setCurrentLearningPathStepId(5L);

        var completedStep = new ChildLearningCompletedStep();
        completedStep.setChildProfileId(1L);
        completedStep.setLearningPathId(1L);
        completedStep.setLearningPathStepId(10L);
        completedStep.setCompletedAt(LocalDateTime.now());

        when(progressRepository.findByChildProfileIdAndLearningPathId(1L, 1L))
                .thenReturn(Optional.of(progress));
        when(completedStepRepository.findByChildProfileIdAndLearningPathId(1L, 1L))
                .thenReturn(List.of(completedStep));

        ChildLearningProgressResponse response = service.getChildLearningProgress(1L, 1L);

        assertEquals(1L, response.getChildProfileId());
        assertEquals(1L, response.getLearningPathId());
        assertEquals(5L, response.getCurrentStepId());
        assertEquals(1, response.getCompletedSteps().size());
        assertEquals(10L, response.getCompletedSteps().get(0).getStepId());
    }

    @Test
    void getChildLearningProgress_returnsEmptyWhenNoProgress() {
        when(progressRepository.findByChildProfileIdAndLearningPathId(1L, 1L))
                .thenReturn(Optional.empty());
        when(completedStepRepository.findByChildProfileIdAndLearningPathId(1L, 1L))
                .thenReturn(List.of());

        ChildLearningProgressResponse response = service.getChildLearningProgress(1L, 1L);

        assertEquals(1L, response.getChildProfileId());
        assertEquals(1L, response.getLearningPathId());
        assertEquals(null, response.getCurrentStepId());
        assertTrue(response.getCompletedSteps().isEmpty());
    }

    @Test
    void getChildLearningProgress_returnsMultipleCompletedSteps() {
        var progress = new ChildLearningProgress();
        progress.setChildProfileId(1L);
        progress.setLearningPathId(1L);

        var step1 = new ChildLearningCompletedStep();
        step1.setChildProfileId(1L);
        step1.setLearningPathId(1L);
        step1.setLearningPathStepId(10L);
        step1.setCompletedAt(LocalDateTime.now());

        var step2 = new ChildLearningCompletedStep();
        step2.setChildProfileId(1L);
        step2.setLearningPathId(1L);
        step2.setLearningPathStepId(20L);
        step2.setCompletedAt(LocalDateTime.now());

        when(progressRepository.findByChildProfileIdAndLearningPathId(1L, 1L))
                .thenReturn(Optional.of(progress));
        when(completedStepRepository.findByChildProfileIdAndLearningPathId(1L, 1L))
                .thenReturn(List.of(step1, step2));

        ChildLearningProgressResponse response = service.getChildLearningProgress(1L, 1L);

        assertEquals(2, response.getCompletedSteps().size());
    }
}
