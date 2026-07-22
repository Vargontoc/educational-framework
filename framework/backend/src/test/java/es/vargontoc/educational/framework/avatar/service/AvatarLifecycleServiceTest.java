package es.vargontoc.educational.framework.avatar.service;

import es.vargontoc.educational.framework.avatar.infrastructure.tts.TtsException;
import es.vargontoc.educational.framework.avatar.ports.out.TtsClient;
import es.vargontoc.educational.framework.content.model.AvatarTone;
import es.vargontoc.educational.framework.family.model.ChildProfile;
import es.vargontoc.educational.framework.family.ports.out.ChildProfileRepository;
import es.vargontoc.educational.framework.session.model.ChildSession;
import es.vargontoc.educational.framework.session.model.ChildSessionStatus;
import es.vargontoc.educational.framework.session.ports.out.ChildSessionRepository;
import es.vargontoc.educational.framework.session.infrastructure.websocket.SessionEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvatarLifecycleServiceTest {

    @Mock
    private TtsClient ttsClient;

    @Mock
    private ChildProfileRepository profileRepository;

    @Mock
    private ChildSessionRepository sessionRepository;

    private AvatarLifecycleService service;

    @BeforeEach
    void setUp() {
        service = new AvatarLifecycleService(ttsClient, profileRepository, sessionRepository);
    }

    @Test
    void welcome_usesNpcContext() {
        ChildSession session = createSession(1L, 100L);
        ChildProfile profile = createProfile(100L, true, true);
        byte[] audioData = "mp3-data".getBytes();

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(profileRepository.findById(100L)).thenReturn(Optional.of(profile));
        when(ttsClient.synthesize(anyString(), anyString(), any(), eq("npc"))).thenReturn(audioData);

        AvatarLifecycleService.AvatarLifecycleResult result = service.welcome(1L);

        assertTrue(result.isPresent());
        ArgumentCaptor<String> contextCaptor = ArgumentCaptor.forClass(String.class);
        verify(ttsClient).synthesize(anyString(), anyString(), any(), contextCaptor.capture());
        assertEquals("npc", contextCaptor.getValue());
    }

    @Test
    void welcome_agentDisabled_suppressesMessage() {
        ChildSession session = createSession(1L, 100L);
        ChildProfile profile = createProfile(100L, true, false);

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(profileRepository.findById(100L)).thenReturn(Optional.of(profile));

        AvatarLifecycleService.AvatarLifecycleResult result = service.welcome(1L);

        assertFalse(result.isPresent());
        verify(ttsClient, never()).synthesize(anyString(), anyString(), any(), anyString());
    }

    @Test
    void welcome_ttsDisabled_sendsTextOnly() {
        ChildSession session = createSession(1L, 100L);
        ChildProfile profile = createProfile(100L, false, true);

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(profileRepository.findById(100L)).thenReturn(Optional.of(profile));

        AvatarLifecycleService.AvatarLifecycleResult result = service.welcome(1L);

        assertTrue(result.isPresent());
        assertFalse(result.event().audioAvailable());
        assertNull(result.event().audioId());
        assertEquals("Hola, vamos a jugar!", result.event().text());
        assertNull(result.audioData());
        verify(ttsClient, never()).synthesize(anyString(), anyString(), any(), anyString());
    }

    @Test
    void welcome_ttsFailure_returnsTextFallback() {
        ChildSession session = createSession(1L, 100L);
        ChildProfile profile = createProfile(100L, true, true);

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(profileRepository.findById(100L)).thenReturn(Optional.of(profile));
        when(ttsClient.synthesize(anyString(), anyString(), any(), anyString()))
            .thenThrow(new TtsException("TTS timeout", "CONNECTION_ERROR", true));

        AvatarLifecycleService.AvatarLifecycleResult result = service.welcome(1L);

        assertTrue(result.isPresent());
        assertFalse(result.event().audioAvailable());
        assertNull(result.event().audioId());
        assertEquals("Hola, vamos a jugar!", result.event().text());
        assertNull(result.audioData());
    }

    @Test
    void welcome_sessionNotFound_returnsNoSession() {
        when(sessionRepository.findById(999L)).thenReturn(Optional.empty());

        AvatarLifecycleService.AvatarLifecycleResult result = service.welcome(999L);

        assertFalse(result.isPresent());
        verify(ttsClient, never()).synthesize(anyString(), anyString(), any(), anyString());
    }

    @Test
    void farewell_usesNpcContext() {
        ChildSession session = createSession(1L, 100L);
        ChildProfile profile = createProfile(100L, true, true);
        byte[] audioData = "mp3-data".getBytes();

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(profileRepository.findById(100L)).thenReturn(Optional.of(profile));
        when(ttsClient.synthesize(anyString(), anyString(), any(), eq("npc"))).thenReturn(audioData);

        AvatarLifecycleService.AvatarLifecycleResult result = service.farewell(1L);

        assertTrue(result.isPresent());
        ArgumentCaptor<String> contextCaptor = ArgumentCaptor.forClass(String.class);
        verify(ttsClient).synthesize(anyString(), anyString(), any(), contextCaptor.capture());
        assertEquals("npc", contextCaptor.getValue());
    }

    @Test
    void farewell_agentDisabled_suppressesMessage() {
        ChildSession session = createSession(1L, 100L);
        ChildProfile profile = createProfile(100L, true, false);

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(profileRepository.findById(100L)).thenReturn(Optional.of(profile));

        AvatarLifecycleService.AvatarLifecycleResult result = service.farewell(1L);

        assertFalse(result.isPresent());
        verify(ttsClient, never()).synthesize(anyString(), anyString(), any(), anyString());
    }

    private ChildSession createSession(Long sessionId, Long childProfileId) {
        ChildSession session = new ChildSession();
        session.setId(sessionId);
        session.setChildProfileId(childProfileId);
        session.setStatus(ChildSessionStatus.ACTIVE);
        return session;
    }

    private ChildProfile createProfile(Long id, boolean ttsEnabled, boolean agentEnabled) {
        ChildProfile profile = new ChildProfile();
        profile.setId(id);
        profile.setTtsEnabled(ttsEnabled);
        profile.setAgentEnabled(agentEnabled);
        return profile;
    }
}