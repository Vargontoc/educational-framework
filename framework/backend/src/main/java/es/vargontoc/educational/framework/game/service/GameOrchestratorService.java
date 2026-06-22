package es.vargontoc.educational.framework.game.service;

import es.vargontoc.educational.framework.content.model.Activity;
import es.vargontoc.educational.framework.content.model.DifficultyLevel;
import es.vargontoc.educational.framework.content.model.GameCatalogReadiness;
import es.vargontoc.educational.framework.content.ports.in.GameCatalogUseCase;
import es.vargontoc.educational.framework.game.engine.FakeGameEngine;
import es.vargontoc.educational.framework.game.exception.EngineNotAvailableException;
import es.vargontoc.educational.framework.game.exception.GameNotFoundException;
import es.vargontoc.educational.framework.game.exception.InvalidStateTransitionException;
import es.vargontoc.educational.framework.game.model.ActionProcessingResult;
import es.vargontoc.educational.framework.game.model.ActionResult;
import es.vargontoc.educational.framework.game.model.ActionResultType;
import es.vargontoc.educational.framework.game.model.GameState;
import es.vargontoc.educational.framework.game.model.GameStatus;
import es.vargontoc.educational.framework.game.ports.in.GameEnginePort;
import es.vargontoc.educational.framework.game.ports.in.GameOrchestrator;
import es.vargontoc.educational.framework.game.ports.out.GameStateRegistry;
import es.vargontoc.educational.framework.tracking.model.AttemptRegistrationResult;
import es.vargontoc.educational.framework.tracking.model.AttemptResult;
import es.vargontoc.educational.framework.tracking.model.GameSessionFinalStatus;
import es.vargontoc.educational.framework.tracking.model.UnlockedAchievement;
import es.vargontoc.educational.framework.tracking.ports.in.EvaluateGameCompletionAchievementsUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.RegisterActivityAttemptUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.RegisterGameSessionSummaryUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class GameOrchestratorService implements GameOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(GameOrchestratorService.class);

    private final GameCatalogUseCase gameCatalogUseCase;
    private final GameStateRegistry gameStateRegistry;
    private final RegisterActivityAttemptUseCase registerActivityAttemptUseCase;
    private final EvaluateGameCompletionAchievementsUseCase evaluateGameCompletionAchievementsUseCase;
    private final RegisterGameSessionSummaryUseCase registerGameSessionSummaryUseCase;
    private final Environment environment;
    private final Map<String, GameEnginePort> engineInstances = new ConcurrentHashMap<>();
    private final Map<Long, ReentrantLock> gameLocks = new ConcurrentHashMap<>();

    public GameOrchestratorService(
            GameCatalogUseCase gameCatalogUseCase,
            GameStateRegistry gameStateRegistry,
            RegisterActivityAttemptUseCase registerActivityAttemptUseCase,
            EvaluateGameCompletionAchievementsUseCase evaluateGameCompletionAchievementsUseCase,
            RegisterGameSessionSummaryUseCase registerGameSessionSummaryUseCase,
            Environment environment) {
        this.gameCatalogUseCase = gameCatalogUseCase;
        this.gameStateRegistry = gameStateRegistry;
        this.registerActivityAttemptUseCase = registerActivityAttemptUseCase;
        this.evaluateGameCompletionAchievementsUseCase = evaluateGameCompletionAchievementsUseCase;
        this.registerGameSessionSummaryUseCase = registerGameSessionSummaryUseCase;
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
    public ActionProcessingResult processAction(Long gameId, String actionPayload, Long topicId, Integer responseTimeMs) {
        ReentrantLock lock = getLock(gameId);
        lock.lock();
        try {
            GameState state = gameStateRegistry.findByGameId(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));

            if (state.getStatus() != GameStatus.IN_PROGRESS) {
                throw new InvalidStateTransitionException(state.getStatus(), GameStatus.IN_PROGRESS);
            }

            if (state.isSystemEventPending()) {
                log.debug("Discarding action for gameId={} due to pending system event", gameId);
                return new ActionProcessingResult(
                    ActionResultType.CORRECT,
                    responseTimeMs,
                    state,
                    false,
                    null,
                    false,
                    List.of(),
                    "discarded_system_event_pending"
                );
            }

            GameEnginePort engine = resolveEngine(state);
            ActionResult engineResult = engine.processAction(state, actionPayload);

            state.setLastActivityAt(LocalDateTime.now());

            AttemptResult trackingResult = mapToTrackingResult(engineResult.getResultType());

            List<UnlockedAchievement> allUnlockedAchievements = new ArrayList<>();
            boolean difficultyChanged = false;
            Long newDifficultyLevelId = null;

            try {
                AttemptRegistrationResult attemptResult = registerActivityAttemptUseCase.register(
                    state.getChildSessionId(),
                    state.getActivityId(),
                    gameId,
                    topicId,
                    state.getDifficultyLevelId(),
                    trackingResult,
                    responseTimeMs,
                    engineResult.getAttemptContext()
                );

                if (attemptResult != null && attemptResult.unlockedAchievements() != null) {
                    allUnlockedAchievements.addAll(attemptResult.unlockedAchievements());
                }

                if (attemptResult != null && attemptResult.difficultyChanged()
                        && attemptResult.newDifficultyLevelId() != null
                        && !attemptResult.newDifficultyLevelId().equals(state.getDifficultyLevelId())) {
                    difficultyChanged = true;
                    newDifficultyLevelId = attemptResult.newDifficultyLevelId();
                    state.setDifficultyLevelId(newDifficultyLevelId);
                }
            } catch (Exception e) {
                log.warn("Tracking operation failed, continuing without tracking update: {}", e.getMessage());
            }

            boolean gameCompleted = engineResult.isCompleted();

            if (gameCompleted) {
                state.setStatus(GameStatus.COMPLETED);
                state.setCompletedAt(LocalDateTime.now());

                try {
                    List<UnlockedAchievement> completionAchievements = evaluateGameCompletionAchievementsUseCase.evaluate(
                        state.getChildSessionId(),
                        state.getActivityId(),
                        topicId
                    );
                    if (completionAchievements != null) {
                        allUnlockedAchievements.addAll(completionAchievements);
                    }

                    registerGameSessionSummaryUseCase.registerGameSessionSummary(
                        state.getChildSessionId(),
                        gameId,
                        state.getActivityId(),
                        state.getDifficultyLevelId(),
                        newDifficultyLevelId != null ? newDifficultyLevelId : state.getDifficultyLevelId(),
                        state.getCurrentScore() != null ? state.getCurrentScore().intValue() : 0,
                        state.getAttempts() != null ? state.getAttempts() : 0,
                        state.getCorrectAttempts() != null ? state.getCorrectAttempts() : 0,
                        state.getTimeoutAttempts() != null ? state.getTimeoutAttempts() : 0,
                        state.getStartedAt(),
                        LocalDateTime.now(),
                        GameSessionFinalStatus.COMPLETED
                    );
                } catch (Exception e) {
                    log.warn("Game completion tracking failed: {}", e.getMessage());
                }

                gameStateRegistry.remove(gameId);
            } else {
                gameStateRegistry.save(state);
            }

            return new ActionProcessingResult(
                engineResult.getResultType(),
                responseTimeMs,
                state,
                difficultyChanged,
                newDifficultyLevelId,
                gameCompleted,
                allUnlockedAchievements,
                engineResult.getAttemptContext()
            );
        } finally {
            lock.unlock();
        }
    }

    @Override
    public GameState abandonGame(Long gameId) {
        ReentrantLock lock = getLock(gameId);
        lock.lock();
        try {
            GameState state = gameStateRegistry.findByGameId(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));

            if (!isActive(state.getStatus())) {
                throw new InvalidStateTransitionException(state.getStatus(), GameStatus.ABANDONED);
            }

            state.setStatus(GameStatus.ABANDONED);
            state.setLastActivityAt(LocalDateTime.now());

            try {
                registerGameSessionSummaryUseCase.registerGameSessionSummary(
                    state.getChildSessionId(),
                    gameId,
                    state.getActivityId(),
                    state.getDifficultyLevelId(),
                    state.getDifficultyLevelId(),
                    state.getCurrentScore() != null ? state.getCurrentScore().intValue() : 0,
                    state.getAttempts() != null ? state.getAttempts() : 0,
                    state.getCorrectAttempts() != null ? state.getCorrectAttempts() : 0,
                    state.getTimeoutAttempts() != null ? state.getTimeoutAttempts() : 0,
                    state.getStartedAt(),
                    LocalDateTime.now(),
                    GameSessionFinalStatus.ABANDONED
                );
            } catch (Exception e) {
                log.warn("Failed to register game session summary for client-abandoned game: {}", e.getMessage());
            }

            gameStateRegistry.remove(gameId);

            return state;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void abandonGameForSession(Long childSessionId) {
        var gameState = gameStateRegistry.findByChildSessionId(childSessionId).orElse(null);
        if (gameState == null) {
            log.debug("No active game found for childSessionId={}", childSessionId);
            return;
        }

        Long gameId = gameState.getGameId();
        ReentrantLock lock = getLock(gameId);
        lock.lock();
        try {
            var state = gameStateRegistry.findByGameId(gameId).orElse(null);
            if (state == null || !isActive(state.getStatus())) {
                return;
            }

            state.setSystemEventPending(true);
            state.setStatus(GameStatus.ABANDONED);
            state.setLastActivityAt(LocalDateTime.now());

            try {
                registerGameSessionSummaryUseCase.registerGameSessionSummary(
                    state.getChildSessionId(),
                    gameId,
                    state.getActivityId(),
                    state.getDifficultyLevelId(),
                    state.getDifficultyLevelId(),
                    state.getCurrentScore() != null ? state.getCurrentScore().intValue() : 0,
                    state.getAttempts() != null ? state.getAttempts() : 0,
                    state.getCorrectAttempts() != null ? state.getCorrectAttempts() : 0,
                    state.getTimeoutAttempts() != null ? state.getTimeoutAttempts() : 0,
                    state.getStartedAt(),
                    LocalDateTime.now(),
                    GameSessionFinalStatus.ABANDONED
                );
            } catch (Exception e) {
                log.warn("Failed to register game session summary for abandoned game: {}", e.getMessage());
            }

            gameStateRegistry.remove(gameId);
            log.info("Game {} abandoned due to system event for childSessionId={}", gameId, childSessionId);

        } finally {
            lock.unlock();
        }
    }

    private ReentrantLock getLock(Long gameId) {
        return gameLocks.computeIfAbsent(gameId, k -> new ReentrantLock());
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

    private AttemptResult mapToTrackingResult(ActionResultType actionResultType) {
        return switch (actionResultType) {
            case CORRECT -> AttemptResult.CORRECT;
            case INCORRECT -> AttemptResult.INCORRECT;
            case TIMEOUT -> AttemptResult.TIMEOUT;
        };
    }
}
