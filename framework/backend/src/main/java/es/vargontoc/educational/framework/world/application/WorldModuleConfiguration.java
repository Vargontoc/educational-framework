package es.vargontoc.educational.framework.world.application;

import es.vargontoc.educational.framework.content.ports.in.WorldCatalogUseCase;
import es.vargontoc.educational.framework.game.ports.in.GameOrchestrator;
import es.vargontoc.educational.framework.game.ports.out.GameStateRegistry;
import es.vargontoc.educational.framework.session.ports.in.ChildSessionUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.RegisterActivityProposalUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.RegisterLearningPathStepProgressUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.ResolveActivityProposalUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.SelectTopicsForDifficultyUseCase;
import es.vargontoc.educational.framework.world.ports.in.EngagementThresholdConfigUseCase;
import es.vargontoc.educational.framework.world.ports.in.WorldActivityProposalUseCase;
import es.vargontoc.educational.framework.world.ports.in.WorldGameStartUseCase;
import es.vargontoc.educational.framework.world.ports.in.WorldHeartbeatUseCase;
import es.vargontoc.educational.framework.world.ports.in.WorldNarrativeCompletionUseCase;
import es.vargontoc.educational.framework.world.ports.in.WorldOrchestrator;
import es.vargontoc.educational.framework.world.ports.in.WorldProposalResolutionUseCase;
import es.vargontoc.educational.framework.world.ports.out.WorldStateRegistry;
import es.vargontoc.educational.framework.world.service.EngagementThresholdConfigService;
import es.vargontoc.educational.framework.world.service.WorldEngagementEvaluator;
import es.vargontoc.educational.framework.world.service.WorldGameCompletionListener;
import es.vargontoc.educational.framework.world.service.WorldGameStartService;
import es.vargontoc.educational.framework.world.service.WorldHeartbeatService;
import es.vargontoc.educational.framework.world.service.WorldInactivityConfig;
import es.vargontoc.educational.framework.world.service.WorldNarrativeCompletionService;
import es.vargontoc.educational.framework.world.service.WorldOrchestratorService;
import es.vargontoc.educational.framework.world.service.WorldProposalService;
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
    WorldInactivityConfig worldInactivityConfig() {
        return new WorldInactivityConfig();
    }

    @Bean
    WorldOrchestrator worldOrchestrator(SelectTopicsForDifficultyUseCase selectTopicsForDifficultyUseCase,
                                        WorldCatalogUseCase worldCatalogUseCase,
                                        EngagementThresholdConfigUseCase engagementThresholdConfigUseCase,
                                        WorldEngagementEvaluator worldEngagementEvaluator) {
        return new WorldOrchestratorService(selectTopicsForDifficultyUseCase, worldCatalogUseCase,
            engagementThresholdConfigUseCase, worldEngagementEvaluator);
    }

    @Bean
    WorldProposalService worldProposalService(RegisterActivityProposalUseCase registerActivityProposalUseCase,
                                              ResolveActivityProposalUseCase resolveActivityProposalUseCase,
                                              WorldStateRegistry worldStateRegistry) {
        return new WorldProposalService(registerActivityProposalUseCase, resolveActivityProposalUseCase, worldStateRegistry);
    }

    @Bean
    WorldGameStartUseCase worldGameStartUseCase(GameOrchestrator gameOrchestrator,
                                               GameStateRegistry gameStateRegistry,
                                               WorldProposalResolutionUseCase worldProposalResolutionUseCase,
                                               WorldOrchestrator worldOrchestrator,
                                               WorldStateRegistry worldStateRegistry) {
        return new WorldGameStartService(gameOrchestrator, gameStateRegistry,
            worldProposalResolutionUseCase, worldOrchestrator, worldStateRegistry);
    }

    @Bean
    WorldNarrativeCompletionUseCase worldNarrativeCompletionUseCase(WorldStateRegistry worldStateRegistry,
                                                                   RegisterLearningPathStepProgressUseCase registerLearningPathStepProgressUseCase) {
        return new WorldNarrativeCompletionService(worldStateRegistry, registerLearningPathStepProgressUseCase);
    }

    @Bean
    WorldGameCompletionListener worldGameCompletionListener(WorldStateRegistry worldStateRegistry) {
        return new WorldGameCompletionListener(worldStateRegistry);
    }

    @Bean
    WorldHeartbeatUseCase worldHeartbeatUseCase(WorldStateRegistry worldStateRegistry,
                                                WorldProposalService worldProposalService,
                                                ChildSessionUseCase childSessionUseCase,
                                                WorldInactivityConfig inactivityConfig) {
        return new WorldHeartbeatService(worldStateRegistry, worldProposalService, childSessionUseCase, inactivityConfig);
    }
}