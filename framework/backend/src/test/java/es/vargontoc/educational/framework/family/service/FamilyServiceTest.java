package es.vargontoc.educational.framework.family.service;

import es.vargontoc.educational.framework.family.model.ChildProfile;
import es.vargontoc.educational.framework.family.model.Family;
import es.vargontoc.educational.framework.family.ports.out.ChildProfileRepository;
import es.vargontoc.educational.framework.family.ports.out.FamilyRepository;
import es.vargontoc.educational.framework.session.model.FamilySession;
import es.vargontoc.educational.framework.session.model.FamilySessionStatus;
import es.vargontoc.educational.framework.session.ports.out.FamilySessionRepository;
import es.vargontoc.educational.framework.session.infrastructure.websocket.SessionEventPublisher;
import es.vargontoc.educational.framework.session.infrastructure.websocket.SessionEventType;
import es.vargontoc.educational.framework.shared.exception.ConflictException;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FamilyServiceTest {

    @Mock
    private FamilyRepository familyRepository;

    @Mock
    private ChildProfileRepository childProfileRepository;

    @Mock
    private FamilySessionRepository familySessionRepository;

    @Mock
    private SessionEventPublisher sessionEventPublisher;

    private FamilyService familyService;

    @BeforeEach
    void setUp() {
        familyService = new FamilyService(
            familyRepository,
            childProfileRepository,
            familySessionRepository,
            sessionEventPublisher
        );
    }

    @Test
    void createFamily_happyPath() {
        when(familyRepository.exists()).thenReturn(false);
        when(familyRepository.save(any(Family.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = familyService.createFamily("My Family", "1234", true, true);

        assertEquals("My Family", result.getName());
        assertTrue(result.isTtsEnabled());
        assertTrue(result.isAgentEnabled());
        assertNotNull(result.getPinHash());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void createFamily_throwsConflictWhenExists() {
        when(familyRepository.exists()).thenReturn(true);

        assertThrows(ConflictException.class, () -> familyService.createFamily("My Family", "1234", true, true));
    }

    @Test
    void updateFamily_propagatesTtsDisabledToChildren() {
        var family = new Family();
        family.setId(1L);
        family.setTtsEnabled(true);
        family.setAgentEnabled(true);

        var child = new ChildProfile();
        child.setId(10L);
        child.setFamilyId(1L);
        child.setTtsEnabled(true);
        child.setAgentEnabled(true);

        when(familyRepository.findFamily()).thenReturn(Optional.of(family));
        when(childProfileRepository.findAll()).thenReturn(List.of(child));
        when(familyRepository.save(any(Family.class))).thenAnswer(invocation -> invocation.getArgument(0));

        familyService.updateFamily("Updated", null, false, true);

        assertFalse(child.isTtsEnabled());
        verify(childProfileRepository).save(child);
    }

    @Test
    void updateFamily_doesNotReenableChildrenOnFamilyReenable() {
        var family = new Family();
        family.setId(1L);
        family.setTtsEnabled(false);
        family.setAgentEnabled(false);

        var child = new ChildProfile();
        child.setId(10L);
        child.setFamilyId(1L);
        child.setTtsEnabled(false);
        child.setAgentEnabled(false);

        when(familyRepository.findFamily()).thenReturn(Optional.of(family));
        when(familyRepository.save(any(Family.class))).thenAnswer(invocation -> invocation.getArgument(0));

        familyService.updateFamily("Updated", null, true, true);

        assertFalse(child.isTtsEnabled());
        assertFalse(child.isAgentEnabled());
        verify(childProfileRepository, never()).save(child);
    }

    @Test
    void updateFamily_pinChangeRevokesActiveFamilySessions() {
        var family = new Family();
        family.setId(1L);
        family.setTtsEnabled(true);
        family.setAgentEnabled(true);

        var session1 = activeSession(100L);
        var session2 = activeSession(200L);

        when(familyRepository.findFamily()).thenReturn(Optional.of(family));
        when(familySessionRepository.findActiveByFamilyId(1L)).thenReturn(List.of(session1, session2));
        when(familyRepository.save(any(Family.class))).thenAnswer(invocation -> invocation.getArgument(0));

        familyService.updateFamily("Updated", "5678", true, true);

        assertEquals(FamilySessionStatus.REVOKED, session1.getStatus());
        assertEquals(FamilySessionStatus.REVOKED, session2.getStatus());
        assertTrue(session1.isRevoked());
        assertTrue(session2.isRevoked());
        verify(familySessionRepository).saveAll(List.of(session1, session2));
    }

    @Test
    void updateFamily_pinChangeSendsSessionInvalidatedEvents() {
        var family = new Family();
        family.setId(1L);
        family.setTtsEnabled(true);
        family.setAgentEnabled(true);

        var session1 = activeSession(100L);
        var session2 = activeSession(200L);

        when(familyRepository.findFamily()).thenReturn(Optional.of(family));
        when(familySessionRepository.findActiveByFamilyId(1L)).thenReturn(List.of(session1, session2));
        when(familyRepository.save(any(Family.class))).thenAnswer(invocation -> invocation.getArgument(0));

        familyService.updateFamily("Updated", "5678", true, true);

        verify(sessionEventPublisher, org.mockito.Mockito.times(2)).notifyParent(eq(1L), any());
    }

    @Test
    void updateFamily_noPinChangeDoesNotRevokeSessions() {
        var family = new Family();
        family.setId(1L);
        family.setTtsEnabled(true);
        family.setAgentEnabled(true);

        when(familyRepository.findFamily()).thenReturn(Optional.of(family));
        when(familyRepository.save(any(Family.class))).thenAnswer(invocation -> invocation.getArgument(0));

        familyService.updateFamily("Updated", null, true, true);

        verify(familySessionRepository, never()).findActiveByFamilyId(any());
        verify(familySessionRepository, never()).saveAll(any());
        verify(sessionEventPublisher, never()).notifyParent(any(), any());
    }

    @Test
    void updateFamily_blankPinDoesNotRevokeSessions() {
        var family = new Family();
        family.setId(1L);
        family.setTtsEnabled(true);
        family.setAgentEnabled(true);

        when(familyRepository.findFamily()).thenReturn(Optional.of(family));
        when(familyRepository.save(any(Family.class))).thenAnswer(invocation -> invocation.getArgument(0));

        familyService.updateFamily("Updated", "   ", true, true);

        verify(familySessionRepository, never()).findActiveByFamilyId(any());
        verify(familySessionRepository, never()).saveAll(any());
        verify(sessionEventPublisher, never()).notifyParent(any(), any());
    }

    @Test
    void updateFamily_noActiveSessionsOnPinChangeDoesNotThrow() {
        var family = new Family();
        family.setId(1L);
        family.setTtsEnabled(true);
        family.setAgentEnabled(true);

        when(familyRepository.findFamily()).thenReturn(Optional.of(family));
        when(familySessionRepository.findActiveByFamilyId(1L)).thenReturn(List.of());
        when(familyRepository.save(any(Family.class))).thenAnswer(invocation -> invocation.getArgument(0));

        familyService.updateFamily("Updated", "5678", true, true);

        verify(familySessionRepository, never()).saveAll(any());
        verify(sessionEventPublisher, never()).notifyParent(any(), any());
    }

    @Test
    void getFamily_throwsWhenNotFound() {
        when(familyRepository.findFamily()).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> familyService.getFamily());
    }

    private FamilySession activeSession(Long id) {
        var session = new FamilySession();
        session.setId(id);
        session.setFamilyId(1L);
        session.setStatus(FamilySessionStatus.ACTIVE);
        session.setRevoked(false);
        return session;
    }
}
