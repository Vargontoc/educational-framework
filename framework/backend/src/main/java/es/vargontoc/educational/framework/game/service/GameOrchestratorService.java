package es.vargontoc.educational.framework.game.service;

import es.vargontoc.educational.framework.content.model.Activity;
import es.vargontoc.educational.framework.content.model.DifficultyLevel;
import es.vargontoc.educational.framework.content.model.GameCatalogReadiness;
import es.vargontoc.educational.framework.content.ports.in.GameCatalogUseCase;
import es.vargontoc.educational.framework.game.engine.FakeGameEngine;
import es.vargontoc.educational.framework.game.exception.EngineNotAvailableException;
import es.vargontoc.educational.framework.game.exception.GameNotFoundException;
import es.vargontoc.educational.framework.game.exception.InvalidStateTransitionException;
import es.vargontoc.educational.framework.game.model.ActionResult;
import es.vargontoc.educational.framework.game.model.GameState;
import es.vargontoc.educational.framework.game.model.GameStatus;
import es.vargontoc.educational.framework.game.ports.in.GameEnginePort;
import es.vargontoc.educational.framework.game.ports.in.GameOrchestrator;
import es.vargontoc.educational.framework.game.ports.out.GameStateRegistry;
import org.springframework.core.env.Environment;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameOrchestratorService implements GameOrchestrator {

    private final GameCatalogUseCase gameCatalogUseCase;
    private final GameStateRegistry gameStateRegistry;
    private final Environment environment;
    private final Map<String, GameEnginePort> engineInstances = new ConcurrentHashMap<>();

    public GameOrchestratorService(
            GameCatalogUseCase gameCatalogUseCase,
            GameStateRegistry gameStateRegistry,
            Environment environment) {
        this.gameCatalogUseCase = gameCatalogUseCase;
        this.gameStateRegistry = gameStateRegistry;
        this.environment = environment;
    }

    @Override
    public GameState startGame(Long childProfileId, Long activityId) {
        GameCatalogReadiness readiness = gameCatalogUseCase.getGameReadiness(childProfileId, activityId);

        Activity activity = readiness.activity();
        DifficultyLevel difficultyLevel = readiness.difficultyLevel();

        GameState state = new GameState();
        state.setGameId(generateGameId());
        state.setChildSessionId(childProfileId);
        state.setActivityId(activityId);
        state.setDifficultyLevelId(difficultyLevel.getId());
        state.setStatus(GameStatus.WAITING);
        state.setStartedAt(LocalDateTime.now());
        state.setLastActivityAt(LocalDateTime.now());

        gameStateRegistry.save(state);

        return state;
    }

    @Override
    public GameState readyGame(Long gameId) {
        GameState state = gameStateRegistry.findByGameId(gameId)
            .orElseThrow(() -> new GameNotFoundException(gameId));

        if (state.getStatus() != GameStatus.WAITING) {
            throw new InvalidStateTransitionException(state.getStatus(), GameStatus.STARTING);
        }

        state.setStatus(GameStatus.STARTING);
        state.setLastActivityAt(LocalDateTime.now());
        gameStateRegistry.save(state);

        GameEnginePort engine = resolveEngine(state);
        engine.initGame(state, getEngineParams(state));

        state.setStatus(GameStatus.IN_PROGRESS);
        state.setLastActivityAt(LocalDateTime.now());
        gameStateRegistry.save(state);

        return state;
    }

    @Override
    public GameState processAction(Long gameId, String actionPayload) {
        GameState state = gameStateRegistry.findByGameId(gameId)
            .orElseThrow(() -> new GameNotFoundException(gameId));

        if (state.getStatus() != GameStatus.IN_PROGRESS) {
            throw new InvalidStateTransitionException(state.getStatus(), GameStatus.IN_PROGRESS);
        }

        GameEnginePort engine = resolveEngine(state);
        ActionResult result = engine.processAction(state, actionPayload);

        state.setLastActivityAt(LocalDateTime.now());

        if (result.isCompleted()) {
            state.setStatus(GameStatus.COMPLETED);
            state.setCompletedAt(LocalDateTime.now());
            gameStateRegistry.remove(gameId);
        } else {
            gameStateRegistry.save(state);
        }

        return state;
    }

    @Override
    public GameState abandonGame(Long gameId) {
        GameState state = gameStateRegistry.findByGameId(gameId)
            .orElseThrow(() -> new GameNotFoundException(gameId));

        if (!isActive(state.getStatus())) {
            throw new InvalidStateTransitionException(state.getStatus(), GameStatus.ABANDONED);
        }

        state.setStatus(GameStatus.ABANDONED);
        state.setLastActivityAt(LocalDateTime.now());
        gameStateRegistry.remove(gameId);

        return state;
    }

    private GameEnginePort resolveEngine(GameState state) {
        String engineType = "fake";

        if (isDevProfile()) {
            engineInstances.putIfAbsent(engineType, new FakeGameEngine());
            return engineInstances.get(engineType);
        }

        throw new EngineNotAvailableException(engineType);
    }

    private String getEngineParams(GameState state) {
        return "{\"difficultyLevelId\":" + state.getDifficultyLevelId() + "}";
    }

    private boolean isDevProfile() {
        for (String profile : environment.getActiveProfiles()) {
            if ("dev".equalsIgnoreCase(profile)) {
                return true;
            }
        }
        return false;
    }

    private boolean isActive(GameStatus status) {
        return status == GameStatus.WAITING
            || status == GameStatus.STARTING
            || status == GameStatus.IN_PROGRESS;
    }

    private Long generateGameId() {
        return System.currentTimeMillis();
    }
}
