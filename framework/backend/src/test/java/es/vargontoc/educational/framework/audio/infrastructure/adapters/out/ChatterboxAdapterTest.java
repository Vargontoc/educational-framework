package es.vargontoc.educational.framework.audio.infrastructure.adapters.out;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;

import es.vargontoc.educational.framework.audio.domain.AudioRequest;
import es.vargontoc.educational.framework.shared.exception.AppException;

class ChatterboxAdapterTest {

    @Test
    void isAvailable_returnsFalseWhenServiceDown() {
        RestClient client = RestClient.builder()
            .baseUrl("http://localhost:1")
            .build();
        ChatterboxAdapter adapter = new ChatterboxAdapter(client, 0);

        assertFalse(adapter.isAvailable());
    }

    @Test
    void synthesizeAudio_throwsWhenServiceDown() {
        RestClient client = RestClient.builder()
            .baseUrl("http://localhost:1")
            .build();
        ChatterboxAdapter adapter = new ChatterboxAdapter(client, 0);

        AudioRequest request = AudioRequest.withPreset("test", es.vargontoc.educational.framework.audio.domain.enums.TonePreset.CALM);
        AppException ex = assertThrows(AppException.class, () -> adapter.synthesizeAudio(request));
        assertTrue(ex.getStatus() == HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void isAvailable_cachesResultWithinTtl() {
        RestClient client = RestClient.builder()
            .baseUrl("http://localhost:1")
            .build();
        ChatterboxAdapter adapter = new ChatterboxAdapter(client, 60);

        long start = System.currentTimeMillis();
        boolean first = adapter.isAvailable();
        long firstDuration = System.currentTimeMillis() - start;

        boolean second = adapter.isAvailable();
        long secondDuration = System.currentTimeMillis() - start - firstDuration;

        assertFalse(first);
        assertFalse(second);
        assertTrue(secondDuration < firstDuration, "Second call should be faster (cached)");
    }
}
