package es.vargontoc.educational.framework.family.infrastructure.web;

import es.vargontoc.educational.framework.family.infrastructure.dto.CreateFamilyRequest;
import es.vargontoc.educational.framework.family.infrastructure.dto.FamilyResponse;
import es.vargontoc.educational.framework.family.infrastructure.dto.UpdateFamilyRequest;
import es.vargontoc.educational.framework.family.model.Family;
import es.vargontoc.educational.framework.family.ports.in.FamilyUseCase;
import es.vargontoc.educational.framework.shared.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/family")
public class FamilyController {

    private final FamilyUseCase familyUseCase;

    public FamilyController(FamilyUseCase familyUseCase) {
        this.familyUseCase = familyUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FamilyResponse>> createFamily(@RequestBody CreateFamilyRequest request) {
        var family = familyUseCase.createFamily(
            request.name(),
            request.pin(),
            request.ttsEnabled(),
            request.agentEnabled()
        );
        return ResponseEntity.status(201).body(ApiResponse.created(toResponse(family)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<FamilyResponse>> getFamily() {
        return ResponseEntity.ok(ApiResponse.ok(toResponse(familyUseCase.getFamily())));
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<FamilyResponse>> updateFamily(@RequestBody UpdateFamilyRequest request) {
        var existing = familyUseCase.getFamily();
        var family = familyUseCase.updateFamily(
            request.name(),
            request.pin(),
            request.ttsEnabled() != null ? request.ttsEnabled() : existing.isTtsEnabled(),
            request.agentEnabled() != null ? request.agentEnabled() : existing.isAgentEnabled()
        );
        return ResponseEntity.ok(ApiResponse.ok(toResponse(family)));
    }

    private static FamilyResponse toResponse(Family family) {
        return new FamilyResponse(
            family.getId(),
            family.getName(),
            family.isTtsEnabled(),
            family.isAgentEnabled(),
            family.getCreatedAt(),
            family.getUpdatedAt()
        );
    }
}
