package es.vargontoc.educational.framework.audio.infrastructure.adapters.in;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import es.vargontoc.educational.framework.audio.application.ports.in.AudioTextSource;
import es.vargontoc.educational.framework.audio.application.ports.in.AudioUseCase;
import es.vargontoc.educational.framework.audio.domain.AudioRequest;
import es.vargontoc.educational.framework.audio.domain.enums.TonePreset;

class AudioWarmupServiceTest {

    private static AudioTextSource source(String name, List<AudioRequest> requests) {
        AudioTextSource source = mock(AudioTextSource.class);
        when(source.name()).thenReturn(name);
        when(source.requests()).thenReturn(requests);
        return source;
    }

    @SuppressWarnings("unchecked")
    private static ObjectProvider<AudioTextSource> providerOf(AudioTextSource... sources) {
        ObjectProvider<AudioTextSource> provider = mock(ObjectProvider.class);
        when(provider.orderedStream()).thenAnswer(invocation -> Stream.of(sources));
        return provider;
    }

    private static AudioRequest request(String text) {
        return AudioRequest.withPreset(text, TonePreset.CALM);
    }

    @Test
    void queuesTheTextsOfEverySourceWithoutDuplicates() {
        AudioUseCase audio = mock(AudioUseCase.class);
        var warmup = new AudioWarmupService(audio, providerOf(
            source("a", List.of(request("uno"), request("dos"))),
            source("b", List.of(request("dos"), request("tres")))), true);

        assertEquals(3, warmup.warmUp());

        verify(audio).warm(request("uno"));
        verify(audio).warm(request("dos"));
        verify(audio).warm(request("tres"));
    }

    @Test
    void aFailingSourceDoesNotStopTheOthers() {
        AudioTextSource broken = mock(AudioTextSource.class);
        when(broken.name()).thenReturn("roto");
        when(broken.requests()).thenThrow(new IllegalStateException("BD no disponible"));
        AudioUseCase audio = mock(AudioUseCase.class);
        var warmup = new AudioWarmupService(audio, providerOf(broken, source("ok", List.of(request("uno")))), true);

        assertEquals(1, warmup.warmUp());

        verify(audio).warm(request("uno"));
    }

    @Test
    void blankTextsAreIgnored() {
        AudioUseCase audio = mock(AudioUseCase.class);
        var warmup = new AudioWarmupService(audio, providerOf(
            source("a", List.of(request("  "), request("hola")))), true);

        assertEquals(1, warmup.warmUp());
    }

    @Test
    void doesNothingWhenDisabled() {
        AudioUseCase audio = mock(AudioUseCase.class);
        var warmup = new AudioWarmupService(audio, providerOf(source("a", List.of(request("uno")))), false);

        assertEquals(0, warmup.warmUp());

        verify(audio, never()).warm(any());
    }
}
