package es.vargontoc.educational.framework.session.infrastructure.web;

import es.vargontoc.educational.framework.family.ports.in.ChildProfileUseCase;
import es.vargontoc.educational.framework.session.infrastructure.dto.ChildSessionResponse;
import es.vargontoc.educational.framework.session.infrastructure.dto.OpenChildSessionRequest;
import es.vargontoc.educational.framework.session.model.ChildSession;
import es.vargontoc.educational.framework.session.ports.in.ChildSessionUseCase;
import es.vargontoc.educational.framework.shared.api.ApiResponse;
import es.vargontoc.educational.framework.shared.config.SessionProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sessions/children")
public class ChildSessionController {

    private final ChildSessionUseCase childSessionUseCase;
    private final ChildProfileUseCase childProfileUseCase;
    private final SessionProperties sessionProperties;

    public ChildSessionController(
        ChildSessionUseCase childSessionUseCase,
        ChildProfileUseCase childProfileUseCase,
        SessionProperties sessionProperties
    ) {
        this.childSessionUseCase = childSessionUseCase;
        this.childProfileUseCase = childProfileUseCase;
        this.sessionProperties = sessionProperties;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ChildSessionResponse>> openSession(
        @RequestBody OpenChildSessionRequest request,
        HttpServletRequest servletRequest
    ) {
        var heartbeatInterval = request.heartbeatIntervalSeconds() != null
            ? request.heartbeatIntervalSeconds()
            : sessionProperties.getDefaultHeartbeatIntervalSeconds();
        var familyId = childProfileUseCase.getChild(request.childProfileId()).getFamilyId();
        var session = childSessionUseCase.openSession(
            request.childProfileId(),
            familyId,
            heartbeatInterval,
            connectionMeta(servletRequest)
        );

        return ResponseEntity.status(201).body(ApiResponse.created(toResponse(session)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ChildSessionResponse>>> getActiveSessions(@RequestParam Long familyId) {
        var sessions = childSessionUseCase.getActiveSessions(familyId).stream()
            .map(ChildSessionController::toResponse)
            .toList();
        return ResponseEntity.ok(ApiResponse.ok(sessions));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> closeSession(@PathVariable Long id) {
        childSessionUseCase.closeSession(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/expel")
    public ResponseEntity<Void> expelChild(@PathVariable Long id) {
        childSessionUseCase.expelChild(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/heartbeat")
    public ResponseEntity<Void> recordHeartbeat(@PathVariable Long id) {
        childSessionUseCase.recordHeartbeat(id);
        return ResponseEntity.noContent().build();
    }

    private static String connectionMeta(HttpServletRequest request) {
        return "{\"ip\":\"" + escapeJson(request.getRemoteAddr())
            + "\",\"userAgent\":\"" + escapeJson(request.getHeader("User-Agent")) + "\"}";
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static ChildSessionResponse toResponse(ChildSession session) {
        return new ChildSessionResponse(
            session.getId(),
            session.getChildProfileId(),
            session.getFamilyId(),
            session.getStatus().name(),
            session.getStartedAt(),
            session.getEndedAt(),
            session.getDurationSeconds(),
            session.getLastActivityAt()
        );
    }
}
