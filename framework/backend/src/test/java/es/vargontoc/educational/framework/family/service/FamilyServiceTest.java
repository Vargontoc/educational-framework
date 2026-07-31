package es.vargontoc.educational.framework.family.service;

import es.vargontoc.educational.framework.family.infrastructure.dto.UpdateFamilyRequest;
import es.vargontoc.educational.framework.family.model.ChildProfile;
import es.vargontoc.educational.framework.family.model.Family;
import es.vargontoc.educational.framework.family.ports.out.ChildProfileRepository;
import es.vargontoc.educational.framework.family.ports.out.FamilyRepository;
import es.vargontoc.educational.framework.session.model.FamilySession;
import es.vargontoc.educational.framework.session.model.FamilySessionStatus;
import es.vargontoc.educational.framework.session.ports.out.ChildSessionRepository;
import es.vargontoc.educational.framework.session.ports.out.FamilySessionRepository;
import es.vargontoc.educational.framework.session.infrastructure.websocket.SessionEventPublisher;
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
    private ChildSessionRepository childSessionRepository;

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
            childSessionRepository,
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
        var child = new ChildProfile();
        child.setId(10L);
        child.setFamilyId(1L);
        child.setTtsEnabled(true);
        child.setAgentEnabled(true);

        when(familyRepository.findFamily()).thenReturn(Optional.of(family));
        when(childProfileRepository.findAll()).thenReturn(List.of(child));
        when(familyRepository.save(any(Family.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var request = new UpdateFamilyRequest("Updated", null, false, true, null, null, null, null, null, null, null);
        familyService.updateFamily(request);

        assertFalse(child.isTtsEnabled());
        verify(childProfileRepository).save(child);
    }

    @Test
    void updateFamily_doesNotReenableChildrenOnFamilyReenable() {
        var family = new Family();
        family.setId(1L);

        var child = new ChildProfile();
        child.setId(10L);
        child.setFamilyId(1L);
        child.setTtsEnabled(false);
        child.setAgentEnabled(false);

        when(familyRepository.findFamily()).thenReturn(Optional.of(family));
        when(familyRepository.save(any(Family.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var request = new UpdateFamilyRequest("Updated", null, true, true, null, null, null, null, null, null, null);
        familyService.updateFamily(request);

        assertFalse(child.isTtsEnabled());
        assertFalse(child.isAgentEnabled());
        verify(childProfileRepository, never()).save(child);
    }

    @Test
    void updateFamily_pinChangeRevokesActiveFamilySessions() {
        var family = new Family();
        family.setId(1L);

        var session1 = activeSession(100L);
        var session2 = activeSession(200L);

        when(familyRepository.findFamily()).thenReturn(Optional.of(family));
        when(familySessionRepository.findActiveByFamilyId(1L)).thenReturn(List.of(session1, session2));
        when(familyRepository.save(any(Family.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var request = new UpdateFamilyRequest("Updated", "5678", true, true, null, null, null, null, null, null, null);
        familyService.updateFamily(request);

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

        var session1 = activeSession(100L);
        var session2 = activeSession(200L);

        when(familyRepository.findFamily()).thenReturn(Optional.of(family));
        when(familySessionRepository.findActiveByFamilyId(1L)).thenReturn(List.of(session1, session2));
        when(familyRepository.save(any(Family.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var request = new UpdateFamilyRequest("Updated", "5678", true, true, null, null, null, null, null, null, null);
        familyService.updateFamily(request);

        verify(sessionEventPublisher, org.mockito.Mockito.times(2)).notifyParent(eq(1L), any());
    }

    @Test
    void updateFamily_noPinChangeDoesNotRevokeSessions() {
        var family = new Family();
        family.setId(1L);

        when(familyRepository.findFamily()).thenReturn(Optional.of(family));
        when(familyRepository.save(any(Family.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var request = new UpdateFamilyRequest("Updated", null, true, true, null, null, null, null, null, null, null);
        familyService.updateFamily(request);

        verify(familySessionRepository, never()).findActiveByFamilyId(any());
        verify(familySessionRepository, never()).saveAll(any());
        verify(sessionEventPublisher, never()).notifyParent(any(), any());
    }

    @Test
    void updateFamily_blankPinDoesNotRevokeSessions() {
        var family = new Family();
        family.setId(1L);

        when(familyRepository.findFamily()).thenReturn(Optional.of(family));
        when(familyRepository.save(any(Family.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var request = new UpdateFamilyRequest("Updated", "   ", true, true, null, null, null, null, null, null, null);
        familyService.updateFamily(request);

        verify(familySessionRepository, never()).findActiveByFamilyId(any());
        verify(familySessionRepository, never()).saveAll(any());
        verify(sessionEventPublisher, never()).notifyParent(any(), any());
    }

    @Test
    void updateFamily_noActiveSessionsOnPinChangeDoesNotThrow() {
        var family = new Family();
        family.setId(1L);

        when(familyRepository.findFamily()).thenReturn(Optional.of(family));
        when(familySessionRepository.findActiveByFamilyId(1L)).thenReturn(List.of());
        when(familyRepository.save(any(Family.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var request = new UpdateFamilyRequest("Updated", "5678", true, true, null, null, null, null, null, null, null);
        familyService.updateFamily(request);

        verify(familySessionRepository, never()).saveAll(any());
        verify(sessionEventPublisher, never()).notifyParent(any(), any());
    }

    @Test
    void getFamily_throwsWhenNotFound() {
        when(familyRepository.findFamily()).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> familyService.getFamily());
    }

    @Test
    void updateFamily_partialUpdate_onlyUpdatesProvidedGlobalConfigFields() {
        var family = new Family();
        family.setId(1L);
        family.setName("Original");
        family.setAudioGeneralEnabled(true);
        family.setAudioGeneralVolume(100);
        family.setNpcEnabled(true);
        family.setNpcVoiceEnabled(true);
        family.setNpcVoiceVolume(100);
        family.setNarrativeVoiceEnabled(true);
        family.setNarrativeVoiceVolume(100);

        when(familyRepository.findFamily()).thenReturn(Optional.of(family));
        when(familyRepository.save(any(Family.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Only update audioGeneralVolume
        var request = new UpdateFamilyRequest(null, null, null, null, null, 50, null, null, null, null, null);
        familyService.updateFamily(request);

        assertEquals("Original", family.getName());
        assertEquals(50, family.getAudioGeneralVolume());
        assertEquals(100, family.getNpcVoiceVolume()); // unchanged
        assertEquals(100, family.getNarrativeVoiceVolume()); // unchanged
    }

    @Test
    void updateFamily_volumeClamping_above100() {
        var family = new Family();
        family.setId(1L);

        when(familyRepository.findFamily()).thenReturn(Optional.of(family));
        when(familyRepository.save(any(Family.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var request = new UpdateFamilyRequest(null, null, null, null, null, 150, null, null, 200, null, null);
        familyService.updateFamily(request);

        assertEquals(100, family.getAudioGeneralVolume());
        assertEquals(100, family.getNpcVoiceVolume());
    }

    @Test
    void updateFamily_volumeClamping_below0() {
        var family = new Family();
        family.setId(1L);

        when(familyRepository.findFamily()).thenReturn(Optional.of(family));
        when(familyRepository.save(any(Family.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var request = new UpdateFamilyRequest(null, null, null, null, null, -10, null, null, -5, null, null);
        familyService.updateFamily(request);

        assertEquals(0, family.getAudioGeneralVolume());
        assertEquals(0, family.getNpcVoiceVolume());
    }

    @Test
    void updateFamily_updatesAllGlobalConfigFields() {
        var family = new Family();
        family.setId(1L);

        when(familyRepository.findFamily()).thenReturn(Optional.of(family));
        when(familyRepository.save(any(Family.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var request = new UpdateFamilyRequest(
            null, null, null, null,
            false, 75,
            false, false, 50,
            false, 25
        );
        familyService.updateFamily(request);

        assertFalse(family.isAudioGeneralEnabled());
        assertEquals(75, family.getAudioGeneralVolume());
        assertFalse(family.isNpcEnabled());
        assertFalse(family.isNpcVoiceEnabled());
        assertEquals(50, family.getNpcVoiceVolume());
        assertFalse(family.isNarrativeVoiceEnabled());
        assertEquals(25, family.getNarrativeVoiceVolume());
    }

    @Test
    void updateFamily_legacyFieldsCoexistWithGlobalConfig() {
        var family = new Family();
        family.setId(1L);
        family.setAudioGeneralEnabled(true);

        when(familyRepository.findFamily()).thenReturn(Optional.of(family));
        when(familyRepository.save(any(Family.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Disable legacy ttsEnabled but keep audioGeneralEnabled
        var request = new UpdateFamilyRequest(null, null, false, true, null, null, null, null, null, null, null);
        familyService.updateFamily(request);

        assertTrue(family.isAudioGeneralEnabled()); // independent
    }

    @Test
    void clampVolume_boundaries() {
        assertEquals(0, FamilyService.clampVolume(0));
        assertEquals(0, FamilyService.clampVolume(-1));
        assertEquals(0, FamilyService.clampVolume(-100));
        assertEquals(50, FamilyService.clampVolume(50));
        assertEquals(100, FamilyService.clampVolume(100));
        assertEquals(100, FamilyService.clampVolume(101));
        assertEquals(100, FamilyService.clampVolume(999));
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
