package es.vargontoc.educational.framework.game.application;

import es.vargontoc.educational.framework.content.ports.in.GameCatalogUseCase;
import es.vargontoc.educational.framework.content.ports.in.TopicUseCase;
import es.vargontoc.educational.framework.game.ports.in.GameOrchestrator;
import es.vargontoc.educational.framework.game.ports.out.GameStateRegistry;
import es.vargontoc.educational.framework.game.ports.out.SessionAntiRepetitionRegistry;
import es.vargontoc.educational.framework.game.service.GameOrchestratorService;
import es.vargontoc.educational.framework.tracking.ports.in.EvaluateGameCompletionAchievementsUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.FilterAllowedRecognitionCategoriesUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.RegisterActivityAttemptUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.RegisterGameSessionSummaryUseCase;
import es.vargontoc.educational.framework.tracking.ports.out.ElementProgressPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class GameModuleConfiguration {

    @Bean
    GameOrchestrator gameOrchestrator(
            GameCatalogUseCase gameCatalogUseCase,
            GameStateRegistry gameStateRegistry,
            SessionAntiRepetitionRegistry sessionAntiRepetitionRegistry,
            RegisterActivityAttemptUseCase registerActivityAttemptUseCase,
            EvaluateGameCompletionAchievementsUseCase evaluateGameCompletionAchievementsUseCase,
            RegisterGameSessionSummaryUseCase registerGameSessionSummaryUseCase,
            ApplicationEventPublisher eventPublisher,
            TopicUseCase topicUseCase,
            FilterAllowedRecognitionCategoriesUseCase filterAllowedRecognitionCategoriesUseCase,
            ElementProgressPort elementProgressPort) {
        return new GameOrchestratorService(
            gameCatalogUseCase,
            gameStateRegistry,
            sessionAntiRepetitionRegistry,
            registerActivityAttemptUseCase,
            evaluateGameCompletionAchievementsUseCase,
            registerGameSessionSummaryUseCase,
            eventPublisher,
            topicUseCase,
            filterAllowedRecognitionCategoriesUseCase,
            elementProgressPort
        );
    }
}
