package es.vargontoc.educational.framework.world.service;

import es.vargontoc.educational.framework.content.model.RecognitionType;
import es.vargontoc.educational.framework.content.model.Topic;
import es.vargontoc.educational.framework.content.ports.in.TopicUseCase;
import es.vargontoc.educational.framework.game.exception.EngineNotAvailableException;
import es.vargontoc.educational.framework.game.model.GameState;
import es.vargontoc.educational.framework.game.model.GameStatus;
import es.vargontoc.educational.framework.game.model.LaunchContext;
import es.vargontoc.educational.framework.game.ports.in.GameOrchestrator;
import es.vargontoc.educational.framework.game.ports.out.GameStateRegistry;
import es.vargontoc.educational.framework.tracking.model.ActivityProposalOutcome;
import es.vargontoc.educational.framework.world.model.WorldDestination;
import es.vargontoc.educational.framework.world.model.WorldDestinationSelectionResult;
import es.vargontoc.educational.framework.world.model.WorldDiscoveryProposal;
import es.vargontoc.educational.framework.world.model.WorldGameStartResult;
import es.vargontoc.educational.framework.world.model.WorldGameStartStatus;
import es.vargontoc.educational.framework.world.model.WorldRuntimeStatus;
import es.vargontoc.educational.framework.world.model.WorldState;
import es.vargontoc.educational.framework.world.ports.in.WorldOrchestrator;
import es.vargontoc.educational.framework.world.ports.in.WorldProposalResolutionUseCase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    @Mock
    private TopicUseCase topicUseCase;

    private WorldGameStartService worldGameStartService;

    @BeforeEach
    void setUp() {
        worldGameStartService = new WorldGameStartService(
            gameOrchestrator,
            gameStateRegistry,
            worldProposalResolutionUseCase,
            worldOrchestrator,
            worldStateRegistry,
            topicUseCase
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

    private WorldDiscoveryProposal createProposal(Long activityId, Long topicId, Long discoveryElementId) {
        WorldDiscoveryProposal proposal = new WorldDiscoveryProposal();
        proposal.setActivityId(activityId);
        proposal.setTopicId(topicId);
        proposal.setDiscoveryElementId(discoveryElementId);
        return proposal;
    }

    private Topic createTopic(Long id, RecognitionType recognitionType) {
        Topic topic = new Topic();
        topic.setId(id);
        topic.setRecognitionType(recognitionType);
        return topic;
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
        when(gameOrchestrator.startGame(eq(childProfileId), eq(activityId), any())).thenReturn(startedGame);

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
        verify(gameOrchestrator, never()).startGame(any(), any(), any());
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
        when(gameOrchestrator.startGame(eq(childProfileId), eq(activityId), any()))
            .thenThrow(new RuntimeException("Activity inactive"));
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
        when(gameOrchestrator.startGame(eq(childProfileId), eq(activityId), any()))
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
    void animalRecognitionLaunch_includesHabitatTag() {
        Long childSessionId = 100L;
        Long childProfileId = 1L;
        Long activityId = 10L;
        Long topicId = 50L;
        Long discoveryElementId = 77L;
        Long gameId = 2000L;

        WorldState worldState = createWorldState(childSessionId, childProfileId);
        WorldDestination destination = new WorldDestination();
        destination.setBiome("JUNGLE");
        destination.setHostId(42L);
        destination.setNarrativeSituationCode("NARR-01");
        WorldDiscoveryProposal proposal = createProposal(activityId, topicId, discoveryElementId);
        destination.setDiscoveryProposals(List.of(proposal));
        worldState.setCurrentDestination(destination);

        Topic animalTopic = createTopic(topicId, RecognitionType.ANIMAL);
        GameState startedGame = createGameState(gameId, childSessionId, GameStatus.WAITING);

        when(worldStateRegistry.findByChildSessionId(childSessionId)).thenReturn(Optional.of(worldState));
        when(gameStateRegistry.hasActiveGameForChildSession(childSessionId)).thenReturn(false);
        when(topicUseCase.getTopic(topicId)).thenReturn(animalTopic);
        when(gameOrchestrator.startGame(eq(childProfileId), eq(activityId), any())).thenReturn(startedGame);

        worldGameStartService.startGameFromProposal(childSessionId, activityId);

        ArgumentCaptor<LaunchContext> captor = ArgumentCaptor.forClass(LaunchContext.class);
        verify(gameOrchestrator).startGame(eq(childProfileId), eq(activityId), captor.capture());

        LaunchContext ctx = captor.getValue();
        assertNotNull(ctx);
        assertEquals("JUNGLE", ctx.getHabitatTag());
        assertEquals("42", ctx.getWorldHostId());
        assertEquals("77", ctx.getDiscoveryElementId());
        assertEquals("NARR-01", ctx.getNarrativeContextId());
    }

    @Test
    void nonAnimalRecognitionLaunch_passesNullContext() {
        Long childSessionId = 100L;
        Long childProfileId = 1L;
        Long activityId = 10L;
        Long topicId = 51L;
        Long gameId = 2001L;

        WorldState worldState = createWorldState(childSessionId, childProfileId);
        WorldDestination destination = new WorldDestination();
        destination.setBiome("FARM");
        destination.setHostId(10L);
        WorldDiscoveryProposal proposal = createProposal(activityId, topicId, 88L);
        destination.setDiscoveryProposals(List.of(proposal));
        worldState.setCurrentDestination(destination);

        Topic letterTopic = createTopic(topicId, RecognitionType.LETTER);
        GameState startedGame = createGameState(gameId, childSessionId, GameStatus.WAITING);

        when(worldStateRegistry.findByChildSessionId(childSessionId)).thenReturn(Optional.of(worldState));
        when(gameStateRegistry.hasActiveGameForChildSession(childSessionId)).thenReturn(false);
        when(topicUseCase.getTopic(topicId)).thenReturn(letterTopic);
        when(gameOrchestrator.startGame(eq(childProfileId), eq(activityId), any())).thenReturn(startedGame);

        worldGameStartService.startGameFromProposal(childSessionId, activityId);

        ArgumentCaptor<LaunchContext> captor = ArgumentCaptor.forClass(LaunchContext.class);
        verify(gameOrchestrator).startGame(eq(childProfileId), eq(activityId), captor.capture());

        assertNull(captor.getValue());
    }

    @Test
    void missingHabitat_fallsBackSafely() {
        Long childSessionId = 100L;
        Long childProfileId = 1L;
        Long activityId = 10L;
        Long topicId = 52L;
        Long gameId = 2002L;

        WorldState worldState = createWorldState(childSessionId, childProfileId);
        WorldDestination destination = new WorldDestination();
        WorldDiscoveryProposal proposal = createProposal(activityId, topicId, 99L);
        destination.setDiscoveryProposals(List.of(proposal));
        worldState.setCurrentDestination(destination);

        Topic animalTopic = createTopic(topicId, RecognitionType.ANIMAL);
        GameState startedGame = createGameState(gameId, childSessionId, GameStatus.WAITING);

        when(worldStateRegistry.findByChildSessionId(childSessionId)).thenReturn(Optional.of(worldState));
        when(gameStateRegistry.hasActiveGameForChildSession(childSessionId)).thenReturn(false);
        when(topicUseCase.getTopic(topicId)).thenReturn(animalTopic);
        when(gameOrchestrator.startGame(eq(childProfileId), eq(activityId), any())).thenReturn(startedGame);

        WorldGameStartResult result = worldGameStartService.startGameFromProposal(childSessionId, activityId);

        assertNotNull(result);
        assertEquals(WorldGameStartStatus.STARTED, result.getStatus());

        ArgumentCaptor<LaunchContext> captor = ArgumentCaptor.forClass(LaunchContext.class);
        verify(gameOrchestrator).startGame(eq(childProfileId), eq(activityId), captor.capture());

        LaunchContext ctx = captor.getValue();
        assertNotNull(ctx);
        assertNull(ctx.getHabitatTag());
        assertNull(ctx.getWorldHostId());
        assertEquals("99", ctx.getDiscoveryElementId());
    }

    @Test
    void recognitionEngine_hasNoDependencyToWorldClasses() {
        Class<?> engineClass = es.vargontoc.educational.framework.game.engine.RecognitionEngine.class;
        String worldPackage = "es.vargontoc.educational.framework.world";

        for (Field field : engineClass.getDeclaredFields()) {
            Class<?> fieldType = field.getType();
            String fieldPackage = fieldType.getPackage() != null ? fieldType.getPackage().getName() : "";
            assertTrue(
                !fieldPackage.startsWith(worldPackage),
                "RecognitionEngine field '" + field.getName() + "' depends on world package: " + fieldPackage
            );
        }

        java.lang.reflect.Method[] methods = engineClass.getDeclaredMethods();
        for (java.lang.reflect.Method method : methods) {
            for (Class<?> paramType : method.getParameterTypes()) {
                String paramPackage = paramType.getPackage() != null ? paramType.getPackage().getName() : "";
                assertTrue(
                    !paramPackage.startsWith(worldPackage),
                    "RecognitionEngine method '" + method.getName() + "' parameter depends on world package: " + paramPackage
                );
            }
            Class<?> returnType = method.getReturnType();
            String returnPackage = returnType.getPackage() != null ? returnType.getPackage().getName() : "";
            assertTrue(
                !returnPackage.startsWith(worldPackage),
                "RecognitionEngine method '" + method.getName() + "' return type depends on world package: " + returnPackage
            );
        }

        Class<?>[] interfaces = engineClass.getInterfaces();
        for (Class<?> iface : interfaces) {
            String ifacePackage = iface.getPackage() != null ? iface.getPackage().getName() : "";
            assertTrue(
                !ifacePackage.startsWith(worldPackage),
                "RecognitionEngine implements interface from world package: " + ifacePackage
            );
        }
    }
}
