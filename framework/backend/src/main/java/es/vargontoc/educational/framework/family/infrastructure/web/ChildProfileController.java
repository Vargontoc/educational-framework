package es.vargontoc.educational.framework.family.infrastructure.web;

import es.vargontoc.educational.framework.family.infrastructure.dto.ChildProfileResponse;
import es.vargontoc.educational.framework.family.infrastructure.dto.CreateChildProfileRequest;
import es.vargontoc.educational.framework.family.infrastructure.dto.UpdateChildProfileRequest;
import es.vargontoc.educational.framework.family.model.ChildProfile;
import es.vargontoc.educational.framework.family.ports.in.ChildProfileUseCase;
import es.vargontoc.educational.framework.family.ports.in.FamilyUseCase;
import es.vargontoc.educational.framework.shared.api.ApiResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/v1/family/children")
public class ChildProfileController {

    private final FamilyUseCase familyUseCase;
    private final ChildProfileUseCase childProfileUseCase;

    public ChildProfileController(FamilyUseCase familyUseCase, ChildProfileUseCase childProfileUseCase) {
        this.familyUseCase = familyUseCase;
        this.childProfileUseCase = childProfileUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ChildProfileResponse>> createChild(@RequestBody CreateChildProfileRequest request) {
        Long familyId = familyUseCase.getFamily().getId();
        var child = childProfileUseCase.createChild(
            familyId,
            request.name(),
            request.birthday(),
            request.avatar(),
            request.ttsEnabled(),
            request.agentEnabled()
        );
        return ResponseEntity.status(201).body(ApiResponse.created(toResponse(child)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ChildProfileResponse>>> getAllChildren() {
        var data = childProfileUseCase.getAllChildren().stream()
            .map(ChildProfileController::toResponse)
            .toList();
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ChildProfileResponse>> getChild(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(toResponse(childProfileUseCase.getChild(id))));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ChildProfileResponse>> updateChild(
        @PathVariable Long id,
        @RequestBody UpdateChildProfileRequest request
    ) {
        var existing = childProfileUseCase.getChild(id);
        var updated = childProfileUseCase.updateChild(
            id,
            request.name(),
            request.birthday(),
            request.avatar(),
            request.ttsEnabled() != null ? request.ttsEnabled() : existing.isTtsEnabled(),
            request.agentEnabled() != null ? request.agentEnabled() : existing.isAgentEnabled()
        );
        return ResponseEntity.ok(ApiResponse.ok(toResponse(updated)));
    }

    @PutMapping("/activation/{id}")
    public ResponseEntity<Void> changeStateChild(@PathVariable Long id) {
        childProfileUseCase.changeActiveState(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChild(@PathVariable Long id) 
    {  
        childProfileUseCase.deleteChild(id);
        return ResponseEntity.noContent().build();
    }

    private static ChildProfileResponse toResponse(ChildProfile source) {
        return new ChildProfileResponse(
            source.getId(),
            source.getFamilyId(),
            source.getName(),
            source.isActive(),
            source.getBirthday(),
            source.getAvatar(),
            source.isTtsEnabled(),
            source.isAgentEnabled(),
            source.getCreatedAt(),
            source.getUpdatedAt()
        );
    }
}
