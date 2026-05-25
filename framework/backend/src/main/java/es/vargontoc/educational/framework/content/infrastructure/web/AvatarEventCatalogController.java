package es.vargontoc.educational.framework.content.infrastructure.web;

import es.vargontoc.educational.framework.content.infrastructure.dto.AvatarEventCatalogResponse;
import es.vargontoc.educational.framework.content.infrastructure.dto.CreateAvatarEventCatalogRequest;
import es.vargontoc.educational.framework.content.infrastructure.dto.UpdateAvatarEventCatalogRequest;
import es.vargontoc.educational.framework.content.model.AvatarEventCatalog;
import es.vargontoc.educational.framework.content.model.AvatarEventType;
import es.vargontoc.educational.framework.content.model.AvatarTone;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.ports.in.AvatarEventCatalogUseCase;
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
@RequestMapping("/api/v1/dev/content/avatar-events")
public class AvatarEventCatalogController {

    private final AvatarEventCatalogUseCase avatarEventCatalogUseCase;

    public AvatarEventCatalogController(AvatarEventCatalogUseCase avatarEventCatalogUseCase) {
        this.avatarEventCatalogUseCase = avatarEventCatalogUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AvatarEventCatalogResponse>> createAvatarEvent(@RequestBody CreateAvatarEventCatalogRequest request) {
        var event = avatarEventCatalogUseCase.createAvatarEvent(
            AvatarEventType.valueOf(request.eventType()),
            AvatarTone.valueOf(request.tone()),
            request.locale(),
            request.messageText(),
            ContentStatus.valueOf(request.status())
        );
        return ResponseEntity.status(201).body(ApiResponse.created(toResponse(event)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AvatarEventCatalogResponse>>> listAvatarEvents(
        @RequestParam(required = false) String eventType,
        @RequestParam(required = false) String tone,
        @RequestParam(required = false) String locale
    ) {
        List<AvatarEventCatalog> events;
        if (eventType != null && tone != null && locale != null) {
            events = avatarEventCatalogUseCase.listActiveAvatarEventsByFilters(
                AvatarEventType.valueOf(eventType),
                AvatarTone.valueOf(tone),
                locale
            );
        } else if (eventType != null) {
            events = avatarEventCatalogUseCase.listAvatarEventsByEventType(AvatarEventType.valueOf(eventType));
        } else {
            events = avatarEventCatalogUseCase.listAvatarEvents();
        }

        var responses = events.stream()
            .map(AvatarEventCatalogController::toResponse)
            .toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AvatarEventCatalogResponse>> getAvatarEvent(@PathVariable Long id) {
        var event = avatarEventCatalogUseCase.getAvatarEvent(id);
        return ResponseEntity.ok(ApiResponse.ok(toResponse(event)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AvatarEventCatalogResponse>> updateAvatarEvent(
        @PathVariable Long id,
        @RequestBody UpdateAvatarEventCatalogRequest request
    ) {
        var event = avatarEventCatalogUseCase.updateAvatarEvent(
            id,
            AvatarEventType.valueOf(request.eventType()),
            AvatarTone.valueOf(request.tone()),
            request.locale(),
            request.messageText(),
            ContentStatus.valueOf(request.status())
        );
        return ResponseEntity.ok(ApiResponse.ok(toResponse(event)));
    }

    private static AvatarEventCatalogResponse toResponse(AvatarEventCatalog event) {
        return new AvatarEventCatalogResponse(
            event.getId(),
            event.getEventType().name(),
            event.getTone().name(),
            event.getLocale(),
            event.getMessageText(),
            event.getStatus().name(),
            event.getCreatedAt(),
            event.getUpdatedAt()
        );
    }
}
