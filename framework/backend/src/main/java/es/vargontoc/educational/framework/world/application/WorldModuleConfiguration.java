package es.vargontoc.educational.framework.world.application;

import es.vargontoc.educational.framework.world.ports.in.EngagementThresholdConfigUseCase;
import es.vargontoc.educational.framework.world.service.EngagementThresholdConfigService;
import es.vargontoc.educational.framework.world.service.WorldEngagementEvaluator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class WorldModuleConfiguration {

    @Bean
    EngagementThresholdConfigUseCase engagementThresholdConfigUseCase() {
        return new EngagementThresholdConfigService();
    }

    @Bean
    WorldEngagementEvaluator worldEngagementEvaluator() {
        return new WorldEngagementEvaluator();
    }
}
