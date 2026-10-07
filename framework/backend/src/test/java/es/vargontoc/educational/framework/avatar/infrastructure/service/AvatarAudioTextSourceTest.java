package es.vargontoc.educational.framework.avatar.infrastructure.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import es.vargontoc.educational.framework.audio.domain.AudioRequest;
import es.vargontoc.educational.framework.audio.domain.enums.TonePreset;
import es.vargontoc.educational.framework.content.model.AvatarEventCatalog;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.ports.out.AvatarEventCatalogRepository;
import es.vargontoc.educational.framework.family.model.ChildProfile;
import es.vargontoc.educational.framework.family.ports.out.ChildProfileRepository;

class AvatarAudioTextSourceTest {

    private static AvatarEventCatalog event(String text, TonePreset tone, ContentStatus status) {
        AvatarEventCatalog event = new AvatarEventCatalog();
        event.setMessageText(text);
        event.setTone(tone);
        event.setStatus(status);
        return event;
    }

    private static ChildProfile child(String name) {
        ChildProfile profile = new ChildProfile();
        profile.setName(name);
        return profile;
    }

    private AvatarAudioTextSource source(List<AvatarEventCatalog> events, List<ChildProfile> children) {
        AvatarEventCatalogRepository catalog = mock(AvatarEventCatalogRepository.class);
        ChildProfileRepository profiles = mock(ChildProfileRepository.class);
        when(catalog.findAll()).thenReturn(events);
        when(profiles.findAll()).thenReturn(children);
        return new AvatarAudioTextSource(catalog, profiles);
    }

    @Test
    void fixedTextsAreEmittedOnce() {
        var requests = source(
            List.of(event("Hasta pronto.", TonePreset.CALM, ContentStatus.ACTIVE)),
            List.of(child("Angela"), child("Leo"))).requests();

        assertEquals(1, requests.size());
        assertEquals("Hasta pronto.", requests.get(0).text());
    }

    @Test
    void nameTemplatesAreExpandedForEveryExistingProfile() {
        var requests = source(
            List.of(event("¡Hola <name>! Vamos a jugar.", TonePreset.CALM, ContentStatus.ACTIVE)),
            List.of(child("Angela"), child("Leo"), child("Angela"))).requests();

        assertEquals(List.of("¡Hola Angela! Vamos a jugar.", "¡Hola Leo! Vamos a jugar."),
            requests.stream().map(AudioRequest::text).toList());
    }

    @Test
    void nameTemplatesWithoutProfilesProduceNothing() {
        var requests = source(
            List.of(event("¡Hola <name>!", TonePreset.CALM, ContentStatus.ACTIVE)), List.of()).requests();

        assertEquals(0, requests.size());
    }

    @Test
    void inactiveOrIncompleteEventsAreSkipped() {
        var requests = source(List.of(
            event("Inactivo", TonePreset.CALM, ContentStatus.INACTIVE),
            event("   ", TonePreset.CALM, ContentStatus.ACTIVE),
            event("Sin tono", null, ContentStatus.ACTIVE),
            event("Activo", TonePreset.CALM, ContentStatus.ACTIVE)), List.of()).requests();

        assertEquals(List.of("Activo"), requests.stream().map(AudioRequest::text).toList());
    }
}
