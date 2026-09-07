package es.vargontoc.educational.framework.avatar.service;

import es.vargontoc.educational.framework.audio.application.ports.in.AudioUseCase;
import es.vargontoc.educational.framework.audio.domain.AudioRequest;
import es.vargontoc.educational.framework.audio.domain.enums.TonePreset;
import es.vargontoc.educational.framework.avatar.domain.AvatarEventRequest;
import es.vargontoc.educational.framework.avatar.domain.AvatarLifecycleResult;
import es.vargontoc.educational.framework.avatar.domain.enums.AvatarEventType;
import es.vargontoc.educational.framework.avatar.infrastructure.service.AvatarService;
import es.vargontoc.educational.framework.content.model.AvatarEventCatalog;
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
    private AudioUseCase audio;

    private AvatarService avatarService;

    @BeforeEach
    void setUp() {
        avatarService = new AvatarService(childSessionRepository, childProfileRepository, avatarEventCatalogRepository, audio);
    }

    @Test
    void processEvent_activeSessionNpcAndVoiceEnabled_returnsAudioResult() {
        ChildSession session = createActiveSession(1L, 100L);
        ChildProfile childProfile = createChildProfile(100L, "Ada", true, true);
        AvatarEventCatalog catalog = createCatalog(AvatarEventType.ACTIVITY_COMPLETED, "Great job <name>!");

        when(childSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(childProfileRepository.findById(100L)).thenReturn(Optional.of(childProfile));
        when(avatarEventCatalogRepository.findByEventType(AvatarEventType.ACTIVITY_COMPLETED))
            .thenReturn(List.of(catalog));
        when(audio.getAudio(any(AudioRequest.class))).thenReturn("fake-mp3-data".getBytes());

        AvatarEventRequest request = new AvatarEventRequest(1L, AvatarEventType.ACTIVITY_COMPLETED, null);
        AvatarLifecycleResult result = avatarService.processEvent(request);

        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals("ACTIVITY_COMPLETED", result.event().eventType());
        assertTrue(result.event().audioAvailable());
        assertNotNull(result.event().audioId());
        assertEquals("Great job <name>!", result.event().text());
        assertNotNull(result.audioData());
    }

    @Test
    void processEvent_inactiveSession_throwsSessionException() {
        ChildSession session = createSession(1L, 100L, ChildSessionStatus.CLOSED);

        when(childSessionRepository.findById(1L)).thenReturn(Optional.of(session));

        AvatarEventRequest request = new AvatarEventRequest(1L, AvatarEventType.ACTIVITY_COMPLETED, null);

        assertThrows(SessionException.class, () -> avatarService.processEvent(request));
    }

    @Test
    void processEvent_farewellOnClosedSession_doesNotThrow() {
        ChildSession session = createSession(1L, 100L, ChildSessionStatus.CLOSED);
        ChildProfile childProfile = createChildProfile(100L, "Ada", true, true);
        AvatarEventCatalog catalog = createCatalog(AvatarEventType.FAREWELL, "Bye <name>!");

        when(childSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(childProfileRepository.findById(100L)).thenReturn(Optional.of(childProfile));
        when(avatarEventCatalogRepository.findByEventType(AvatarEventType.FAREWELL))
            .thenReturn(List.of(catalog));
        when(audio.getAudio(any(AudioRequest.class))).thenReturn("fake-mp3-data".getBytes());

        AvatarEventRequest request = new AvatarEventRequest(1L, AvatarEventType.FAREWELL, null);
        AvatarLifecycleResult result = avatarService.processEvent(request);

        assertNotNull(result);
        assertTrue(result.event().audioAvailable());
    }

    @Test
    void processEvent_missingSession_throwsResourceNotFound() {
        when(childSessionRepository.findById(999L)).thenReturn(Optional.empty());

        AvatarEventRequest request = new AvatarEventRequest(999L, AvatarEventType.ACTIVITY_COMPLETED, null);

        assertThrows(ResourceNotFoundException.class, () -> avatarService.processEvent(request));
    }

    @Test
    void processEvent_missingChildProfile_throwsResourceNotFound() {
        ChildSession session = createActiveSession(1L, 100L);

        when(childSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(childProfileRepository.findById(100L)).thenReturn(Optional.empty());

        AvatarEventRequest request = new AvatarEventRequest(1L, AvatarEventType.ACTIVITY_COMPLETED, null);

        assertThrows(ResourceNotFoundException.class, () -> avatarService.processEvent(request));
    }

    @Test
    void processEvent_npcDisabled_returnsFallbackResult() {
        ChildSession session = createActiveSession(1L, 100L);
        ChildProfile childProfile = createChildProfile(100L, "Ada", true, false);

        when(childSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(childProfileRepository.findById(100L)).thenReturn(Optional.of(childProfile));

        AvatarEventRequest request = new AvatarEventRequest(1L, AvatarEventType.ACTIVITY_COMPLETED, null);
        AvatarLifecycleResult result = avatarService.processEvent(request);

        assertNotNull(result);
        assertFalse(result.event().audioAvailable());
        assertNull(result.event().audioId());
        assertEquals("", result.event().text());
        assertNull(result.audioData());
    }

    @Test
    void processEvent_npcVoiceDisabled_returnsFallbackResult() {
        ChildSession session = createActiveSession(1L, 100L);
        ChildProfile childProfile = createChildProfile(100L, "Ada", false, true);
        AvatarEventCatalog catalog = createCatalog(AvatarEventType.ACTIVITY_COMPLETED, "Great job!");

        when(childSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(childProfileRepository.findById(100L)).thenReturn(Optional.of(childProfile));
        when(avatarEventCatalogRepository.findByEventType(AvatarEventType.ACTIVITY_COMPLETED))
            .thenReturn(List.of(catalog));

        AvatarEventRequest request = new AvatarEventRequest(1L, AvatarEventType.ACTIVITY_COMPLETED, null);
        AvatarLifecycleResult result = avatarService.processEvent(request);

        assertNotNull(result);
        assertFalse(result.event().audioAvailable());
        assertEquals("Great job!", result.event().text());
        assertNull(result.audioData());
    }

    @Test
    void processEvent_noCatalogMatch_returnsFallbackResult() {
        ChildSession session = createActiveSession(1L, 100L);
        ChildProfile childProfile = createChildProfile(100L, "Ada", true, true);

        when(childSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(childProfileRepository.findById(100L)).thenReturn(Optional.of(childProfile));
        when(avatarEventCatalogRepository.findByEventType(AvatarEventType.ACTIVITY_COMPLETED))
            .thenReturn(List.of());

        AvatarEventRequest request = new AvatarEventRequest(1L, AvatarEventType.ACTIVITY_COMPLETED, null);
        AvatarLifecycleResult result = avatarService.processEvent(request);

        assertNotNull(result);
        assertFalse(result.event().audioAvailable());
        assertEquals("", result.event().text());
        assertNull(result.audioData());
    }

    @Test
    void processEvent_audioSynthesisFails_returnsFallbackResult() {
        ChildSession session = createActiveSession(1L, 100L);
        ChildProfile childProfile = createChildProfile(100L, "Ada", true, true);
        AvatarEventCatalog catalog = createCatalog(AvatarEventType.ACTIVITY_COMPLETED, "Great job!");

        when(childSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(childProfileRepository.findById(100L)).thenReturn(Optional.of(childProfile));
        when(avatarEventCatalogRepository.findByEventType(AvatarEventType.ACTIVITY_COMPLETED))
            .thenReturn(List.of(catalog));
        when(audio.getAudio(any(AudioRequest.class))).thenThrow(new RuntimeException("TTS timeout"));

        AvatarEventRequest request = new AvatarEventRequest(1L, AvatarEventType.ACTIVITY_COMPLETED, null);
        AvatarLifecycleResult result = avatarService.processEvent(request);

        assertNotNull(result);
        assertFalse(result.event().audioAvailable());
        assertEquals("Great job!", result.event().text());
        assertNull(result.audioData());
    }

    @Test
    void processEvent_multipleCatalogEntries_doesNotRepeatConsecutively() {
        ChildSession session = createActiveSession(1L, 100L);
        ChildProfile childProfile = createChildProfile(100L, "Ada", true, true);
        AvatarEventCatalog catalogA = createCatalog(AvatarEventType.ACTIVITY_COMPLETED, "Message A");
        catalogA.setId(1L);
        AvatarEventCatalog catalogB = createCatalog(AvatarEventType.ACTIVITY_COMPLETED, "Message B");
        catalogB.setId(2L);

        when(childSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(childProfileRepository.findById(100L)).thenReturn(Optional.of(childProfile));
        when(avatarEventCatalogRepository.findByEventType(AvatarEventType.ACTIVITY_COMPLETED))
            .thenReturn(List.of(catalogA, catalogB));
        when(audio.getAudio(any(AudioRequest.class))).thenReturn("fake-mp3-data".getBytes());

        var requestCaptor = org.mockito.ArgumentCaptor.forClass(AudioRequest.class);
        AvatarEventRequest request = new AvatarEventRequest(1L, AvatarEventType.ACTIVITY_COMPLETED, null);

        avatarService.processEvent(request);
        avatarService.processEvent(request);
        avatarService.processEvent(request);

        org.mockito.Mockito.verify(audio, org.mockito.Mockito.times(3)).getAudio(requestCaptor.capture());
        var texts = requestCaptor.getAllValues().stream().map((ar) -> ar.text()).toList();
        for (int i = 1; i < texts.size(); i++) {
            assertFalse(texts.get(i).equals(texts.get(i - 1)),
                "Consecutive avatar messages should not repeat when alternatives exist");
        }
    }

    private ChildSession createActiveSession(Long sessionId, Long childProfileId) {
        return createSession(sessionId, childProfileId, ChildSessionStatus.ACTIVE);
    }

    private ChildSession createSession(Long sessionId, Long childProfileId, ChildSessionStatus status) {
        ChildSession session = new ChildSession();
        session.setId(sessionId);
        session.setChildProfileId(childProfileId);
        session.setStatus(status);
        return session;
    }

    private ChildProfile createChildProfile(Long id, String name, boolean npcVoiceEnabled, boolean npcEnabled) {
        ChildProfile profile = new ChildProfile();
        profile.setId(id);
        profile.setName(name);
        profile.setNpcVoiceEnabled(npcVoiceEnabled);
        profile.setNpcEnabled(npcEnabled);
        return profile;
    }

    private AvatarEventCatalog createCatalog(AvatarEventType eventType, String messageText) {
        AvatarEventCatalog catalog = new AvatarEventCatalog();
        catalog.setId(1L);
        catalog.setEventType(eventType);
        catalog.setTone(TonePreset.CALM);
        catalog.setMessageText(messageText);
        catalog.setStatus(ContentStatus.ACTIVE);
        return catalog;
    }
}
