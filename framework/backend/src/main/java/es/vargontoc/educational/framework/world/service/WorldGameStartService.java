package es.vargontoc.educational.framework.world.service;

import es.vargontoc.educational.framework.content.model.RecognitionType;
import es.vargontoc.educational.framework.content.model.Topic;
import es.vargontoc.educational.framework.content.ports.in.TopicUseCase;
import es.vargontoc.educational.framework.game.exception.EngineNotAvailableException;
import es.vargontoc.educational.framework.game.model.GameState;
import es.vargontoc.educational.framework.game.model.LaunchContext;
import es.vargontoc.educational.framework.game.ports.in.GameOrchestrator;
import es.vargontoc.educational.framework.game.ports.out.GameStateRegistry;
import es.vargontoc.educational.framework.tracking.model.ActivityProposalOutcome;
import es.vargontoc.educational.framework.world.model.WorldDestination;
import es.vargontoc.educational.framework.world.model.WorldDestinationSelectionResult;
import es.vargontoc.educational.framework.world.model.WorldDiscoveryProposal;
import es.vargontoc.educational.framework.world.model.WorldGameStartResult;
import es.vargontoc.educational.framework.world.model.WorldGameStartStatus;
import es.vargontoc.educational.framework.world.model.WorldState;
import es.vargontoc.educational.framework.world.ports.in.WorldGameStartUseCase;
import es.vargontoc.educational.framework.world.ports.in.WorldOrchestrator;
import es.vargontoc.educational.framework.world.ports.in.WorldProposalResolutionUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WorldGameStartService implements WorldGameStartUseCase {

    private static final Logger log = LoggerFactory.getLogger(WorldGameStartService.class);

    private final GameOrchestrator gameOrchestrator;
    private final GameStateRegistry gameStateRegistry;
    private final WorldProposalResolutionUseCase worldProposalResolutionUseCase;
    private final WorldOrchestrator worldOrchestrator;
    private final es.vargontoc.educational.framework.world.ports.out.WorldStateRegistry worldStateRegistry;
    private final TopicUseCase topicUseCase;

    public WorldGameStartService(GameOrchestrator gameOrchestrator,
                               GameStateRegistry gameStateRegistry,
                               WorldProposalResolutionUseCase worldProposalResolutionUseCase,
                               WorldOrchestrator worldOrchestrator,
                               es.vargontoc.educational.framework.world.ports.out.WorldStateRegistry worldStateRegistry,
                               TopicUseCase topicUseCase) {
        this.gameOrchestrator = gameOrchestrator;
        this.gameStateRegistry = gameStateRegistry;
        this.worldProposalResolutionUseCase = worldProposalResolutionUseCase;
        this.worldOrchestrator = worldOrchestrator;
        this.worldStateRegistry = worldStateRegistry;
        this.topicUseCase = topicUseCase;
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
                existingGame.map(gs -> gs.getGameId()).orElse(null),
                WorldGameStartStatus.EXISTING_GAME_ACTIVE,
                null,
                existingGame.map(s -> s.getEngine()).orElse(null)
            );
        }

        try {
            LaunchContext launchContext = buildLaunchContext(worldState, activityId);
            GameState startedGame = gameOrchestrator.startGame(childProfileId, activityId, launchContext);
            startedGame.setChildSessionId(childSessionId);
            gameStateRegistry.save(startedGame);

            worldProposalResolutionUseCase.resolveProposal(childSessionId, ActivityProposalOutcome.STARTED);

            return new WorldGameStartResult(
                childSessionId,
                activityId,
                startedGame.getGameId(),
                WorldGameStartStatus.STARTED,
                null,
                startedGame.getEngine()
            );

        } catch (EngineNotAvailableException e) {
            log.warn("Engine not available for activityId={}: {}", activityId, e.getMessage());
            return buildFallbackResult(childSessionId, activityId, WorldGameStartStatus.FALLBACK_DESTINATION);

        } catch (Exception e) {
            log.warn("Failed to start game for activityId={}: {}", activityId, e.getMessage());
            return buildFallbackResult(childSessionId, activityId, WorldGameStartStatus.FALLBACK_DESTINATION);
        }
    }

    LaunchContext buildLaunchContext(WorldState worldState, Long activityId) {
        WorldDiscoveryProposal matchingProposal = findMatchingProposal(worldState, activityId);

        if (matchingProposal == null || matchingProposal.getTopicId() == null) {
            return null;
        }

        Topic topic;
        try {
            topic = topicUseCase.getTopic(matchingProposal.getTopicId());
        } catch (Exception e) {
            log.warn("Failed to resolve topic={} for launch context: {}", matchingProposal.getTopicId(), e.getMessage());
            return null;
        }

        if (topic == null || topic.getRecognitionType() != RecognitionType.ANIMAL) {
            return null;
        }

        WorldDestination destination = worldState.getCurrentDestination();
        String habitatTag = null;
        String worldHostId = null;
        String narrativeContextId = null;

        if (destination != null) {
            habitatTag = destination.getBiome();
            if (destination.getHostId() != null) {
                worldHostId = String.valueOf(destination.getHostId());
            }
            narrativeContextId = destination.getNarrativeSituationCode();
        }

        String discoveryElementId = null;
        if (matchingProposal.getDiscoveryElementId() != null) {
            discoveryElementId = String.valueOf(matchingProposal.getDiscoveryElementId());
        }

        return new LaunchContext(worldHostId, habitatTag, discoveryElementId, narrativeContextId);
    }

    private WorldDiscoveryProposal findMatchingProposal(WorldState worldState, Long activityId) {
        List<WorldDiscoveryProposal> allProposals = new ArrayList<>();

        WorldDestination destination = worldState.getCurrentDestination();
        if (destination != null && destination.getDiscoveryProposals() != null) {
            allProposals.addAll(destination.getDiscoveryProposals());
        }

        if (worldState.getVisibleDiscoveryElements() != null) {
            allProposals.addAll(worldState.getVisibleDiscoveryElements());
        }

        return allProposals.stream()
                .filter(p -> activityId.equals(p.getActivityId()))
                .findFirst()
                .orElse(null);
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
            fallbackDestination,
            null
        );
    }
}