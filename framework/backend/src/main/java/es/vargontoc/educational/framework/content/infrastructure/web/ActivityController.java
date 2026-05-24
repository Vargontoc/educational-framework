package es.vargontoc.educational.framework.content.infrastructure.web;

import es.vargontoc.educational.framework.content.infrastructure.dto.ActivityResponse;
import es.vargontoc.educational.framework.content.infrastructure.dto.CreateActivityRequest;
import es.vargontoc.educational.framework.content.infrastructure.dto.UpdateActivityRequest;
import es.vargontoc.educational.framework.content.model.Activity;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.ports.in.ActivityUseCase;
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
@RequestMapping("/api/v1/dev/content/activities")
public class ActivityController {

    private final ActivityUseCase activityUseCase;

    public ActivityController(ActivityUseCase activityUseCase) {
        this.activityUseCase = activityUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ActivityResponse>> createActivity(@RequestBody CreateActivityRequest request) {
        var activity = activityUseCase.createActivity(
            request.name(),
            request.description(),
            request.gameEngineType(),
            ContentStatus.valueOf(request.status()),
            request.minAge(),
            request.maxAge(),
            request.topicIds()
        );
        return ResponseEntity.status(201).body(ApiResponse.created(toResponse(activity)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ActivityResponse>>> listActivities(
        @RequestParam(required = false) Long topicId
    ) {
        List<Activity> activities = topicId != null
            ? activityUseCase.listActivitiesByTopic(topicId)
            : activityUseCase.listActivities();

        var responses = activities.stream()
            .map(ActivityController::toResponse)
            .toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ActivityResponse>> getActivity(@PathVariable Long id) {
        var activity = activityUseCase.getActivity(id);
        return ResponseEntity.ok(ApiResponse.ok(toResponse(activity)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ActivityResponse>> updateActivity(
        @PathVariable Long id,
        @RequestBody UpdateActivityRequest request
    ) {
        var activity = activityUseCase.updateActivity(
            id,
            request.name(),
            request.description(),
            request.gameEngineType(),
            ContentStatus.valueOf(request.status()),
            request.minAge(),
            request.maxAge(),
            request.topicIds()
        );
        return ResponseEntity.ok(ApiResponse.ok(toResponse(activity)));
    }

    private static ActivityResponse toResponse(Activity activity) {
        return new ActivityResponse(
            activity.getId(),
            activity.getName(),
            activity.getDescription(),
            activity.getGameEngineType(),
            activity.getStatus().name(),
            activity.getMinAge(),
            activity.getMaxAge(),
            activity.getTopicIds(),
            activity.getCreatedAt(),
            activity.getUpdatedAt()
        );
    }
}
