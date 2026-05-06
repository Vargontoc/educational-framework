package es.vargontoc.educational.framework.session.service;

import es.vargontoc.educational.framework.family.ports.out.FamilyRepository;
import es.vargontoc.educational.framework.session.model.FamilySession;
import es.vargontoc.educational.framework.session.model.FamilySessionResult;
import es.vargontoc.educational.framework.session.model.FamilySessionStatus;
import es.vargontoc.educational.framework.session.ports.in.FamilySessionUseCase;
import es.vargontoc.educational.framework.session.ports.out.FamilySessionRepository;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
import es.vargontoc.educational.framework.shared.exception.SessionException;
import es.vargontoc.educational.framework.shared.security.TokenGenerator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class FamilySessionService implements FamilySessionUseCase {

    private final FamilyRepository familyRepository;
    private final FamilySessionRepository familySessionRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public FamilySessionService(FamilyRepository familyRepository, FamilySessionRepository familySessionRepository) {
        this.familyRepository = familyRepository;
        this.familySessionRepository = familySessionRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public FamilySessionResult authenticate(Long familyId, String rawPin) {
        var family = familyRepository.findFamily()
            .orElseThrow(() -> new ResourceNotFoundException("Family not found"));

        if (!passwordEncoder.matches(rawPin, family.getPinHash())) {
            throw new SessionException("Invalid PIN");
        }

        var rawToken = TokenGenerator.generateRawToken();
        var session = new FamilySession();
        session.setTokenHash(TokenGenerator.hashToken(rawToken));
        session.setTokenType("opaque");
        session.setFamilyId(familyId);
        session.setCreatedAt(LocalDateTime.now());
        session.setRevoked(false);
        session.setStatus(FamilySessionStatus.ACTIVE);

        return new FamilySessionResult(rawToken, familySessionRepository.save(session));
    }

    @Override
    public void logout(String rawToken) {
        var session = findValidSession(rawToken);
        session.setStatus(FamilySessionStatus.REVOKED);
        session.setRevoked(true);
        session.setUpdatedAt(LocalDateTime.now());
        familySessionRepository.save(session);
    }

    @Override
    public void revokeAllByFamily(Long familyId) {
        var now = LocalDateTime.now();
        var sessions = familySessionRepository.findActiveByFamilyId(familyId);

        for (FamilySession session : sessions) {
            session.setStatus(FamilySessionStatus.REVOKED);
            session.setRevoked(true);
            session.setUpdatedAt(now);
        }

        familySessionRepository.saveAll(sessions);
    }

    @Override
    @Transactional(readOnly = true)
    public FamilySession getByToken(String rawToken) {
        return findValidSession(rawToken);
    }

    private FamilySession findValidSession(String rawToken) {
        var tokenHash = TokenGenerator.hashToken(rawToken);
        var session = familySessionRepository.findByTokenHash(tokenHash)
            .orElseThrow(() -> new SessionException("Session not found"));

        if (session.isRevoked() || FamilySessionStatus.REVOKED.equals(session.getStatus())) {
            throw new SessionException("Session revoked");
        }

        return session;
    }
}
