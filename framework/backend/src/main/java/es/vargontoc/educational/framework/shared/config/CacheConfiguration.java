package es.vargontoc.educational.framework.shared.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import es.vargontoc.educational.framework.shared.infrastructure.GameCacheStorage;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties(CacheProperties.class)
public class CacheConfiguration {

    @Bean
    public GameCacheStorage gameCacheStorage(CacheProperties props) {
        return new GameCacheStorage(
            Caffeine.newBuilder()
                .maximumSize(props.recognitionElementMaxSize())
                .expireAfterWrite(props.recognitionElementTtlMinutes(), TimeUnit.MINUTES)
                .recordStats()
                .build(),
            Caffeine.newBuilder()
                .maximumSize(props.accessibleColorMaxSize())
                .expireAfterWrite(props.accessibleColorTtlMinutes(), TimeUnit.MINUTES)
                .recordStats()
                .build(),
            Caffeine.newBuilder()
                .maximumSize(props.accessibleColorMaxSize())
                .expireAfterWrite(props.accessibleColorTtlMinutes(), TimeUnit.MINUTES)
                .recordStats()
                .build(),
            Caffeine.newBuilder()
                .maximumSize(props.worldCatalogMaxSize())
                .expireAfterWrite(props.worldCatalogTtlMinutes(), TimeUnit.MINUTES)
                .recordStats()
                .build(),
            Caffeine.newBuilder()
                .maximumSize(props.avatarEventMaxSize())
                .expireAfterWrite(props.avatarEventTtlMinutes(), TimeUnit.MINUTES)
                .recordStats()
                .build(),
            Caffeine.newBuilder()
                .maximumSize(props.childProfileMaxSize())
                .expireAfterWrite(props.childProfileTtlMinutes(), TimeUnit.MINUTES)
                .recordStats()
                .build()
        );
    }
}
