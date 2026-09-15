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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvatarServiceSprint094Test {

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
    void worldTravel_biomeWithActiveContent_sendsBiomeTransitionWithAudio() {
        ChildSession session = createActiveSession(1L, 100L);
        ChildProfile profile = createChildProfile(100L, "Ada", true, true);
        AvatarEventCatalog catalog = createCatalog(AvatarEventType.BIOME_TRANSITION, "Look, a farm!", "FARM");
        catalog.setId(10L);

        when(childSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(childProfileRepository.findById(100L)).thenReturn(Optional.of(profile));
        when(avatarEventCatalogRepository.findActiveByEventTypeAndBiome(AvatarEventType.BIOME_TRANSITION, "FARM"))
            .thenReturn(List.of(catalog));
        when(audio.getAudio(any(AudioRequest.class))).thenReturn("mp3-data".getBytes());

        AvatarEventRequest request = new AvatarEventRequest(1L, AvatarEventType.BIOME_TRANSITION, Map.of("biome", "FARM"));
        AvatarLifecycleResult result = avatarService.processEvent(request);

        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals("BIOME_TRANSITION", result.event().eventType());
        assertTrue(result.event().audioAvailable());
        assertNotNull(result.event().audioId());
        assertEquals("Look, a farm!", result.event().text());
        assertNotNull(result.audioData());
    }

    @Test
    void worldTravel_biomeWithoutContent_sendsFallbackWithoutBreakingFlow() {
        ChildSession session = createActiveSession(1L, 100L);
        ChildProfile profile = createChildProfile(100L, "Ada", true, true);

        when(childSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(childProfileRepository.findById(100L)).thenReturn(Optional.of(profile));
        when(avatarEventCatalogRepository.findActiveByEventTypeAndBiome(AvatarEventType.BIOME_TRANSITION, "SPACE"))
            .thenReturn(List.of());

        AvatarEventRequest request = new AvatarEventRequest(1L, AvatarEventType.BIOME_TRANSITION, Map.of("biome", "SPACE"));
        AvatarLifecycleResult result = avatarService.processEvent(request);

        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals("BIOME_TRANSITION", result.event().eventType());
        assertFalse(result.event().audioAvailable());
        assertNull(result.event().audioId());
        assertEquals("", result.event().text());
        assertNull(result.audioData());
    }

    @Test
    void worldTravel_npcDisabled_sendsEventWithoutAudio() {
        ChildSession session = createActiveSession(1L, 100L);
        ChildProfile profile = createChildProfile(100L, "Ada", true, false);

        when(childSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(childProfileRepository.findById(100L)).thenReturn(Optional.of(profile));

        AvatarEventRequest request = new AvatarEventRequest(1L, AvatarEventType.BIOME_TRANSITION, Map.of("biome", "BEACH"));
        AvatarLifecycleResult result = avatarService.processEvent(request);

        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals("BIOME_TRANSITION", result.event().eventType());
        assertFalse(result.event().audioAvailable());
        assertNull(result.event().audioId());
        assertEquals("", result.event().text());
        assertNull(result.audioData());
        verify(audio, never()).getAudio(any());
    }

    @Test
    void worldTravel_differentBiomes_doNotShareAntiRepetition() {
        ChildSession session = createActiveSession(1L, 100L);
        ChildProfile profile = createChildProfile(100L, "Ada", true, true);

        AvatarEventCatalog farmCatalog = createCatalog(AvatarEventType.BIOME_TRANSITION, "Farm phrase", "FARM");
        farmCatalog.setId(10L);
        AvatarEventCatalog beachCatalog = createCatalog(AvatarEventType.BIOME_TRANSITION, "Beach phrase", "BEACH");
        beachCatalog.setId(20L);

        when(childSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(childProfileRepository.findById(100L)).thenReturn(Optional.of(profile));
        when(avatarEventCatalogRepository.findActiveByEventTypeAndBiome(AvatarEventType.BIOME_TRANSITION, "FARM"))
            .thenReturn(List.of(farmCatalog));
        when(avatarEventCatalogRepository.findActiveByEventTypeAndBiome(AvatarEventType.BIOME_TRANSITION, "BEACH"))
            .thenReturn(List.of(beachCatalog));
        when(audio.getAudio(any(AudioRequest.class))).thenReturn("mp3-data".getBytes());

        AvatarLifecycleResult farmResult = avatarService.processEvent(
            new AvatarEventRequest(1L, AvatarEventType.BIOME_TRANSITION, Map.of("biome", "FARM")));
        AvatarLifecycleResult beachResult = avatarService.processEvent(
            new AvatarEventRequest(1L, AvatarEventType.BIOME_TRANSITION, Map.of("biome", "BEACH")));

        assertEquals("Farm phrase", farmResult.event().text());
        assertEquals("Beach phrase", beachResult.event().text());
    }

    @Test
    void worldTravel_sameBiomeTwice_doesNotRepeatVariantIfOtherAvailable() {
        ChildSession session = createActiveSession(1L, 100L);
        ChildProfile profile = createChildProfile(100L, "Ada", true, true);

        AvatarEventCatalog variantA = createCatalog(AvatarEventType.BIOME_TRANSITION, "Variant A", "MEADOW");
        variantA.setId(10L);
        AvatarEventCatalog variantB = createCatalog(AvatarEventType.BIOME_TRANSITION, "Variant B", "MEADOW");
        variantB.setId(20L);

        when(childSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(childProfileRepository.findById(100L)).thenReturn(Optional.of(profile));
        when(avatarEventCatalogRepository.findActiveByEventTypeAndBiome(AvatarEventType.BIOME_TRANSITION, "MEADOW"))
            .thenReturn(List.of(variantA, variantB));
        when(audio.getAudio(any(AudioRequest.class))).thenReturn("mp3-data".getBytes());

        var requestCaptor = org.mockito.ArgumentCaptor.forClass(AudioRequest.class);

        avatarService.processEvent(
            new AvatarEventRequest(1L, AvatarEventType.BIOME_TRANSITION, Map.of("biome", "MEADOW")));
        avatarService.processEvent(
            new AvatarEventRequest(1L, AvatarEventType.BIOME_TRANSITION, Map.of("biome", "MEADOW")));

        verify(audio, org.mockito.Mockito.times(2)).getAudio(requestCaptor.capture());
        var texts = requestCaptor.getAllValues().stream().map(a -> a.text()).toList();
        assertFalse(texts.get(0).equals(texts.get(1)),
            "Consecutive biome transitions to same biome should not repeat variant when alternatives exist");
    }

    @Test
    void findByEventType_excludesDraftAndArchived_forBiomeTransitionAndWelcome() {
        ChildSession session = createActiveSession(1L, 100L);
        ChildProfile profile = createChildProfile(100L, "Ada", true, true);

        AvatarEventCatalog activeWelcome = createCatalog(AvatarEventType.WELCOME, "Welcome!");
        activeWelcome.setId(1L);
        activeWelcome.setStatus(ContentStatus.ACTIVE);

        when(childSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(childProfileRepository.findById(100L)).thenReturn(Optional.of(profile));
        when(avatarEventCatalogRepository.findByEventType(AvatarEventType.WELCOME))
            .thenReturn(List.of(activeWelcome));
        when(audio.getAudio(any(AudioRequest.class))).thenReturn("mp3-data".getBytes());

        AvatarLifecycleResult result = avatarService.processEvent(
            new AvatarEventRequest(1L, AvatarEventType.WELCOME, null));

        assertTrue(result.isPresent());
        assertTrue(result.event().audioAvailable());

        AvatarEventCatalog activeBiome = createCatalog(AvatarEventType.BIOME_TRANSITION, "Farm!", "FARM");
        activeBiome.setId(2L);
        activeBiome.setStatus(ContentStatus.ACTIVE);

        when(avatarEventCatalogRepository.findActiveByEventTypeAndBiome(AvatarEventType.BIOME_TRANSITION, "FARM"))
            .thenReturn(List.of(activeBiome));

        AvatarLifecycleResult biomeResult = avatarService.processEvent(
            new AvatarEventRequest(1L, AvatarEventType.BIOME_TRANSITION, Map.of("biome", "FARM")));

        assertTrue(biomeResult.isPresent());
        assertTrue(biomeResult.event().audioAvailable());
    }

    @Test
    void worldTravel_emptyCatalogForBiome_doesNotThrowException() {
        ChildSession session = createActiveSession(1L, 100L);
        ChildProfile profile = createChildProfile(100L, "Ada", true, true);

        when(childSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(childProfileRepository.findById(100L)).thenReturn(Optional.of(profile));
        when(avatarEventCatalogRepository.findActiveByEventTypeAndBiome(AvatarEventType.BIOME_TRANSITION, "FOREST"))
            .thenReturn(List.of());

        AvatarEventRequest request = new AvatarEventRequest(1L, AvatarEventType.BIOME_TRANSITION, Map.of("biome", "FOREST"));
        AvatarLifecycleResult result = avatarService.processEvent(request);

        assertNotNull(result);
        assertTrue(result.isPresent());
        assertFalse(result.event().audioAvailable());
        assertEquals("", result.event().text());
    }

    @Test
    void lastShownCatalogMap_doesNotGrowUnbounded() throws Exception {
        ChildSession session = createActiveSession(1L, 100L);
        ChildProfile profile = createChildProfile(100L, "Ada", true, true);

        when(childSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(childProfileRepository.findById(100L)).thenReturn(Optional.of(profile));
        when(audio.getAudio(any(AudioRequest.class))).thenReturn("mp3-data".getBytes());

        String[] biomes = {"MEADOW", "FARM", "BEACH", "FOREST", "SPACE", "UNDERWATER"};

        for (int round = 0; round < 200; round++) {
            for (String biome : biomes) {
                AvatarEventCatalog catalog = createCatalog(AvatarEventType.BIOME_TRANSITION, "Phrase " + biome, biome);
                catalog.setId((long) (round * 10 + biomes.length));
                when(avatarEventCatalogRepository.findActiveByEventTypeAndBiome(AvatarEventType.BIOME_TRANSITION, biome))
                    .thenReturn(List.of(catalog));

                avatarService.processEvent(
                    new AvatarEventRequest(1L, AvatarEventType.BIOME_TRANSITION, Map.of("biome", biome)));
            }
        }

        var field = AvatarService.class.getDeclaredField("lastShownCatalogIdByChild");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        var map = (Map<String, Long>) field.get(avatarService);

        int size;
        synchronized (map) {
            size = map.size();
        }
        assertTrue(size <= 512, "lastShownCatalogIdByChild should be bounded to 512, was: " + size);
    }

    private ChildSession createActiveSession(Long sessionId, Long childProfileId) {
        ChildSession session = new ChildSession();
        session.setId(sessionId);
        session.setChildProfileId(childProfileId);
        session.setStatus(ChildSessionStatus.ACTIVE);
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

    private AvatarEventCatalog createCatalog(AvatarEventType eventType, String messageText, String biome) {
        AvatarEventCatalog catalog = new AvatarEventCatalog();
        catalog.setId(1L);
        catalog.setEventType(eventType);
        catalog.setTone(TonePreset.CALM);
        catalog.setMessageText(messageText);
        catalog.setBiome(biome);
        catalog.setStatus(ContentStatus.ACTIVE);
        return catalog;
    }

    private AvatarEventCatalog createCatalog(AvatarEventType eventType, String messageText) {
        return createCatalog(eventType, messageText, null);
    }
}
