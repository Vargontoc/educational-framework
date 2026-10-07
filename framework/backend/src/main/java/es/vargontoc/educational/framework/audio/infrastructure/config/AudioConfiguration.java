package es.vargontoc.educational.framework.audio.infrastructure.config;

import java.nio.file.Path;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import es.vargontoc.educational.framework.audio.application.ports.in.AudioUseCase;
import es.vargontoc.educational.framework.audio.application.ports.out.AudioPort;
import es.vargontoc.educational.framework.audio.infrastructure.adapters.in.AudioAdapter;
import es.vargontoc.educational.framework.audio.infrastructure.adapters.in.AudioAsync;
import es.vargontoc.educational.framework.audio.infrastructure.cache.AudioCacheStorage;
import io.micrometer.core.instrument.MeterRegistry;

@Configuration
@EnableConfigurationProperties(AudioCacheConfiguration.class)
public class AudioConfiguration {

    @Bean("audio-client")
    public RestClient getAudioClient(
            @Value("${app.audio.base-url}") String baseUrl,
            @Value("${app.audio.connect-timeout-seconds:3}") int connectTimeoutSeconds,
            @Value("${app.audio.read-timeout-seconds:90}") int readTimeoutSeconds) {
        // HTTP/1.1 explicito: el HttpClient del JDK intenta por defecto un upgrade h2c sobre HTTP sin cifrar y
        // Uvicorn/FastAPI (Chatterbox) pierde el cuerpo de los POST, respondiendo 422 "body: Field required".
        java.net.http.HttpClient httpClient = java.net.http.HttpClient.newBuilder()
            .version(java.net.http.HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(connectTimeoutSeconds))
            .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(readTimeoutSeconds));
        return RestClient.builder()
            .baseUrl(baseUrl)
            .requestFactory(factory)
            .build();
    }

    @Bean
    public AudioCacheStorage audioCacheStorage(AudioCacheConfiguration properties) {
        return new AudioCacheStorage(Path.of(properties.diskPath()), properties);
    }

    @Bean
    public AudioUseCase audioUseCase(AudioPort audioPort, AudioAsync async, AudioCacheStorage audioCacheStorage, MeterRegistry meterRegistry) {
        return new AudioAdapter(audioPort, async, audioCacheStorage, meterRegistry);
    }
}
