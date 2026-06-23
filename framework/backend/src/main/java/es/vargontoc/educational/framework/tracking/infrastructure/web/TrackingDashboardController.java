package es.vargontoc.educational.framework.tracking.infrastructure.web;

import es.vargontoc.educational.framework.family.model.ChildProfile;
import es.vargontoc.educational.framework.family.ports.in.ChildProfileUseCase;
import es.vargontoc.educational.framework.family.ports.in.FamilyUseCase;
import es.vargontoc.educational.framework.shared.api.ApiResponse;
import es.vargontoc.educational.framework.shared.exception.ForbiddenException;
import es.vargontoc.educational.framework.tracking.infrastructure.dto.ChildTrackingSummaryResponse;
import es.vargontoc.educational.framework.tracking.infrastructure.dto.DifficultyEvolutionResponse;
import es.vargontoc.educational.framework.tracking.infrastructure.dto.ResponseTimeMetricsResponse;
import es.vargontoc.educational.framework.tracking.model.ActivityEngagementSummaryResult;
import es.vargontoc.educational.framework.tracking.model.ChildAchievement;
import es.vargontoc.educational.framework.tracking.model.ChildLearningProgress;
import es.vargontoc.educational.framework.tracking.ports.in.GetActivityEngagementSummaryUseCase;
import es.vargontoc.educational.framework.tracking.service.TrackingDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tracking/children")
public class TrackingDashboardController {

    private final TrackingDashboardService dashboardService;
    private final GetActivityEngagementSummaryUseCase engagementSummaryUseCase;
    private final FamilyUseCase familyUseCase;
    private final ChildProfileUseCase childProfileUseCase;

    public TrackingDashboardController(
            TrackingDashboardService dashboardService,
            GetActivityEngagementSummaryUseCase engagementSummaryUseCase,
            FamilyUseCase familyUseCase,
            ChildProfileUseCase childProfileUseCase) {
        this.dashboardService = dashboardService;
        this.engagementSummaryUseCase = engagementSummaryUseCase;
        this.familyUseCase = familyUseCase;
        this.childProfileUseCase = childProfileUseCase;
    }

    @GetMapping("/{childProfileId}/summary")
    public ResponseEntity<ApiResponse<ChildTrackingSummaryResponse>> getChildTrackingSummary(@PathVariable Long childProfileId) {
        verifyChildBelongsToFamily(childProfileId);
        var summary = dashboardService.getChildTrackingSummary(childProfileId);
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }

    @GetMapping("/{childProfileId}/activities")
    public ResponseEntity<ApiResponse<List<?>>> getActivityPerformance(@PathVariable Long childProfileId) {
        verifyChildBelongsToFamily(childProfileId);
        var activities = dashboardService.getActivityPerformance(childProfileId);
        return ResponseEntity.ok(ApiResponse.ok(activities));
    }

    @GetMapping("/{childProfileId}/topics")
    public ResponseEntity<ApiResponse<List<?>>> getTopicPerformance(@PathVariable Long childProfileId) {
        verifyChildBelongsToFamily(childProfileId);
        var topics = dashboardService.getTopicPerformance(childProfileId);
        return ResponseEntity.ok(ApiResponse.ok(topics));
    }

    @GetMapping("/{childProfileId}/difficulty")
    public ResponseEntity<ApiResponse<DifficultyEvolutionResponse>> getDifficultyEvolution(
            @PathVariable Long childProfileId,
            @RequestParam(required = false) Long activityId) {
        verifyChildBelongsToFamily(childProfileId);
        var evolution = dashboardService.getDifficultyEvolution(childProfileId, activityId);
        return ResponseEntity.ok(ApiResponse.ok(evolution));
    }

    @GetMapping("/{childProfileId}/response-time")
    public ResponseEntity<ApiResponse<ResponseTimeMetricsResponse>> getResponseTimeMetrics(@PathVariable Long childProfileId) {
        verifyChildBelongsToFamily(childProfileId);
        var metrics = dashboardService.getResponseTimeMetrics(childProfileId);
        return ResponseEntity.ok(ApiResponse.ok(metrics));
    }

    @GetMapping("/{childProfileId}/achievements")
    public ResponseEntity<ApiResponse<List<ChildAchievement>>> getChildAchievements(
            @PathVariable Long childProfileId,
            @RequestParam(required = false) Long activityId) {
        verifyChildBelongsToFamily(childProfileId);
        var achievements = dashboardService.getChildAchievements(childProfileId, activityId);
        return ResponseEntity.ok(ApiResponse.ok(achievements));
    }

    @GetMapping("/{childProfileId}/learning-progress")
    public ResponseEntity<ApiResponse<List<ChildLearningProgress>>> getChildLearningProgress(
            @PathVariable Long childProfileId,
            @RequestParam(required = false) Long learningPathId) {
        verifyChildBelongsToFamily(childProfileId);
        var progress = dashboardService.getChildLearningProgress(childProfileId, learningPathId);
        return ResponseEntity.ok(ApiResponse.ok(progress));
    }

    @GetMapping("/{childProfileId}/engagement")
    public ResponseEntity<ApiResponse<ActivityEngagementSummaryResult>> getActivityEngagementSummary(
            @PathVariable Long childProfileId) {
        verifyChildBelongsToFamily(childProfileId);
        var result = engagementSummaryUseCase.getActivityEngagementSummary(childProfileId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    private void verifyChildBelongsToFamily(Long childProfileId) {
        var familyId = familyUseCase.getFamily().getId();
        var child = childProfileUseCase.getChild(childProfileId);
        if (!familyId.equals(child.getFamilyId())) {
            throw new ForbiddenException("Child profile does not belong to the current family");
        }
    }
}
