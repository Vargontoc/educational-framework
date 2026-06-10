package es.vargontoc.educational.framework.avatar.application;

import es.vargontoc.educational.framework.avatar.infrastructure.cache.AvatarAudioCache;
import es.vargontoc.educational.framework.avatar.infrastructure.cache.AvatarAudioCacheProperties;
import es.vargontoc.educational.framework.avatar.infrastructure.cache.CachingTtsClient;
import es.vargontoc.educational.framework.avatar.infrastructure.tts.TtsClientAdapter;
import es.vargontoc.educational.framework.avatar.infrastructure.tts.TtsProperties;
import es.vargontoc.educational.framework.avatar.infrastructure.tts.TtsToneMapper;
import es.vargontoc.educational.framework.avatar.ports.in.NarrateStorytellerUseCase;
import es.vargontoc.educational.framework.avatar.ports.out.TtsClient;
import es.vargontoc.educational.framework.avatar.service.AvatarLifecycleService;
import es.vargontoc.educational.framework.avatar.service.AvatarService;
import es.vargontoc.educational.framework.avatar.service.NarrateStorytellerService;
import es.vargontoc.educational.framework.content.ports.out.AvatarEventCatalogRepository;
import es.vargontoc.educational.framework.family.ports.out.ChildProfileRepository;
import es.vargontoc.educational.framework.session.ports.out.ChildSessionRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties({TtsProperties.class, AvatarAudioCacheProperties.class})
class AvatarModuleConfiguration {

    @Bean
    TtsToneMapper ttsToneMapper() {
        return new TtsToneMapper();
    }

    @Bean
    AvatarAudioCache avatarAudioCache(AvatarAudioCacheProperties cacheProperties) {
        return new AvatarAudioCache(cacheProperties);
    }

    @Bean
    TtsClient ttsClientAdapter(
            RestClient.Builder restClientBuilder,
            TtsProperties ttsProperties,
            TtsToneMapper ttsToneMapper,
            AvatarAudioCache cache,
            AvatarAudioCacheProperties cacheProperties) {
        TtsClientAdapter delegate = new TtsClientAdapter(restClientBuilder, ttsProperties, ttsToneMapper);
        if (cacheProperties.isEnabled()) {
            return new CachingTtsClient(delegate, cache, ttsToneMapper, cacheProperties);
        }
        return delegate;
    }

    @Bean
    AvatarService avatarService(
            ChildSessionRepository childSessionRepository,
            ChildProfileRepository childProfileRepository,
            AvatarEventCatalogRepository avatarEventCatalogRepository,
            TtsClient ttsClient) {
        return new AvatarService(childSessionRepository, childProfileRepository, avatarEventCatalogRepository, ttsClient);
    }

    @Bean
    NarrateStorytellerUseCase narrateStorytellerUseCase(TtsClient ttsClient) {
        return new NarrateStorytellerService(ttsClient);
    }

    @Bean
    AvatarLifecycleService avatarLifecycleService(
            TtsClient ttsClient,
            ChildProfileRepository childProfileRepository,
            ChildSessionRepository childSessionRepository) {
        return new AvatarLifecycleService(ttsClient, childProfileRepository, childSessionRepository);
    }
}