package es.vargontoc.educational.framework.avatar.application;

import es.vargontoc.educational.framework.avatar.infrastructure.tts.TtsClientAdapter;
import es.vargontoc.educational.framework.avatar.infrastructure.tts.TtsProperties;
import es.vargontoc.educational.framework.avatar.infrastructure.tts.TtsToneMapper;
import es.vargontoc.educational.framework.avatar.ports.out.TtsClient;
import es.vargontoc.educational.framework.avatar.service.AvatarService;
import es.vargontoc.educational.framework.content.ports.out.AvatarEventCatalogRepository;
import es.vargontoc.educational.framework.family.ports.out.ChildProfileRepository;
import es.vargontoc.educational.framework.session.ports.out.ChildSessionRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(TtsProperties.class)
class AvatarModuleConfiguration {

    @Bean
    TtsToneMapper ttsToneMapper() {
        return new TtsToneMapper();
    }

    @Bean
    TtsClient ttsClientAdapter(RestClient.Builder restClientBuilder, TtsProperties ttsProperties, TtsToneMapper ttsToneMapper) {
        return new TtsClientAdapter(restClientBuilder, ttsProperties, ttsToneMapper);
    }

    @Bean
    AvatarService avatarService(
            ChildSessionRepository childSessionRepository,
            ChildProfileRepository childProfileRepository,
            AvatarEventCatalogRepository avatarEventCatalogRepository,
            TtsClient ttsClient) {
        return new AvatarService(childSessionRepository, childProfileRepository, avatarEventCatalogRepository, ttsClient);
    }
}