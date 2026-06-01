package es.vargontoc.educational.framework.session.service;

import es.vargontoc.educational.framework.session.infrastructure.websocket.SessionEvent;
import es.vargontoc.educational.framework.session.infrastructure.websocket.SessionEventPublisher;
import es.vargontoc.educational.framework.session.infrastructure.websocket.SessionEventType;
import es.vargontoc.educational.framework.session.model.ChildSession;
import es.vargontoc.educational.framework.session.model.ChildSessionStatus;
import es.vargontoc.educational.framework.session.ports.out.ChildSessionRepository;
import es.vargontoc.educational.framework.shared.exception.SessionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChildSessionServiceTest {

    @Mock
    private ChildSessionRepository childSessionRepository;

    @Mock
    private SessionEventPublisher sessionEventPublisher;

    @InjectMocks
    private ChildSessionService childSessionService;

    @Test
    void openSession_withoutPriorSessionCreatesActiveSession() {
        when(childSessionRepository.findActiveByChildProfileId(10L)).thenReturn(Optional.empty());
        when(childSessionRepository.save(any(ChildSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = childSessionService.openSession(10L, 1L, 30, "{\"deviceId\":\"tablet\"}");

        assertEquals(10L, result.getChildProfileId());
        assertEquals(1L, result.getFamilyId());
        assertEquals(ChildSessionStatus.ACTIVE, result.getStatus());
        assertEquals(30, result.getHeartbeatIntervalSeconds());
        assertNotNull(result.getStartedAt());
        assertNotNull(result.getLastActivityAt());
        verify(sessionEventPublisher, never()).notifyChildAndParent(any(), any(), any());
    }

    @Test
    void openSession_withPriorActiveClosesPriorAndCreatesNewSession() {
        var priorSession = activeSession();
        priorSession.setStartedAt(LocalDateTime.now().minusSeconds(5));

        when(childSessionRepository.findActiveByChildProfileId(10L)).thenReturn(Optional.of(priorSession));
        when(childSessionRepository.save(any(ChildSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = childSessionService.openSession(10L, 1L, 30, null);

        assertEquals(ChildSessionStatus.CLOSED, priorSession.getStatus());
        assertNotNull(priorSession.getEndedAt());
        assertTrue(priorSession.getDurationSeconds() > 0);
        assertEquals(ChildSessionStatus.ACTIVE, result.getStatus());
        verify(childSessionRepository).save(priorSession);
    }

    @Test
    void openSession_withPriorActiveSendsSessionExpiredEvent() {
        var priorSession = activeSession();
        priorSession.setId(99L);
        priorSession.setFamilyId(1L);
        priorSession.setStartedAt(LocalDateTime.now().minusSeconds(5));

        when(childSessionRepository.findActiveByChildProfileId(10L)).thenReturn(Optional.of(priorSession));
        when(childSessionRepository.save(any(ChildSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        childSessionService.openSession(10L, 1L, 30, null);

        var eventCaptor = ArgumentCaptor.forClass(SessionEvent.class);
        verify(sessionEventPublisher).notifyChildAndParent(eq(99L), eq(1L), eventCaptor.capture());

        var event = eventCaptor.getValue();
        assertEquals(SessionEventType.SESSION_EXPIRED, event.event());
        assertEquals(99L, event.sessionId());
        assertEquals("new_session_opened", event.payload().get("reason"));
    }

    @Test
    void closeSession_computesPositiveDuration() {
        var session = activeSession();
        session.setStartedAt(LocalDateTime.now().minusSeconds(5));

        when(childSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(childSessionRepository.save(any(ChildSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = childSessionService.closeSession(1L);

        assertEquals(ChildSessionStatus.CLOSED, result.getStatus());
        assertNotNull(result.getEndedAt());
        assertTrue(result.getDurationSeconds() > 0);
    }

    @Test
    void expelChild_setsStatusExpelled() {
        var session = activeSession();
        session.setStartedAt(LocalDateTime.now().minusSeconds(5));

        when(childSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(childSessionRepository.save(any(ChildSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = childSessionService.expelChild(1L);

        assertEquals(ChildSessionStatus.EXPELLED, result.getStatus());
        assertNotNull(result.getEndedAt());
        assertTrue(result.getDurationSeconds() > 0);
    }

    @Test
    void recordHeartbeat_updatesLastActivityAt() {
        var session = activeSession();
        var previousActivity = LocalDateTime.now().minusMinutes(1);
        session.setLastActivityAt(previousActivity);

        when(childSessionRepository.findById(1L)).thenReturn(Optional.of(session));

        childSessionService.recordHeartbeat(1L);

        assertTrue(session.getLastActivityAt().isAfter(previousActivity));
        verify(childSessionRepository).save(session);
    }

    @Test
    void recordHeartbeat_onNonActiveSessionThrowsSessionException() {
        var session = activeSession();
        session.setStatus(ChildSessionStatus.CLOSED);

        when(childSessionRepository.findById(1L)).thenReturn(Optional.of(session));

        assertThrows(SessionException.class, () -> childSessionService.recordHeartbeat(1L));
    }

    @Test
    void getSession_found_returnsSession() {
        var session = activeSession();
        when(childSessionRepository.findById(1L)).thenReturn(Optional.of(session));

        var result = childSessionService.getSession(1L);

        assertEquals(1L, result.getId());
        assertEquals(ChildSessionStatus.ACTIVE, result.getStatus());
    }

    @Test
    void getSession_notFound_throwsResourceNotFoundException() {
        when(childSessionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException.class,
            () -> childSessionService.getSession(99L));
    }

    @Test
    void expireInactiveSessions_marksAllResultsExpired() {
        var first = activeSession();
        var second = activeSession();
        var cutoff = LocalDateTime.now().minusMinutes(1);

        when(childSessionRepository.findExpirableSessions(cutoff)).thenReturn(List.of(first, second));

        childSessionService.expireInactiveSessions(cutoff);

        assertEquals(ChildSessionStatus.EXPIRED, first.getStatus());
        assertEquals(ChildSessionStatus.EXPIRED, second.getStatus());

        verify(childSessionRepository).saveAll(List.of(first, second));
    }

    @Test
    void expireInactiveSessions_sendsSessionExpiredEventWithInactivityReason() {
        var session = activeSession();
        session.setId(55L);
        session.setFamilyId(1L);
        var cutoff = LocalDateTime.now().minusMinutes(1);

        when(childSessionRepository.findExpirableSessions(cutoff)).thenReturn(List.of(session));

        childSessionService.expireInactiveSessions(cutoff);

        var eventCaptor = ArgumentCaptor.forClass(SessionEvent.class);
        verify(sessionEventPublisher).notifyChildAndParent(eq(55L), eq(1L), eventCaptor.capture());

        var event = eventCaptor.getValue();
        assertEquals(SessionEventType.SESSION_EXPIRED, event.event());
        assertEquals(55L, event.sessionId());
        assertEquals("inactivity", event.payload().get("reason"));
    }

    private ChildSession activeSession() {
        var session = new ChildSession();
        session.setId(1L);
        session.setChildProfileId(10L);
        session.setFamilyId(1L);
        session.setStartedAt(LocalDateTime.now().minusSeconds(1));
        session.setStatus(ChildSessionStatus.ACTIVE);
        session.setLastActivityAt(LocalDateTime.now().minusSeconds(1));
        return session;
    }
}
