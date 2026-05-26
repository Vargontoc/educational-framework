package es.vargontoc.educational.framework.content.service;

import es.vargontoc.educational.framework.content.model.LearningPathStep;
import es.vargontoc.educational.framework.content.ports.in.LearningPathStepUseCase;
import es.vargontoc.educational.framework.content.ports.out.ActivityRepository;
import es.vargontoc.educational.framework.content.ports.out.LearningPathRepository;
import es.vargontoc.educational.framework.content.ports.out.LearningPathStepRepository;
import es.vargontoc.educational.framework.content.validation.LearningPathStepValidator;
import es.vargontoc.educational.framework.shared.exception.ConflictException;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Transactional
public class LearningPathStepService implements LearningPathStepUseCase {

    private final LearningPathStepRepository stepRepository;
    private final LearningPathRepository learningPathRepository;
    private final ActivityRepository activityRepository;
    private final LearningPathStepValidator stepValidator;

    public LearningPathStepService(LearningPathStepRepository stepRepository, LearningPathRepository learningPathRepository, ActivityRepository activityRepository) {
        this.stepRepository = stepRepository;
        this.learningPathRepository = learningPathRepository;
        this.activityRepository = activityRepository;
        this.stepValidator = new LearningPathStepValidator();
    }

    @Override
    public LearningPathStep createStep(Long learningPathId, Long activityId, Integer stepOrder, String unlockCondition, String visualMetadata) {
        stepValidator.validateForCreate(learningPathId, activityId, stepOrder);

        if (learningPathRepository.findById(learningPathId).isEmpty()) {
            throw new ResourceNotFoundException("LearningPath not found with id: " + learningPathId);
        }
        if (activityRepository.findById(activityId).isEmpty()) {
            throw new ResourceNotFoundException("Activity not found with id: " + activityId);
        }
        if (stepRepository.existsByLearningPathIdAndStepOrder(learningPathId, stepOrder)) {
            throw new ConflictException("Step order " + stepOrder + " already exists in learning path " + learningPathId);
        }

        var step = new LearningPathStep();
        step.setLearningPathId(learningPathId);
        step.setActivityId(activityId);
        step.setStepOrder(stepOrder);
        step.setUnlockCondition(unlockCondition);
        step.setVisualMetadata(visualMetadata);
        step.setCreatedAt(LocalDateTime.now());

        return stepRepository.save(step);
    }

    @Override
    @Transactional(readOnly = true)
    public LearningPathStep getStep(Long id) {
        return stepRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("LearningPathStep not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearningPathStep> listStepsByLearningPath(Long learningPathId) {
        return stepRepository.findByLearningPathId(learningPathId);
    }

    @Override
    public LearningPathStep updateStep(Long id, Long learningPathId, Long activityId, Integer stepOrder, String unlockCondition, String visualMetadata) {
        stepValidator.validateForUpdate(learningPathId, activityId, stepOrder);

        var existing = stepRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("LearningPathStep not found with id: " + id));

        if (learningPathRepository.findById(learningPathId).isEmpty()) {
            throw new ResourceNotFoundException("LearningPath not found with id: " + learningPathId);
        }
        if (activityRepository.findById(activityId).isEmpty()) {
            throw new ResourceNotFoundException("Activity not found with id: " + activityId);
        }

        boolean samePosition = existing.getLearningPathId().equals(learningPathId) && existing.getStepOrder().equals(stepOrder);
        if (!samePosition && stepRepository.existsByLearningPathIdAndStepOrder(learningPathId, stepOrder)) {
            throw new ConflictException("Step order " + stepOrder + " already exists in learning path " + learningPathId);
        }

        existing.setLearningPathId(learningPathId);
        existing.setActivityId(activityId);
        existing.setStepOrder(stepOrder);
        existing.setUnlockCondition(unlockCondition);
        existing.setVisualMetadata(visualMetadata);
        existing.setUpdatedAt(LocalDateTime.now());

        return stepRepository.save(existing);
    }
}
