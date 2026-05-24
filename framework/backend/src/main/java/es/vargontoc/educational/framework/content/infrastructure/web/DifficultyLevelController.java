package es.vargontoc.educational.framework.content.infrastructure.web;

import es.vargontoc.educational.framework.content.infrastructure.dto.CreateDifficultyLevelRequest;
import es.vargontoc.educational.framework.content.infrastructure.dto.DifficultyLevelResponse;
import es.vargontoc.educational.framework.content.infrastructure.dto.UpdateDifficultyLevelRequest;
import es.vargontoc.educational.framework.content.model.DifficultyCode;
import es.vargontoc.educational.framework.content.model.DifficultyLevel;
import es.vargontoc.educational.framework.content.ports.in.DifficultyLevelUseCase;
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
@RequestMapping("/api/v1/dev/content/difficulty-levels")
public class DifficultyLevelController {

    private final DifficultyLevelUseCase difficultyLevelUseCase;

    public DifficultyLevelController(DifficultyLevelUseCase difficultyLevelUseCase) {
        this.difficultyLevelUseCase = difficultyLevelUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DifficultyLevelResponse>> createDifficultyLevel(
        @RequestBody CreateDifficultyLevelRequest request
    ) {
        var difficultyLevel = difficultyLevelUseCase.createDifficultyLevel(
            request.activityId(),
            DifficultyCode.valueOf(request.difficultyCode()),
            request.engineParams(),
            request.adaptiveThresholdConfig()
        );
        return ResponseEntity.status(201).body(ApiResponse.created(toResponse(difficultyLevel)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DifficultyLevelResponse>>> listDifficultyLevels(
        @RequestParam Long activityId
    ) {
        var difficultyLevels = difficultyLevelUseCase.listByActivity(activityId).stream()
            .map(DifficultyLevelController::toResponse)
            .toList();
        return ResponseEntity.ok(ApiResponse.ok(difficultyLevels));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DifficultyLevelResponse>> updateDifficultyLevel(
        @PathVariable Long id,
        @RequestBody UpdateDifficultyLevelRequest request
    ) {
        var difficultyLevel = difficultyLevelUseCase.updateDifficultyLevel(
            id,
            DifficultyCode.valueOf(request.difficultyCode()),
            request.engineParams(),
            request.adaptiveThresholdConfig()
        );
        return ResponseEntity.ok(ApiResponse.ok(toResponse(difficultyLevel)));
    }

    private static DifficultyLevelResponse toResponse(DifficultyLevel difficultyLevel) {
        return new DifficultyLevelResponse(
            difficultyLevel.getId(),
            difficultyLevel.getActivityId(),
            difficultyLevel.getDifficultyCode().name(),
            difficultyLevel.getEngineParams(),
            difficultyLevel.getAdaptiveThresholdConfig(),
            difficultyLevel.getCreatedAt(),
            difficultyLevel.getUpdatedAt()
        );
    }
}
