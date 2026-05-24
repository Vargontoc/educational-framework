package es.vargontoc.educational.framework.content.infrastructure.web;

import es.vargontoc.educational.framework.content.infrastructure.dto.ActivityResourceResponse;
import es.vargontoc.educational.framework.content.infrastructure.dto.CreateActivityResourceRequest;
import es.vargontoc.educational.framework.content.infrastructure.dto.UpdateActivityResourceRequest;
import es.vargontoc.educational.framework.content.model.ActivityResource;
import es.vargontoc.educational.framework.content.model.ResourceType;
import es.vargontoc.educational.framework.content.ports.in.ActivityResourceUseCase;
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
@RequestMapping("/api/v1/dev/content/activity-resources")
public class ActivityResourceController {

    private final ActivityResourceUseCase activityResourceUseCase;

    public ActivityResourceController(ActivityResourceUseCase activityResourceUseCase) {
        this.activityResourceUseCase = activityResourceUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ActivityResourceResponse>> createResource(
        @RequestBody CreateActivityResourceRequest request
    ) {
        var resource = activityResourceUseCase.createResource(
            request.activityId(),
            request.topicId(),
            ResourceType.valueOf(request.resourceType()),
            request.path(),
            request.metadata()
        );
        return ResponseEntity.status(201).body(ApiResponse.created(toResponse(resource)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ActivityResourceResponse>>> listResources(
        @RequestParam Long activityId
    ) {
        var resources = activityResourceUseCase.listByActivity(activityId).stream()
            .map(ActivityResourceController::toResponse)
            .toList();
        return ResponseEntity.ok(ApiResponse.ok(resources));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ActivityResourceResponse>> updateResource(
        @PathVariable Long id,
        @RequestBody UpdateActivityResourceRequest request
    ) {
        var resource = activityResourceUseCase.updateResource(
            id,
            request.topicId(),
            ResourceType.valueOf(request.resourceType()),
            request.path(),
            request.metadata()
        );
        return ResponseEntity.ok(ApiResponse.ok(toResponse(resource)));
    }

    private static ActivityResourceResponse toResponse(ActivityResource resource) {
        return new ActivityResourceResponse(
            resource.getId(),
            resource.getActivityId(),
            resource.getTopicId(),
            resource.getResourceType().name(),
            resource.getPath(),
            resource.getMetadata(),
            resource.getCreatedAt(),
            resource.getUpdatedAt()
        );
    }
}
