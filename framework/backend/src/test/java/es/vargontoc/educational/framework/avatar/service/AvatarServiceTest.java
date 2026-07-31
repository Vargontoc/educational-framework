package es.vargontoc.educational.framework.avatar.service;

import es.vargontoc.educational.framework.avatar.infrastructure.dto.AvatarEventRequest;
import es.vargontoc.educational.framework.avatar.infrastructure.tts.TtsException;
import es.vargontoc.educational.framework.avatar.model.AvatarEventResult;
import es.vargontoc.educational.framework.avatar.ports.out.TtsClient;
import es.vargontoc.educational.framework.content.model.AvatarEventCatalog;
import es.vargontoc.educational.framework.content.model.AvatarEventType;
import es.vargontoc.educational.framework.content.model.AvatarTone;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.ports.out.AvatarEventCatalogRepository;
import es.vargontoc.educational.framework.family.model.ChildProfile;
import es.vargontoc.educational.framework.family.ports.out.ChildProfileRepository;
import es.vargontoc.educational.framework.session.model.ChildSession;
import es.vargontoc.educational.framework.session.model.ChildSessionStatus;
import es.vargontoc.educational.framework.session.ports.out.ChildSessionRepository;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
import es.vargontoc.educational.framework.shared.exception.SessionException;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvatarServiceTest {

    @Mock
    private ChildSessionRepository childSessionRepository;

    @Mock
    private ChildProfileRepository childProfileRepository;

    @Mock
    private AvatarEventCatalogRepository avatarEventCatalogRepository;

    @Mock
    private TtsClient ttsClient;

    private AvatarService avatarService;

    @BeforeEach
    void setUp() {
        avatarService = new AvatarService(childSessionRepository, childProfileRepository, avatarEventCatalogRepository, ttsClient);
    }

    @Test
    void processAvatarEvent_activeSession_returnsCatalogText() {
        ChildSession session = createActiveSession(1L, 100L, 10L);
        ChildProfile childProfile = createChildProfile(100L, true, true);
        AvatarEventCatalog catalog = createCatalog(AvatarEventType.ACTIVITY_COMPLETED, AvatarTone.NEUTRAL, "es-ES", "Great job!");

        when(childSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(childProfileRepository.findById(100L)).thenReturn(Optional.of(childProfile));
        when(avatarEventCatalogRepository.findActiveByFilters(AvatarEventType.ACTIVITY_COMPLETED, AvatarTone.NEUTRAL, "es-ES"))
            .thenReturn(List.of(catalog));
        when(ttsClient.synthesize(anyString(), anyString(), any(), anyString()))
            .thenReturn("fake-mp3-data".getBytes());

        AvatarEventRequest request = new AvatarEventRequest(1L, AvatarEventType.ACTIVITY_COMPLETED, "es-ES", null);
        AvatarEventResult result = avatarService.processAvatarEvent(request);

        assertNotNull(result);
        assertEquals(AvatarEventType.ACTIVITY_COMPLETED, result.getEventType());
        assertEquals("Great job!", result.getText());
        assertTrue(result.isAudioAvailable());
        assertFalse(result.isSuppressed());
        assertNotNull(result.getAudioData());
    }

    @Test
    void processAvatarEvent_inactiveSession_throwsSessionException() {
        ChildSession session = createInactiveSession(1L, 100L, 10L);

        when(childSessionRepository.findById(1L)).thenReturn(Optional.of(session));

        AvatarEventRequest request = new AvatarEventRequest(1L, AvatarEventType.ACTIVITY_COMPLETED, "es-ES", null);

        assertThrows(SessionException.class, () -> avatarService.processAvatarEvent(request));
    }

    @Test
    void processAvatarEvent_missingSession_throwsResourceNotFound() {
        when(childSessionRepository.findById(999L)).thenReturn(Optional.empty());

        AvatarEventRequest request = new AvatarEventRequest(999L, AvatarEventType.ACTIVITY_COMPLETED, "es-ES", null);

        assertThrows(ResourceNotFoundException.class, () -> avatarService.processAvatarEvent(request));
    }

    @Test
    void processAvatarEvent_agentDisabled_returnsSuppressedResult() {
        ChildSession session = createActiveSession(1L, 100L, 10L);
        ChildProfile childProfile = createChildProfile(100L, true, false);

        when(childSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(childProfileRepository.findById(100L)).thenReturn(Optional.of(childProfile));

        AvatarEventRequest request = new AvatarEventRequest(1L, AvatarEventType.ACTIVITY_COMPLETED, "es-ES", null);
        AvatarEventResult result = avatarService.processAvatarEvent(request);

        assertNotNull(result);
        assertEquals(AvatarEventType.ACTIVITY_COMPLETED, result.getEventType());
        assertNull(result.getText());
        assertFalse(result.isAudioAvailable());
        assertTrue(result.isSuppressed());
    }

    @Test
    void processAvatarEvent_ttsDisabled_returnsTextOnlyResult() {
        ChildSession session = createActiveSession(1L, 100L, 10L);
        ChildProfile childProfile = createChildProfile(100L, false, true);
        AvatarEventCatalog catalog = createCatalog(AvatarEventType.ACTIVITY_COMPLETED, AvatarTone.NEUTRAL, "es-ES", "Great job!");

        when(childSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(childProfileRepository.findById(100L)).thenReturn(Optional.of(childProfile));
        when(avatarEventCatalogRepository.findActiveByFilters(AvatarEventType.ACTIVITY_COMPLETED, AvatarTone.NEUTRAL, "es-ES"))
            .thenReturn(List.of(catalog));

        AvatarEventRequest request = new AvatarEventRequest(1L, AvatarEventType.ACTIVITY_COMPLETED, "es-ES", null);
        AvatarEventResult result = avatarService.processAvatarEvent(request);

        assertNotNull(result);
        assertEquals(AvatarEventType.ACTIVITY_COMPLETED, result.getEventType());
        assertEquals("Great job!", result.getText());
        assertFalse(result.isAudioAvailable());
        assertFalse(result.isSuppressed());
        assertNull(result.getAudioData());
    }

    @Test
    void processAvatarEvent_noCatalogMatch_returnsFallbackText() {
        ChildSession session = createActiveSession(1L, 100L, 10L);
        ChildProfile childProfile = createChildProfile(100L, true, true);

        when(childSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(childProfileRepository.findById(100L)).thenReturn(Optional.of(childProfile));
        when(avatarEventCatalogRepository.findActiveByFilters(any(), any(), any()))
            .thenReturn(List.of());
        when(ttsClient.synthesize(anyString(), anyString(), any(), anyString()))
            .thenReturn("fake-mp3-data".getBytes());

        AvatarEventRequest request = new AvatarEventRequest(1L, AvatarEventType.ACTIVITY_COMPLETED, "es-ES", null);
        AvatarEventResult result = avatarService.processAvatarEvent(request);

        assertNotNull(result);
        assertEquals(AvatarEventType.ACTIVITY_COMPLETED, result.getEventType());
        assertEquals("Thank you for using the educational framework!", result.getText());
        assertTrue(result.isAudioAvailable());
        assertFalse(result.isSuppressed());
    }

    @Test
    void processAvatarEvent_ttsEnabled_butSynthesisFails_returnsTextOnly() {
        ChildSession session = createActiveSession(1L, 100L, 10L);
        ChildProfile childProfile = createChildProfile(100L, true, true);
        AvatarEventCatalog catalog = createCatalog(AvatarEventType.ACTIVITY_COMPLETED, AvatarTone.NEUTRAL, "es-ES", "Great job!");

        when(childSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(childProfileRepository.findById(100L)).thenReturn(Optional.of(childProfile));
        when(avatarEventCatalogRepository.findActiveByFilters(AvatarEventType.ACTIVITY_COMPLETED, AvatarTone.NEUTRAL, "es-ES"))
            .thenReturn(List.of(catalog));
        when(ttsClient.synthesize(anyString(), anyString(), any(), anyString()))
            .thenThrow(new TtsException("TTS timeout", "CONNECTION_ERROR", true));

        AvatarEventRequest request = new AvatarEventRequest(1L, AvatarEventType.ACTIVITY_COMPLETED, "es-ES", null);
        AvatarEventResult result = avatarService.processAvatarEvent(request);

        assertNotNull(result);
        assertEquals(AvatarEventType.ACTIVITY_COMPLETED, result.getEventType());
        assertEquals("Great job!", result.getText());
        assertFalse(result.isAudioAvailable());
        assertFalse(result.isSuppressed());
        assertNull(result.getAudioData());
    }

    private ChildSession createActiveSession(Long sessionId, Long childProfileId, Long familyId) {
        ChildSession session = new ChildSession();
        session.setId(sessionId);
        session.setChildProfileId(childProfileId);
        session.setFamilyId(familyId);
        session.setStatus(ChildSessionStatus.ACTIVE);
        return session;
    }

    private ChildSession createInactiveSession(Long sessionId, Long childProfileId, Long familyId) {
        ChildSession session = new ChildSession();
        session.setId(sessionId);
        session.setChildProfileId(childProfileId);
        session.setFamilyId(familyId);
        session.setStatus(ChildSessionStatus.CLOSED);
        return session;
    }

    private ChildProfile createChildProfile(Long id, boolean ttsEnabled, boolean agentEnabled) {
        ChildProfile profile = new ChildProfile();
        profile.setId(id);
        profile.setNpcVoiceEnabled(ttsEnabled);
        profile.setNpcEnabled(agentEnabled);
        return profile;
    }

    private AvatarEventCatalog createCatalog(AvatarEventType eventType, AvatarTone tone, String locale, String messageText) {
        AvatarEventCatalog catalog = new AvatarEventCatalog();
        catalog.setId(1L);
        catalog.setEventType(eventType);
        catalog.setTone(tone);
        catalog.setLocale(locale);
        catalog.setMessageText(messageText);
        catalog.setStatus(ContentStatus.ACTIVE);
        return catalog;
    }
}