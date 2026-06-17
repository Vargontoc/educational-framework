package es.vargontoc.educational.framework.tracking.service;

import es.vargontoc.educational.framework.content.ports.out.LearningPathRepository;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import es.vargontoc.educational.framework.tracking.model.ChildLearningProgressResponse;
import es.vargontoc.educational.framework.tracking.model.CompletedStepInfo;
import es.vargontoc.educational.framework.tracking.model.ChildLearningCompletedStep;
import es.vargontoc.educational.framework.tracking.model.ChildLearningProgress;
import es.vargontoc.educational.framework.tracking.ports.in.GetChildLearningProgressUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.RegisterChildLearningCompletedStepUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.UpdateChildLearningProgressUseCase;
import es.vargontoc.educational.framework.tracking.ports.out.ChildLearningCompletedStepRepository;
import es.vargontoc.educational.framework.tracking.ports.out.ChildLearningProgressRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Transactional
public class ChildLearningProgressService implements
        UpdateChildLearningProgressUseCase,
        RegisterChildLearningCompletedStepUseCase,
        GetChildLearningProgressUseCase {

    private final ChildLearningProgressRepository progressRepository;
    private final ChildLearningCompletedStepRepository completedStepRepository;
    private final LearningPathRepository learningPathRepository;

    public ChildLearningProgressService(
            ChildLearningProgressRepository progressRepository,
            ChildLearningCompletedStepRepository completedStepRepository,
            LearningPathRepository learningPathRepository) {
        this.progressRepository = progressRepository;
        this.completedStepRepository = completedStepRepository;
        this.learningPathRepository = learningPathRepository;
    }

    @Override
    public void updateCurrentStep(Long childProfileId, Long learningPathId, Long currentStepId) {
        if (learningPathId == null) {
            throw new ValidationException("Learning path ID cannot be null");
        }

        var existingPath = learningPathRepository.findById(learningPathId);
        if (existingPath.isEmpty()) {
            throw new ValidationException("Learning path not found: " + learningPathId);
        }

        var progress = progressRepository.findByChildProfileIdAndLearningPathId(childProfileId, learningPathId)
                .orElseGet(() -> {
                    var newProgress = new ChildLearningProgress();
                    newProgress.setChildProfileId(childProfileId);
                    newProgress.setLearningPathId(learningPathId);
                    return newProgress;
                });

        progress.setCurrentLearningPathStepId(currentStepId);
        progressRepository.save(progress);
    }

    @Override
    public void registerCompletedStep(Long childProfileId, Long learningPathId, Long stepId) {
        var existing = completedStepRepository.findByChildProfileIdAndLearningPathIdAndStepId(
                childProfileId, learningPathId, stepId);

        if (existing.isPresent()) {
            return;
        }

        var step = new ChildLearningCompletedStep();
        step.setChildProfileId(childProfileId);
        step.setLearningPathId(learningPathId);
        step.setLearningPathStepId(stepId);
        step.setCompletedAt(LocalDateTime.now());

        completedStepRepository.save(step);
    }

    @Override
    public ChildLearningProgressResponse getChildLearningProgress(Long childProfileId, Long learningPathId) {
        var progress = progressRepository.findByChildProfileIdAndLearningPathId(childProfileId, learningPathId)
                .orElse(null);

        var completedSteps = completedStepRepository.findByChildProfileIdAndLearningPathId(childProfileId, learningPathId);

        var response = new ChildLearningProgressResponse();
        response.setChildProfileId(childProfileId);
        response.setLearningPathId(learningPathId);

        if (progress != null) {
            response.setCurrentStepId(progress.getCurrentLearningPathStepId());
        }

        List<CompletedStepInfo> stepInfos = completedSteps.stream()
                .map(s -> new CompletedStepInfo(s.getLearningPathStepId(), s.getCompletedAt()))
                .toList();
        response.setCompletedSteps(stepInfos);

        return response;
    }
}
