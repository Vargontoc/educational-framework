package es.vargontoc.educational.framework.world.application;

import es.vargontoc.educational.framework.content.ports.in.WorldCatalogUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.SelectTopicsForDifficultyUseCase;
import es.vargontoc.educational.framework.world.ports.in.EngagementThresholdConfigUseCase;
import es.vargontoc.educational.framework.world.ports.in.WorldOrchestrator;
import es.vargontoc.educational.framework.world.service.EngagementThresholdConfigService;
import es.vargontoc.educational.framework.world.service.WorldEngagementEvaluator;
import es.vargontoc.educational.framework.world.service.WorldOrchestratorService;
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

    @Bean
    WorldOrchestrator worldOrchestrator(SelectTopicsForDifficultyUseCase selectTopicsForDifficultyUseCase,
                                        WorldCatalogUseCase worldCatalogUseCase,
                                        EngagementThresholdConfigUseCase engagementThresholdConfigUseCase,
                                        WorldEngagementEvaluator worldEngagementEvaluator) {
        return new WorldOrchestratorService(selectTopicsForDifficultyUseCase, worldCatalogUseCase,
            engagementThresholdConfigUseCase, worldEngagementEvaluator);
    }
}