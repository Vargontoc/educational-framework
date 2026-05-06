package es.vargontoc.educational.framework.session.infrastructure.web;

import es.vargontoc.educational.framework.session.infrastructure.dto.ChildSessionResponse;
import es.vargontoc.educational.framework.session.infrastructure.dto.OpenChildSessionRequest;
import es.vargontoc.educational.framework.session.model.ChildSession;
import es.vargontoc.educational.framework.session.ports.in.ChildSessionUseCase;
import es.vargontoc.educational.framework.shared.api.ApiResponse;
import es.vargontoc.educational.framework.shared.config.SessionProperties;
import es.vargontoc.educational.framework.shared.exception.SessionException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sessions/children")
public class ChildSessionController {

    private final ChildSessionUseCase childSessionUseCase;
    private final SessionProperties sessionProperties;

    public ChildSessionController(ChildSessionUseCase childSessionUseCase, SessionProperties sessionProperties) {
        this.childSessionUseCase = childSessionUseCase;
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
        var session = childSessionUseCase.openSession(
            request.childProfileId(),
            currentFamilyId(),
            heartbeatInterval,
            connectionMeta(servletRequest)
        );

        return ResponseEntity.status(201).body(ApiResponse.created(toResponse(session)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ChildSessionResponse>>> getActiveSessions() {
        var sessions = childSessionUseCase.getActiveSessions(currentFamilyId()).stream()
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

    private static Long currentFamilyId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Long familyId)) {
            throw new SessionException("Authenticated family not found");
        }
        return familyId;
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
