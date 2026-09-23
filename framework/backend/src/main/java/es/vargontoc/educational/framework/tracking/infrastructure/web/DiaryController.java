package es.vargontoc.educational.framework.tracking.infrastructure.web;

import es.vargontoc.educational.framework.family.ports.in.ChildProfileUseCase;
import es.vargontoc.educational.framework.family.ports.in.FamilyUseCase;
import es.vargontoc.educational.framework.shared.api.ApiResponse;
import es.vargontoc.educational.framework.shared.exception.ForbiddenException;
import es.vargontoc.educational.framework.tracking.infrastructure.dto.AbandonmentSignalResponse;
import es.vargontoc.educational.framework.tracking.infrastructure.dto.DiaryActivityResponse;
import es.vargontoc.educational.framework.tracking.infrastructure.dto.DiarySummaryResponse;
import es.vargontoc.educational.framework.tracking.model.DiaryPeriod;
import es.vargontoc.educational.framework.tracking.service.DiaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/diary/children/{childProfileId}")
public class DiaryController {

    private final DiaryService diaryService;
    private final FamilyUseCase familyUseCase;
    private final ChildProfileUseCase childProfileUseCase;

    public DiaryController(
            DiaryService diaryService,
            FamilyUseCase familyUseCase,
            ChildProfileUseCase childProfileUseCase) {
        this.diaryService = diaryService;
        this.familyUseCase = familyUseCase;
        this.childProfileUseCase = childProfileUseCase;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DiarySummaryResponse>> getSummary(
            @PathVariable Long childProfileId,
            @RequestParam(defaultValue = "WEEK") DiaryPeriod period) {
        verifyChildBelongsToFamily(childProfileId);
        var summary = diaryService.getSummary(childProfileId, period);
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }

    @GetMapping("/activities")
    public ResponseEntity<ApiResponse<List<DiaryActivityResponse>>> getActivities(
            @PathVariable Long childProfileId,
            @RequestParam(defaultValue = "WEEK") DiaryPeriod period) {
        verifyChildBelongsToFamily(childProfileId);
        var activities = diaryService.getActivities(childProfileId, period);
        return ResponseEntity.ok(ApiResponse.ok(activities));
    }

    @GetMapping("/abandonment-signal")
    public ResponseEntity<ApiResponse<AbandonmentSignalResponse>> getAbandonmentSignal(
            @PathVariable Long childProfileId,
            @RequestParam Long activityId) {
        verifyChildBelongsToFamily(childProfileId);
        var signal = diaryService.getAbandonmentSignal(childProfileId, activityId);
        if (signal == null) {
            return ResponseEntity.ok(ApiResponse.ok(null));
        }
        return ResponseEntity.ok(ApiResponse.ok(signal));
    }

    private void verifyChildBelongsToFamily(Long childProfileId) {
        var familyId = familyUseCase.getFamily().getId();
        var child = childProfileUseCase.getChild(childProfileId);
        if (!familyId.equals(child.getFamilyId())) {
            throw new ForbiddenException("Child profile does not belong to the current family");
        }
    }
}
