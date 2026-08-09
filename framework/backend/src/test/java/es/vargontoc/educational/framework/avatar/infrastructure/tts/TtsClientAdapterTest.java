package es.vargontoc.educational.framework.avatar.infrastructure.tts;

import es.vargontoc.educational.framework.content.model.AvatarTone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TtsClientAdapterTest {

    private TtsProperties properties;
    private TtsToneMapper toneMapper;
    private RestClient.Builder restClientBuilder;

    @BeforeEach
    void setUp() {
        properties = new TtsProperties();
        properties.setBaseUrl("http://localhost:8081");
        properties.setTimeoutMs(5000);
        properties.setEnabled(true);

        toneMapper = new TtsToneMapper();
        restClientBuilder = mock(RestClient.Builder.class);
        when(restClientBuilder.baseUrl(any(String.class))).thenReturn(restClientBuilder);
        when(restClientBuilder.requestFactory(any())).thenReturn(restClientBuilder);
        when(restClientBuilder.build()).thenReturn(mock(RestClient.class));
    }

    @Test
    void synthesize_disabledTts_throwsTtsDisabledException() {
        properties.setEnabled(false);
        TtsClientAdapter adapter = new TtsClientAdapter(restClientBuilder, properties, toneMapper);

        TtsException exception = assertThrows(TtsException.class, () ->
            adapter.synthesize("Hello", "es", AvatarTone.CALM, "npc"));
        assertEquals("TTS_DISABLED", exception.getErrorCode());
        assertEquals(false, exception.isRetryable());
    }
}
