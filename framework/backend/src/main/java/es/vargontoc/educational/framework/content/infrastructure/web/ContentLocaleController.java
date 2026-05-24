package es.vargontoc.educational.framework.content.infrastructure.web;

import es.vargontoc.educational.framework.content.infrastructure.dto.ContentLocaleResponse;
import es.vargontoc.educational.framework.content.infrastructure.dto.CreateContentLocaleRequest;
import es.vargontoc.educational.framework.content.infrastructure.dto.UpdateContentLocaleRequest;
import es.vargontoc.educational.framework.content.model.ContentLocale;
import es.vargontoc.educational.framework.content.model.EntityType;
import es.vargontoc.educational.framework.content.ports.in.ContentLocaleUseCase;
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
@RequestMapping("/api/v1/dev/content/locales")
public class ContentLocaleController {

    private final ContentLocaleUseCase contentLocaleUseCase;

    public ContentLocaleController(ContentLocaleUseCase contentLocaleUseCase) {
        this.contentLocaleUseCase = contentLocaleUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ContentLocaleResponse>> createLocale(
        @RequestBody CreateContentLocaleRequest request
    ) {
        var locale = contentLocaleUseCase.createLocale(
            EntityType.valueOf(request.entityType()),
            request.entityId(),
            request.localeCode(),
            request.name(),
            request.description()
        );
        return ResponseEntity.status(201).body(ApiResponse.created(toResponse(locale)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ContentLocaleResponse>>> listLocales(
        @RequestParam String entityType,
        @RequestParam Long entityId
    ) {
        var locales = contentLocaleUseCase.listByEntity(
            EntityType.valueOf(entityType),
            entityId
        ).stream()
            .map(ContentLocaleController::toResponse)
            .toList();
        return ResponseEntity.ok(ApiResponse.ok(locales));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ContentLocaleResponse>> updateLocale(
        @PathVariable Long id,
        @RequestBody UpdateContentLocaleRequest request
    ) {
        var locale = contentLocaleUseCase.updateLocale(
            id,
            request.name(),
            request.description()
        );
        return ResponseEntity.ok(ApiResponse.ok(toResponse(locale)));
    }

    private static ContentLocaleResponse toResponse(ContentLocale locale) {
        return new ContentLocaleResponse(
            locale.getId(),
            locale.getEntityType().name(),
            locale.getEntityId(),
            locale.getLocaleCode(),
            locale.getName(),
            locale.getDescription(),
            locale.getCreatedAt(),
            locale.getUpdatedAt()
        );
    }
}
