package es.vargontoc.educational.framework.game.application;

import es.vargontoc.educational.framework.content.ports.in.GameCatalogUseCase;
import es.vargontoc.educational.framework.game.ports.out.GameStateRegistry;
import es.vargontoc.educational.framework.game.service.GameOrchestratorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
class GameModuleConfiguration {

    @Bean
    GameOrchestratorService gameOrchestratorService(
            GameCatalogUseCase gameCatalogUseCase,
            GameStateRegistry gameStateRegistry,
            Environment environment) {
        return new GameOrchestratorService(gameCatalogUseCase, gameStateRegistry, environment);
    }
}
