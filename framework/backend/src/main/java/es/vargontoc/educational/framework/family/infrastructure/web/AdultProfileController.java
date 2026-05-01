package es.vargontoc.educational.framework.family.infrastructure.web;

import es.vargontoc.educational.framework.family.infrastructure.dto.AdultProfileResponse;
import es.vargontoc.educational.framework.family.infrastructure.dto.CreateAdultProfileRequest;
import es.vargontoc.educational.framework.family.infrastructure.dto.UpdateAdultProfileRequest;
import es.vargontoc.educational.framework.family.model.AdultProfile;
import es.vargontoc.educational.framework.family.ports.in.AdultProfileUseCase;
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

@RestController
@RequestMapping("/api/v1/family/adults")
public class AdultProfileController {

    private final FamilyUseCase familyUseCase;
    private final AdultProfileUseCase adultProfileUseCase;

    public AdultProfileController(FamilyUseCase familyUseCase, AdultProfileUseCase adultProfileUseCase) {
        this.familyUseCase = familyUseCase;
        this.adultProfileUseCase = adultProfileUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdultProfileResponse>> createAdult(@RequestBody CreateAdultProfileRequest request) {
        Long familyId = familyUseCase.getFamily().getId();
        var adult = adultProfileUseCase.createAdult(
            familyId,
            request.name(),
            request.birthday(),
            request.avatar()
        );
        return ResponseEntity.status(201).body(ApiResponse.created(toResponse(adult)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AdultProfileResponse>>> getAllAdults() {
        var data = adultProfileUseCase.getAllAdults().stream()
            .map(AdultProfileController::toResponse)
            .toList();
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdultProfileResponse>> getAdult(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(toResponse(adultProfileUseCase.getAdult(id))));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<AdultProfileResponse>> updateAdult(
        @PathVariable Long id,
        @RequestBody UpdateAdultProfileRequest request
    ) {
        var updated = adultProfileUseCase.updateAdult(id, request.name(), request.birthday(), request.avatar());
        return ResponseEntity.ok(ApiResponse.ok(toResponse(updated)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdult(@PathVariable Long id) {
        adultProfileUseCase.deleteAdult(id);
        return ResponseEntity.noContent().build();
    }

    private static AdultProfileResponse toResponse(AdultProfile source) {
        return new AdultProfileResponse(
            source.getId(),
            source.getFamilyId(),
            source.getName(),
            source.getBirthday(),
            source.getAvatar(),
            source.getCreatedAt(),
            source.getUpdatedAt()
        );
    }
}
