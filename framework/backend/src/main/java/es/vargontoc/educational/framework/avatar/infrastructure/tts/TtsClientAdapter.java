package es.vargontoc.educational.framework.avatar.infrastructure.tts;

import es.vargontoc.educational.framework.avatar.ports.out.TtsClient;
import es.vargontoc.educational.framework.content.model.AvatarTone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.time.Duration;

public class TtsClientAdapter implements TtsClient {

    private static final Logger log = LoggerFactory.getLogger(TtsClientAdapter.class);

    private static final String SYNTHESIZE_PATH = "/api/v1/tts/synthesize";
    private static final String AUDIO_MPEG = "audio/mpeg";

    private final RestClient restClient;
    private final TtsProperties properties;
    private final TtsToneMapper toneMapper;

    public TtsClientAdapter(RestClient.Builder restClientBuilder, TtsProperties properties, TtsToneMapper toneMapper) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(Duration.ofMillis(properties.getTimeoutMs()));
        this.restClient = restClientBuilder
            .baseUrl(properties.getBaseUrl())
            .requestFactory(factory)
            .build();
        this.properties = properties;
        this.toneMapper = toneMapper;
    }

    @Override
    public byte[] synthesize(String text, String locale, AvatarTone tone, String voiceProfile) {
        if (!properties.isEnabled()) {
            throw new TtsException("TTS is disabled", "TTS_DISABLED", false);
        }

        String toneStr = toneMapper.map(tone);
        TtsSynthesizeRequest request = new TtsSynthesizeRequest(text, locale, toneStr, voiceProfile);

        log.debug("Synthesizing TTS: text={}, locale={}, tone={}, voiceProfile={}",
            text, locale, toneStr, voiceProfile);

        try {
            ResponseEntity<byte[]> responseEntity = restClient.post()
                .uri(SYNTHESIZE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, AUDIO_MPEG)
                .body(request)
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), (request2, response) -> {
                    throw HttpClientErrorException.create(
                        HttpStatus.valueOf(response.getStatusCode().value()),
                        "TTS HTTP error",
                        HttpHeaders.EMPTY,
                        new byte[0],
                        null);
                })
                .toEntity(byte[].class);

            String contentType = responseEntity.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
            if (contentType == null || !contentType.startsWith(AUDIO_MPEG)) {
                throw new TtsException("Unexpected content type: " + contentType, "UNEXPECTED_CONTENT_TYPE", false);
            }

            byte[] audioBytes = responseEntity.getBody();
            if (audioBytes == null || audioBytes.length == 0) {
                throw new TtsException("Empty audio response from TTS service", "EMPTY_RESPONSE", false);
            }

            log.debug("TTS synthesis successful: {} bytes", audioBytes.length);
            return audioBytes;

        } catch (HttpClientErrorException e) {
            int status = e.getStatusCode().value();
            String code = classifyHttpError(status);
            log.warn("TTS client error {}: {}", status, e.getMessage());
            throw new TtsException("TTS HTTP client error: " + status, code, false);

        } catch (HttpServerErrorException e) {
            int status = e.getStatusCode().value();
            String code = classifyHttpError(status);
            log.warn("TTS server error {}: {}", status, e.getMessage());
            throw new TtsException("TTS HTTP server error: " + status, code, false);

        } catch (ResourceAccessException e) {
            log.warn("TTS connection failure: {}", e.getMessage());
            throw new TtsException("TTS connection failure: " + e.getMessage(), "CONNECTION_ERROR", true);

        } catch (Exception e) {
            if (e instanceof TtsException) {
                throw e;
            }
            log.error("TTS unexpected error: {}", e.getMessage(), e);
            throw new TtsException("TTS unexpected error: " + e.getMessage(), "UNKNOWN", false);
        }
    }

    private String classifyHttpError(int status) {
        return switch (status) {
            case 400 -> "BAD_REQUEST";
            case 401, 403 -> "AUTH_ERROR";
            case 404 -> "NOT_FOUND";
            case 422 -> "VALIDATION_ERROR";
            case 429 -> "RATE_LIMITED";
            case 500 -> "INTERNAL_ERROR";
            case 502, 503, 504 -> "SERVICE_ERROR";
            default -> "HTTP_ERROR_" + status;
        };
    }
}