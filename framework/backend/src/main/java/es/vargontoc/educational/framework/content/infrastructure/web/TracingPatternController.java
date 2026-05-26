package es.vargontoc.educational.framework.content.infrastructure.web;

import es.vargontoc.educational.framework.content.infrastructure.dto.CreateTracingPatternRequest;
import es.vargontoc.educational.framework.content.infrastructure.dto.TracingPatternResponse;
import es.vargontoc.educational.framework.content.infrastructure.dto.UpdateTracingPatternRequest;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.TracingPattern;
import es.vargontoc.educational.framework.content.ports.in.TracingPatternUseCase;
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
@RequestMapping("/api/v1/dev/content/tracing-patterns")
public class TracingPatternController {

    private final TracingPatternUseCase tracingPatternUseCase;

    public TracingPatternController(TracingPatternUseCase tracingPatternUseCase) {
        this.tracingPatternUseCase = tracingPatternUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TracingPatternResponse>> createTracingPattern(@RequestBody CreateTracingPatternRequest request) {
        var tracingPattern = tracingPatternUseCase.createTracingPattern(
            request.topicId(),
            request.name(),
            request.description(),
            request.points(),
            ContentStatus.valueOf(request.status())
        );
        return ResponseEntity.status(201).body(ApiResponse.created(toResponse(tracingPattern)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TracingPatternResponse>>> listTracingPatterns(
        @RequestParam(required = false) Long topicId
    ) {
        List<TracingPattern> tracingPatterns;
        if (topicId != null) {
            tracingPatterns = tracingPatternUseCase.listTracingPatternsByTopic(topicId);
        } else {
            tracingPatterns = tracingPatternUseCase.listTracingPatterns();
        }

        var responses = tracingPatterns.stream()
            .map(TracingPatternController::toResponse)
            .toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TracingPatternResponse>> getTracingPattern(@PathVariable Long id) {
        var tracingPattern = tracingPatternUseCase.getTracingPattern(id);
        return ResponseEntity.ok(ApiResponse.ok(toResponse(tracingPattern)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TracingPatternResponse>> updateTracingPattern(
        @PathVariable Long id,
        @RequestBody UpdateTracingPatternRequest request
    ) {
        var tracingPattern = tracingPatternUseCase.updateTracingPattern(
            id,
            request.topicId(),
            request.name(),
            request.description(),
            request.points(),
            ContentStatus.valueOf(request.status())
        );
        return ResponseEntity.ok(ApiResponse.ok(toResponse(tracingPattern)));
    }

    private static TracingPatternResponse toResponse(TracingPattern tracingPattern) {
        return new TracingPatternResponse(
            tracingPattern.getId(),
            tracingPattern.getTopicId(),
            tracingPattern.getName(),
            tracingPattern.getDescription(),
            tracingPattern.getPoints(),
            tracingPattern.getStatus().name(),
            tracingPattern.getCreatedAt(),
            tracingPattern.getUpdatedAt()
        );
    }
}
