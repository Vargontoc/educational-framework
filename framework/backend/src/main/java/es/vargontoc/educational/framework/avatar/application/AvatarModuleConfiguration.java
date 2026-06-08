package es.vargontoc.educational.framework.avatar.application;

import es.vargontoc.educational.framework.avatar.service.AvatarService;
import es.vargontoc.educational.framework.content.ports.out.AvatarEventCatalogRepository;
import es.vargontoc.educational.framework.family.ports.out.ChildProfileRepository;
import es.vargontoc.educational.framework.session.ports.out.ChildSessionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class AvatarModuleConfiguration {

    @Bean
    AvatarService avatarService(
            ChildSessionRepository childSessionRepository,
            ChildProfileRepository childProfileRepository,
            AvatarEventCatalogRepository avatarEventCatalogRepository) {
        return new AvatarService(childSessionRepository, childProfileRepository, avatarEventCatalogRepository);
    }
}