package es.vargontoc.educational.framework.session.service;

import es.vargontoc.educational.framework.family.model.Family;
import es.vargontoc.educational.framework.family.ports.out.FamilyRepository;
import es.vargontoc.educational.framework.session.model.FamilySession;
import es.vargontoc.educational.framework.session.model.FamilySessionStatus;
import es.vargontoc.educational.framework.session.ports.out.FamilySessionRepository;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
import es.vargontoc.educational.framework.shared.exception.SessionException;
import es.vargontoc.educational.framework.shared.security.TokenGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FamilySessionServiceTest {

    @Mock
    private FamilyRepository familyRepository;

    @Mock
    private FamilySessionRepository familySessionRepository;

    @InjectMocks
    private FamilySessionService familySessionService;

    @Test
    void authenticate_happyPathReturnsRawTokenAndSession() {
        var family = familyWithPin("1234");

        when(familyRepository.findFamily()).thenReturn(Optional.of(family));
        when(familySessionRepository.save(any(FamilySession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = familySessionService.authenticate(1L, "1234");

        assertNotNull(result.rawToken());
        assertEquals(43, result.rawToken().length());
        assertEquals(1L, result.session().getFamilyId());
        assertEquals(FamilySessionStatus.ACTIVE, result.session().getStatus());
        assertFalse(result.session().isRevoked());
        assertEquals(64, result.session().getTokenHash().length());
        assertFalse(result.toString().contains(result.rawToken()));
    }

    @Test
    void authenticate_throwsSessionExceptionForWrongPin() {
        when(familyRepository.findFamily()).thenReturn(Optional.of(familyWithPin("1234")));

        assertThrows(SessionException.class, () -> familySessionService.authenticate(1L, "9999"));
    }

    @Test
    void authenticate_throwsResourceNotFoundWhenFamilyIsMissing() {
        when(familyRepository.findFamily()).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> familySessionService.authenticate(1L, "1234"));
    }

    @Test
    void logout_validTokenRevokesSession() {
        var rawToken = "raw-token";
        var session = activeSession();

        when(familySessionRepository.findByTokenHash(TokenGenerator.hashToken(rawToken))).thenReturn(Optional.of(session));

        familySessionService.logout(rawToken);

        assertEquals(FamilySessionStatus.REVOKED, session.getStatus());
        assertTrue(session.isRevoked());
        assertNotNull(session.getUpdatedAt());
        verify(familySessionRepository).save(session);
    }

    @Test
    void logout_alreadyRevokedTokenThrowsSessionException() {
        var rawToken = "raw-token";
        var session = activeSession();
        session.setRevoked(true);
        session.setStatus(FamilySessionStatus.REVOKED);

        when(familySessionRepository.findByTokenHash(TokenGenerator.hashToken(rawToken))).thenReturn(Optional.of(session));

        assertThrows(SessionException.class, () -> familySessionService.logout(rawToken));
    }

    @Test
    void revokeAllByFamily_marksAllActiveSessionsRevoked() {
        var first = activeSession();
        var second = activeSession();

        when(familySessionRepository.findActiveByFamilyId(1L)).thenReturn(List.of(first, second));

        familySessionService.revokeAllByFamily(1L);

        assertEquals(FamilySessionStatus.REVOKED, first.getStatus());
        assertEquals(FamilySessionStatus.REVOKED, second.getStatus());
        assertTrue(first.isRevoked());
        assertTrue(second.isRevoked());

        verify(familySessionRepository).saveAll(List.of(first, second));
    }

    private Family familyWithPin(String rawPin) {
        var family = new Family();
        family.setId(1L);
        family.setPinHash(new BCryptPasswordEncoder().encode(rawPin));
        return family;
    }

    private FamilySession activeSession() {
        var session = new FamilySession();
        session.setStatus(FamilySessionStatus.ACTIVE);
        session.setRevoked(false);
        return session;
    }
}
