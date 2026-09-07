package es.vargontoc.educational.framework.audio.infrastructure.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import es.vargontoc.educational.framework.audio.application.ports.in.AudioUseCase;
import es.vargontoc.educational.framework.audio.application.ports.out.AudioPort;
import es.vargontoc.educational.framework.audio.infrastructure.adapters.in.AudioAdapter;
import es.vargontoc.educational.framework.audio.infrastructure.cache.AudioCacheStorage;

@Configuration
@EnableConfigurationProperties(AudioCacheConfiguration.class)
public class AudioConfiguration {

    @Bean("audio-client")
    public RestClient getAudioClient(@Value("${app.audio.base-url}") String baseUrl ) {
        return RestClient.builder().baseUrl(baseUrl)
            .requestFactory(new SimpleClientHttpRequestFactory(){
                {
                    setConnectTimeout(Duration.ofSeconds(90));
                    setReadTimeout(Duration.ofSeconds(90));
                }
            })
            .build();
    }

    @Bean
    public AudioCacheStorage audioCacheStorage(
            @Value("classpath:/stories") Resource cachePath,
            AudioCacheConfiguration properties) {
        return new AudioCacheStorage(cachePath, properties);
    }

    @Bean
    public AudioUseCase audioUseCase(AudioPort audioPort, AudioCacheStorage audioCacheStorage) {
        return new AudioAdapter(audioPort, audioCacheStorage);
    }
}
