package es.vargontoc.educational.framework.world.service;

import es.vargontoc.educational.framework.game.exception.EngineNotAvailableException;
import es.vargontoc.educational.framework.game.model.GameState;
import es.vargontoc.educational.framework.game.ports.in.GameOrchestrator;
import es.vargontoc.educational.framework.game.ports.out.GameStateRegistry;
import es.vargontoc.educational.framework.tracking.model.ActivityProposalOutcome;
import es.vargontoc.educational.framework.world.model.WorldDestination;
import es.vargontoc.educational.framework.world.model.WorldDestinationSelectionResult;
import es.vargontoc.educational.framework.world.model.WorldGameStartResult;
import es.vargontoc.educational.framework.world.model.WorldGameStartStatus;
import es.vargontoc.educational.framework.world.model.WorldState;
import es.vargontoc.educational.framework.world.ports.in.WorldGameStartUseCase;
import es.vargontoc.educational.framework.world.ports.in.WorldOrchestrator;
import es.vargontoc.educational.framework.world.ports.in.WorldProposalResolutionUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class WorldGameStartService implements WorldGameStartUseCase {

    private static final Logger log = LoggerFactory.getLogger(WorldGameStartService.class);

    private final GameOrchestrator gameOrchestrator;
    private final GameStateRegistry gameStateRegistry;
    private final WorldProposalResolutionUseCase worldProposalResolutionUseCase;
    private final WorldOrchestrator worldOrchestrator;
    private final es.vargontoc.educational.framework.world.ports.out.WorldStateRegistry worldStateRegistry;

    public WorldGameStartService(GameOrchestrator gameOrchestrator,
                               GameStateRegistry gameStateRegistry,
                               WorldProposalResolutionUseCase worldProposalResolutionUseCase,
                               WorldOrchestrator worldOrchestrator,
                               es.vargontoc.educational.framework.world.ports.out.WorldStateRegistry worldStateRegistry) {
        this.gameOrchestrator = gameOrchestrator;
        this.gameStateRegistry = gameStateRegistry;
        this.worldProposalResolutionUseCase = worldProposalResolutionUseCase;
        this.worldOrchestrator = worldOrchestrator;
        this.worldStateRegistry = worldStateRegistry;
    }

    @Override
    public WorldGameStartResult startGameFromProposal(Long childSessionId, Long activityId) {
        Optional<WorldState> worldStateOpt = worldStateRegistry.findByChildSessionId(childSessionId);

        if (worldStateOpt.isEmpty()) {
            log.warn("World state not found for childSessionId={}", childSessionId);
            return buildFallbackResult(childSessionId, activityId, WorldGameStartStatus.BLOCKED);
        }

        WorldState worldState = worldStateOpt.get();
        Long childProfileId = worldState.getChildProfileId();

        if (gameStateRegistry.hasActiveGameForChildSession(childSessionId)) {
            Optional<GameState> existingGame = gameStateRegistry.findByChildSessionId(childSessionId);
            return new WorldGameStartResult(
                childSessionId,
                activityId,
                existingGame.map(GameState::getGameId).orElse(null),
                WorldGameStartStatus.EXISTING_GAME_ACTIVE,
                null
            );
        }

        try {
            GameState startedGame = gameOrchestrator.startGame(childProfileId, activityId);

            worldProposalResolutionUseCase.resolveProposal(childSessionId, ActivityProposalOutcome.STARTED);

            return new WorldGameStartResult(
                childSessionId,
                activityId,
                startedGame.getGameId(),
                WorldGameStartStatus.STARTED,
                null
            );

        } catch (EngineNotAvailableException e) {
            log.warn("Engine not available for activityId={}: {}", activityId, e.getMessage());
            return buildFallbackResult(childSessionId, activityId, WorldGameStartStatus.FALLBACK_DESTINATION);

        } catch (Exception e) {
            log.warn("Failed to start game for activityId={}: {}", activityId, e.getMessage());
            return buildFallbackResult(childSessionId, activityId, WorldGameStartStatus.FALLBACK_DESTINATION);
        }
    }

    private WorldGameStartResult buildFallbackResult(Long childSessionId, Long activityId, WorldGameStartStatus status) {
        Optional<WorldState> worldStateOpt = worldStateRegistry.findByChildSessionId(childSessionId);

        WorldDestination fallbackDestination = null;

        if (worldStateOpt.isPresent()) {
            WorldState worldState = worldStateOpt.get();

            WorldDestinationSelectionResult selectionResult = worldOrchestrator.selectDestination(
                childSessionId,
                worldState.getChildProfileId(),
                null,
                null
            );

            fallbackDestination = selectionResult.getDestination();
        }

        return new WorldGameStartResult(
            childSessionId,
            activityId,
            null,
            status,
            fallbackDestination
        );
    }
}