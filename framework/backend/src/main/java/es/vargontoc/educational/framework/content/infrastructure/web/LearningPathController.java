package es.vargontoc.educational.framework.content.infrastructure.web;

import es.vargontoc.educational.framework.content.infrastructure.dto.CreateLearningPathRequest;
import es.vargontoc.educational.framework.content.infrastructure.dto.LearningPathResponse;
import es.vargontoc.educational.framework.content.infrastructure.dto.UpdateLearningPathRequest;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.LearningPath;
import es.vargontoc.educational.framework.content.ports.in.LearningPathUseCase;
import es.vargontoc.educational.framework.shared.api.ApiResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Profile("dev")
@RequestMapping("/api/v1/dev/content/learning-paths")
public class LearningPathController {

    private final LearningPathUseCase learningPathUseCase;

    public LearningPathController(LearningPathUseCase learningPathUseCase) {
        this.learningPathUseCase = learningPathUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LearningPathResponse>> createLearningPath(@RequestBody CreateLearningPathRequest request) {
        var learningPath = learningPathUseCase.createLearningPath(
            request.name(),
            request.description(),
            request.minAge(),
            request.maxAge(),
            request.locale(),
            ContentStatus.valueOf(request.status())
        );
        return ResponseEntity.status(201).body(ApiResponse.created(toResponse(learningPath)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LearningPathResponse>>> listLearningPaths(
        @RequestParam(required = false) String status
    ) {
        List<LearningPath> learningPaths;
        if (status != null) {
            learningPaths = learningPathUseCase.listLearningPathsByStatus(ContentStatus.valueOf(status));
        } else {
            learningPaths = learningPathUseCase.listLearningPaths();
        }

        var responses = learningPaths.stream()
            .map(LearningPathController::toResponse)
            .toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LearningPathResponse>> getLearningPath(@PathVariable Long id) {
        var learningPath = learningPathUseCase.getLearningPath(id);
        return ResponseEntity.ok(ApiResponse.ok(toResponse(learningPath)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LearningPathResponse>> updateLearningPath(
        @PathVariable Long id,
        @RequestBody UpdateLearningPathRequest request
    ) {
        var learningPath = learningPathUseCase.updateLearningPath(
            id,
            request.name(),
            request.description(),
            request.minAge(),
            request.maxAge(),
            request.locale(),
            ContentStatus.valueOf(request.status())
        );
        return ResponseEntity.ok(ApiResponse.ok(toResponse(learningPath)));
    }

    private static LearningPathResponse toResponse(LearningPath learningPath) {
        return new LearningPathResponse(
            learningPath.getId(),
            learningPath.getName(),
            learningPath.getDescription(),
            learningPath.getMinAge(),
            learningPath.getMaxAge(),
            learningPath.getLocale(),
            learningPath.getStatus().name(),
            learningPath.getCreatedAt(),
            learningPath.getUpdatedAt()
        );
    }
}
