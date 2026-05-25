package es.vargontoc.educational.framework.content.infrastructure.web;

import es.vargontoc.educational.framework.content.infrastructure.dto.CreateCuriosityRequest;
import es.vargontoc.educational.framework.content.infrastructure.dto.CuriosityResponse;
import es.vargontoc.educational.framework.content.infrastructure.dto.UpdateCuriosityRequest;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.Curiosity;
import es.vargontoc.educational.framework.content.ports.in.CuriosityUseCase;
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
@RequestMapping("/api/v1/dev/content/curiosities")
public class CuriosityController {

    private final CuriosityUseCase curiosityUseCase;

    public CuriosityController(CuriosityUseCase curiosityUseCase) {
        this.curiosityUseCase = curiosityUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CuriosityResponse>> createCuriosity(@RequestBody CreateCuriosityRequest request) {
        var curiosity = curiosityUseCase.createCuriosity(
            request.text(),
            request.topicId(),
            request.minAge(),
            request.maxAge(),
            request.tags(),
            request.locale(),
            request.phoneticHint(),
            ContentStatus.valueOf(request.status())
        );
        return ResponseEntity.status(201).body(ApiResponse.created(toResponse(curiosity)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CuriosityResponse>>> listCuriosities(
        @RequestParam(required = false) Long topicId,
        @RequestParam(required = false) Integer age,
        @RequestParam(required = false) String locale
    ) {
        List<Curiosity> curiosities;
        if (age != null || locale != null) {
            curiosities = curiosityUseCase.listActiveCuriositiesByFilters(topicId, age, locale);
        } else if (topicId != null) {
            curiosities = curiosityUseCase.listCuriositiesByTopic(topicId);
        } else {
            curiosities = curiosityUseCase.listCuriosities();
        }

        var responses = curiosities.stream()
            .map(CuriosityController::toResponse)
            .toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CuriosityResponse>> getCuriosity(@PathVariable Long id) {
        var curiosity = curiosityUseCase.getCuriosity(id);
        return ResponseEntity.ok(ApiResponse.ok(toResponse(curiosity)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CuriosityResponse>> updateCuriosity(
        @PathVariable Long id,
        @RequestBody UpdateCuriosityRequest request
    ) {
        var curiosity = curiosityUseCase.updateCuriosity(
            id,
            request.text(),
            request.topicId(),
            request.minAge(),
            request.maxAge(),
            request.tags(),
            request.locale(),
            request.phoneticHint(),
            ContentStatus.valueOf(request.status())
        );
        return ResponseEntity.ok(ApiResponse.ok(toResponse(curiosity)));
    }

    private static CuriosityResponse toResponse(Curiosity curiosity) {
        return new CuriosityResponse(
            curiosity.getId(),
            curiosity.getText(),
            curiosity.getTopicId(),
            curiosity.getMinAge(),
            curiosity.getMaxAge(),
            curiosity.getTags(),
            curiosity.getLocale(),
            curiosity.getPhoneticHint(),
            curiosity.getStatus().name(),
            curiosity.getCreatedAt(),
            curiosity.getUpdatedAt()
        );
    }
}
