package es.vargontoc.educational.framework.session.infrastructure.web;

import es.vargontoc.educational.framework.family.ports.in.FamilyUseCase;
import es.vargontoc.educational.framework.session.infrastructure.dto.LoginRequest;
import es.vargontoc.educational.framework.session.infrastructure.dto.LoginResponse;
import es.vargontoc.educational.framework.session.model.FamilySessionResult;
import es.vargontoc.educational.framework.session.ports.in.FamilySessionUseCase;
import es.vargontoc.educational.framework.shared.api.ApiResponse;
import es.vargontoc.educational.framework.shared.exception.SessionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    private final FamilyUseCase familyUseCase;
    private final FamilySessionUseCase familySessionUseCase;

    public AuthController(FamilyUseCase familyUseCase, FamilySessionUseCase familySessionUseCase) {
        this.familyUseCase = familyUseCase;
        this.familySessionUseCase = familySessionUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        var family = familyUseCase.getFamily();
        var result = familySessionUseCase.authenticate(family.getId(), request.pin());

        LOGGER.info("Family session created: sessionId={}, familyId={}",
            result.session().getId(), result.session().getFamilyId());

        return ResponseEntity.status(201).body(ApiResponse.created(toResponse(result)));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
        @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new SessionException("Missing bearer token");
        }
        familySessionUseCase.logout(authorization.substring("Bearer ".length()));
        return ResponseEntity.noContent().build();
    }

    private static LoginResponse toResponse(FamilySessionResult result) {
        var session = result.session();
        return new LoginResponse(
            result.rawToken(),
            session.getId(),
            session.getFamilyId(),
            session.getCreatedAt()
        );
    }
}
