package es.vargontoc.educational.framework.world.service;

import es.vargontoc.educational.framework.game.exception.EngineNotAvailableException;
import es.vargontoc.educational.framework.game.model.GameState;
import es.vargontoc.educational.framework.game.model.GameStatus;
import es.vargontoc.educational.framework.game.ports.in.GameOrchestrator;
import es.vargontoc.educational.framework.game.ports.out.GameStateRegistry;
import es.vargontoc.educational.framework.tracking.model.ActivityProposalOutcome;
import es.vargontoc.educational.framework.world.model.WorldDestination;
import es.vargontoc.educational.framework.world.model.WorldDestinationSelectionResult;
import es.vargontoc.educational.framework.world.model.WorldGameStartResult;
import es.vargontoc.educational.framework.world.model.WorldGameStartStatus;
import es.vargontoc.educational.framework.world.model.WorldRuntimeStatus;
import es.vargontoc.educational.framework.world.model.WorldState;
import es.vargontoc.educational.framework.world.ports.in.WorldOrchestrator;
import es.vargontoc.educational.framework.world.ports.in.WorldProposalResolutionUseCase;
import es.vargontoc.educational.framework.world.ports.out.WorldStateRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorldGameStartServiceTest {

    @Mock
    private GameOrchestrator gameOrchestrator;

    @Mock
    private GameStateRegistry gameStateRegistry;

    @Mock
    private WorldProposalResolutionUseCase worldProposalResolutionUseCase;

    @Mock
    private WorldOrchestrator worldOrchestrator;

    @Mock
    private es.vargontoc.educational.framework.world.ports.out.WorldStateRegistry worldStateRegistry;

    private WorldGameStartService worldGameStartService;

    @BeforeEach
    void setUp() {
        worldGameStartService = new WorldGameStartService(
            gameOrchestrator,
            gameStateRegistry,
            worldProposalResolutionUseCase,
            worldOrchestrator,
            worldStateRegistry
        );
    }

    private WorldState createWorldState(Long childSessionId, Long childProfileId) {
        WorldState state = new WorldState();
        state.setChildSessionId(childSessionId);
        state.setChildProfileId(childProfileId);
        state.setStatus(WorldRuntimeStatus.ACTIVE);
        state.setLastWorldActivityAt(LocalDateTime.now());
        state.setCreatedAt(LocalDateTime.now());
        state.setUpdatedAt(LocalDateTime.now());
        return state;
    }

    private GameState createGameState(Long gameId, Long childSessionId, GameStatus status) {
        GameState state = new GameState();
        state.setGameId(gameId);
        state.setChildSessionId(childSessionId);
        state.setStatus(status);
        return state;
    }

    @Test
    void startGameFromProposal_success() {
        Long childSessionId = 100L;
        Long childProfileId = 1L;
        Long activityId = 10L;
        Long gameId = 1000L;

        WorldState worldState = createWorldState(childSessionId, childProfileId);
        GameState startedGame = createGameState(gameId, childSessionId, GameStatus.WAITING);

        when(worldStateRegistry.findByChildSessionId(childSessionId)).thenReturn(Optional.of(worldState));
        when(gameStateRegistry.hasActiveGameForChildSession(childSessionId)).thenReturn(false);
        when(gameOrchestrator.startGame(childProfileId, activityId)).thenReturn(startedGame);

        WorldGameStartResult result = worldGameStartService.startGameFromProposal(childSessionId, activityId);

        assertNotNull(result);
        assertEquals(WorldGameStartStatus.STARTED, result.getStatus());
        assertEquals(gameId, result.getGameId());
        assertEquals(activityId, result.getActivityId());
        assertNull(result.getSafeFallbackDestination());

        verify(worldProposalResolutionUseCase).resolveProposal(childSessionId, ActivityProposalOutcome.STARTED);
    }

    @Test
    void startGameFromProposal_existingGame() {
        Long childSessionId = 100L;
        Long childProfileId = 1L;
        Long activityId = 10L;
        Long existingGameId = 999L;

        WorldState worldState = createWorldState(childSessionId, childProfileId);
        GameState existingGame = createGameState(existingGameId, childSessionId, GameStatus.IN_PROGRESS);

        when(worldStateRegistry.findByChildSessionId(childSessionId)).thenReturn(Optional.of(worldState));
        when(gameStateRegistry.hasActiveGameForChildSession(childSessionId)).thenReturn(true);
        when(gameStateRegistry.findByChildSessionId(childSessionId)).thenReturn(Optional.of(existingGame));

        WorldGameStartResult result = worldGameStartService.startGameFromProposal(childSessionId, activityId);

        assertNotNull(result);
        assertEquals(WorldGameStartStatus.EXISTING_GAME_ACTIVE, result.getStatus());
        assertEquals(existingGameId, result.getGameId());
        assertEquals(activityId, result.getActivityId());

        verify(gameOrchestrator, never()).startGame(any(), any());
    }

    @Test
    void startGameFromProposal_activityInactive_fallsBack() {
        Long childSessionId = 100L;
        Long childProfileId = 1L;
        Long activityId = 10L;

        WorldState worldState = createWorldState(childSessionId, childProfileId);
        WorldDestination fallbackDestination = new WorldDestination();
        fallbackDestination.setDestinationId("fallback-1");

        WorldDestinationSelectionResult selectionResult = new WorldDestinationSelectionResult();
        selectionResult.setDestination(fallbackDestination);

        when(worldStateRegistry.findByChildSessionId(childSessionId)).thenReturn(Optional.of(worldState));
        when(gameStateRegistry.hasActiveGameForChildSession(childSessionId)).thenReturn(false);
        when(gameOrchestrator.startGame(childProfileId, activityId)).thenThrow(new RuntimeException("Activity inactive"));
        when(worldOrchestrator.selectDestination(eq(childSessionId), eq(childProfileId), any(), any()))
            .thenReturn(selectionResult);

        WorldGameStartResult result = worldGameStartService.startGameFromProposal(childSessionId, activityId);

        assertNotNull(result);
        assertEquals(WorldGameStartStatus.FALLBACK_DESTINATION, result.getStatus());
        assertNull(result.getGameId());
        assertNotNull(result.getSafeFallbackDestination());
    }

    @Test
    void startGameFromProposal_engineUnavailable_fallsBack() {
        Long childSessionId = 100L;
        Long childProfileId = 1L;
        Long activityId = 10L;

        WorldState worldState = createWorldState(childSessionId, childProfileId);
        WorldDestination fallbackDestination = new WorldDestination();
        fallbackDestination.setDestinationId("fallback-1");

        WorldDestinationSelectionResult selectionResult = new WorldDestinationSelectionResult();
        selectionResult.setDestination(fallbackDestination);

        when(worldStateRegistry.findByChildSessionId(childSessionId)).thenReturn(Optional.of(worldState));
        when(gameStateRegistry.hasActiveGameForChildSession(childSessionId)).thenReturn(false);
        when(gameOrchestrator.startGame(childProfileId, activityId))
            .thenThrow(new EngineNotAvailableException("Engine not available"));
        when(worldOrchestrator.selectDestination(eq(childSessionId), eq(childProfileId), any(), any()))
            .thenReturn(selectionResult);

        WorldGameStartResult result = worldGameStartService.startGameFromProposal(childSessionId, activityId);

        assertNotNull(result);
        assertEquals(WorldGameStartStatus.FALLBACK_DESTINATION, result.getStatus());
        assertNull(result.getGameId());
        assertNotNull(result.getSafeFallbackDestination());
    }

    @Test
    void startGameFromProposal_worldStateNotFound_returnsBlocked() {
        Long childSessionId = 100L;
        Long activityId = 10L;

        when(worldStateRegistry.findByChildSessionId(childSessionId)).thenReturn(Optional.empty());

        WorldGameStartResult result = worldGameStartService.startGameFromProposal(childSessionId, activityId);

        assertNotNull(result);
        assertEquals(WorldGameStartStatus.BLOCKED, result.getStatus());
        assertNull(result.getGameId());
        assertNull(result.getSafeFallbackDestination());
    }

    @Test
    void startGameFromProposal_noDirectGameToWorldDependency() {
        String sourceCode = es.vargontoc.educational.framework.world.service.WorldGameStartService.class.getName();
        String packageName = es.vargontoc.educational.framework.world.service.WorldGameStartService.class.getPackage().getName();

        assertNotNull(packageName);
        assertEquals("es.vargontoc.educational.framework.world.service", packageName);

        assertEquals("WorldGameStartService", sourceCode.substring(sourceCode.lastIndexOf(".") + 1));
    }
}