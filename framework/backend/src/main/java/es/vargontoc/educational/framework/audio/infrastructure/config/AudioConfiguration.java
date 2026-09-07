package es.vargontoc.educational.framework.audio.infrastructure.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class AudioConfiguration {
    
    @Bean("audio-client")
    public RestClient getAudioClient(@Value("${app.tts.base-url}") String baseUrl ) {
        return RestClient.builder().baseUrl(baseUrl)
            .requestFactory(new SimpleClientHttpRequestFactory(){
                {
                    setConnectTimeout(Duration.ofSeconds(90));
                    setReadTimeout(Duration.ofSeconds(90));
                }
            })
            .build();
    }
}
