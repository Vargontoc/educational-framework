package es.vargontoc.educational.framework.avatar.infrastructure.config;


import es.vargontoc.educational.framework.audio.application.ports.in.AudioUseCase;
import es.vargontoc.educational.framework.avatar.infrastructure.service.AvatarLifecycleService;
import es.vargontoc.educational.framework.avatar.infrastructure.service.AvatarService;
import es.vargontoc.educational.framework.content.ports.out.AvatarEventCatalogRepository;
import es.vargontoc.educational.framework.family.ports.out.ChildProfileRepository;
import es.vargontoc.educational.framework.session.ports.out.ChildSessionRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties()
class AvatarModuleConfiguration {

    @Bean
    AvatarService avatarService(
            ChildSessionRepository childSessionRepository,
            ChildProfileRepository childProfileRepository,
            AvatarEventCatalogRepository avatarEventCatalogRepository,
            AudioUseCase audio) {
        return new AvatarService(childSessionRepository, childProfileRepository, avatarEventCatalogRepository, audio);
    }

    @Bean
    AvatarLifecycleService avatarLifecycleService(
            ChildProfileRepository childProfileRepository,
            ChildSessionRepository childSessionRepository, 
            AvatarEventCatalogRepository catalogRepository) {
        return new AvatarLifecycleService(childProfileRepository, childSessionRepository, catalogRepository);
    }
}