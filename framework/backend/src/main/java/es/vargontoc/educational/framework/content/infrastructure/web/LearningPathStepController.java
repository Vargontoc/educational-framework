package es.vargontoc.educational.framework.content.infrastructure.web;

import es.vargontoc.educational.framework.content.infrastructure.dto.CreateLearningPathStepRequest;
import es.vargontoc.educational.framework.content.infrastructure.dto.LearningPathStepResponse;
import es.vargontoc.educational.framework.content.infrastructure.dto.UpdateLearningPathStepRequest;
import es.vargontoc.educational.framework.content.model.LearningPathStep;
import es.vargontoc.educational.framework.content.ports.in.LearningPathStepUseCase;
import es.vargontoc.educational.framework.shared.api.ApiResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Profile("dev")
@RequestMapping("/api/v1/dev/content/learning-paths/{learningPathId}/steps")
public class LearningPathStepController {

    private final LearningPathStepUseCase stepUseCase;

    public LearningPathStepController(LearningPathStepUseCase stepUseCase) {
        this.stepUseCase = stepUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LearningPathStepResponse>> createStep(
        @PathVariable Long learningPathId,
        @RequestBody CreateLearningPathStepRequest request
    ) {
        var step = stepUseCase.createStep(
            learningPathId,
            request.activityId(),
            request.stepOrder(),
            request.unlockCondition(),
            request.visualMetadata()
        );
        return ResponseEntity.status(201).body(ApiResponse.created(toResponse(step)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LearningPathStepResponse>>> listSteps(@PathVariable Long learningPathId) {
        var steps = stepUseCase.listStepsByLearningPath(learningPathId);
        var responses = steps.stream()
            .map(LearningPathStepController::toResponse)
            .toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/{stepId}")
    public ResponseEntity<ApiResponse<LearningPathStepResponse>> getStep(
        @PathVariable Long learningPathId,
        @PathVariable Long stepId
    ) {
        var step = stepUseCase.getStep(stepId);
        return ResponseEntity.ok(ApiResponse.ok(toResponse(step)));
    }

    @PutMapping("/{stepId}")
    public ResponseEntity<ApiResponse<LearningPathStepResponse>> updateStep(
        @PathVariable Long learningPathId,
        @PathVariable Long stepId,
        @RequestBody UpdateLearningPathStepRequest request
    ) {
        var step = stepUseCase.updateStep(
            stepId,
            learningPathId,
            request.activityId(),
            request.stepOrder(),
            request.unlockCondition(),
            request.visualMetadata()
        );
        return ResponseEntity.ok(ApiResponse.ok(toResponse(step)));
    }

    private static LearningPathStepResponse toResponse(LearningPathStep step) {
        return new LearningPathStepResponse(
            step.getId(),
            step.getLearningPathId(),
            step.getActivityId(),
            step.getStepOrder(),
            step.getUnlockCondition(),
            step.getVisualMetadata(),
            step.getCreatedAt(),
            step.getUpdatedAt()
        );
    }
}
