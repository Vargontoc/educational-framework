package es.vargontoc.educational.framework.game.service;

import es.vargontoc.educational.framework.content.model.Activity;
import es.vargontoc.educational.framework.content.model.Biome;
import es.vargontoc.educational.framework.content.model.DifficultyLevel;
import es.vargontoc.educational.framework.content.model.GameCatalogReadiness;
import es.vargontoc.educational.framework.content.model.RecognitionType;
import es.vargontoc.educational.framework.content.model.Topic;
import es.vargontoc.educational.framework.content.ports.in.GameCatalogUseCase;
import es.vargontoc.educational.framework.content.ports.in.TopicUseCase;
import es.vargontoc.educational.framework.game.engine.RecognitionEngine;
import es.vargontoc.educational.framework.game.exception.EngineNotAvailableException;
import es.vargontoc.educational.framework.game.exception.GameNotFoundException;
import es.vargontoc.educational.framework.game.exception.InvalidStateTransitionException;
import es.vargontoc.educational.framework.game.model.ActionProcessingResult;
import es.vargontoc.educational.framework.game.model.ActionResult;
import es.vargontoc.educational.framework.game.model.ActionResultType;
import es.vargontoc.educational.framework.game.model.GameState;
import es.vargontoc.educational.framework.game.model.GameStatus;
import es.vargontoc.educational.framework.game.model.LaunchContext;
import es.vargontoc.educational.framework.game.model.enums.EngineType;
import es.vargontoc.educational.framework.game.model.enums.RecognitionCategory;
import es.vargontoc.educational.framework.game.model.event.GameSessionCompletedEvent;
import es.vargontoc.educational.framework.game.ports.in.GameEnginePort;
import es.vargontoc.educational.framework.game.ports.in.GameOrchestrator;
import es.vargontoc.educational.framework.game.ports.out.GameStateRegistry;
import es.vargontoc.educational.framework.tracking.model.AttemptRegistrationResult;
import es.vargontoc.educational.framework.tracking.model.AttemptResult;
import es.vargontoc.educational.framework.tracking.model.GameSessionFinalStatus;
import es.vargontoc.educational.framework.tracking.model.UnlockedAchievement;
import es.vargontoc.educational.framework.tracking.ports.in.EvaluateGameCompletionAchievementsUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.FilterAllowedRecognitionCategoriesUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.RegisterActivityAttemptUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.RegisterGameSessionSummaryUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;
    private final TopicUseCase topicUseCase;
    private final FilterAllowedRecognitionCategoriesUseCase filterAllowedRecognitionCategoriesUseCase;
    private final Map<String, GameEnginePort> engineInstances = new ConcurrentHashMap<>();
    private final Map<Long, ReentrantLock> gameLocks = new ConcurrentHashMap<>();

    public GameOrchestratorService(
            GameCatalogUseCase gameCatalogUseCase,
            GameStateRegistry gameStateRegistry,
            RegisterActivityAttemptUseCase registerActivityAttemptUseCase,
            EvaluateGameCompletionAchievementsUseCase evaluateGameCompletionAchievementsUseCase,
            RegisterGameSessionSummaryUseCase registerGameSessionSummaryUseCase,
            ApplicationEventPublisher eventPublisher,
            TopicUseCase topicUseCase,
            FilterAllowedRecognitionCategoriesUseCase filterAllowedRecognitionCategoriesUseCase) {
        this.gameCatalogUseCase = gameCatalogUseCase;
        this.gameStateRegistry = gameStateRegistry;
        this.registerActivityAttemptUseCase = registerActivityAttemptUseCase;
        this.evaluateGameCompletionAchievementsUseCase = evaluateGameCompletionAchievementsUseCase;
        this.registerGameSessionSummaryUseCase = registerGameSessionSummaryUseCase;
        this.eventPublisher = eventPublisher;
        this.topicUseCase = topicUseCase;
        this.filterAllowedRecognitionCategoriesUseCase = filterAllowedRecognitionCategoriesUseCase;

        this.engineInstances.putIfAbsent(EngineType.RECOGNITION.name(), new RecognitionEngine());
    }

    @Override
    public GameState startGame(Long childProfileId, Long activityId) {
        return startGame(childProfileId, activityId, null);
    }

    @Override
    public GameState startGame(Long childProfileId, Long activityId, LaunchContext launchContext) {
        GameCatalogReadiness readiness = gameCatalogUseCase.getGameReadiness(childProfileId, activityId);

        Activity activity = readiness.activity();
        DifficultyLevel difficultyLevel = readiness.difficultyLevel();

        GameState state = new GameState();
        state.setGameId(generateGameId());
        state.setChildSessionId(childProfileId);
        state.setActivityId(activityId);
        state.setDifficultyLevelId(difficultyLevel.getId());
        state.setEngine(resolveEngineType(activity));
        state.setStatus(GameStatus.WAITING);
        state.setStartedAt(LocalDateTime.now());
        state.setLastActivityAt(LocalDateTime.now());

        if (state.getEngine() == EngineType.RECOGNITION) {
            List<String> candidates = resolveCandidates(childProfileId, activity, launchContext);
            state.setCandidates(candidates);
        }

        gameStateRegistry.save(state);

        return state;
    }

    private EngineType resolveEngineType(Activity activity) {
        String gameEngineType = activity.getGameEngineType();
        try {
            return EngineType.valueOf(gameEngineType);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new EngineNotAvailableException(gameEngineType);
        }
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

                publishGameCompletedEvent(gameId, state.getChildSessionId(), state.getActivityId(), GameSessionFinalStatus.COMPLETED);
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

            publishGameCompletedEvent(gameId, state.getChildSessionId(), state.getActivityId(), GameSessionFinalStatus.ABANDONED);

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

            publishGameCompletedEvent(gameId, state.getChildSessionId(), state.getActivityId(), GameSessionFinalStatus.ABANDONED);
            log.info("Game {} abandoned due to system event for childSessionId={}", gameId, childSessionId);

        } finally {
            lock.unlock();
        }
    }

    private ReentrantLock getLock(Long gameId) {
        return gameLocks.computeIfAbsent(gameId, k -> new ReentrantLock());
    }

    private GameEnginePort resolveEngine(GameState state) {
        try{
            GameEnginePort engine = engineInstances.get(state.getEngine().name());
            if (engine == null) {
                throw new EngineNotAvailableException(state.getEngine().name());
            }
            return engine;
        }catch(EngineNotAvailableException e){
            throw e;
        }catch(Exception e){
            throw new EngineNotAvailableException(state.getEngine().name());
        }
    }

    private String getEngineParams(GameState state) {
        List<String> candidates = state.getCandidates() != null ? state.getCandidates() : List.of();
        StringBuilder sb = new StringBuilder("{\"candidates\":[");
        for (int i = 0; i < candidates.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("\"").append(candidates.get(i)).append("\"");
        }
        sb.append("]}");
        return sb.toString();
    }

    private List<String> resolveCandidates(Long childProfileId, Activity activity, LaunchContext launchContext) {
        RecognitionCategory category = resolveRecognitionCategory(activity);
        if (category == null) {
            return List.of();
        }

        es.vargontoc.educational.framework.tracking.model.RecognitionCategory trackingCategory =
                es.vargontoc.educational.framework.tracking.model.RecognitionCategory.valueOf(category.name());

        List<es.vargontoc.educational.framework.tracking.model.RecognitionCategory> allowed =
                filterAllowedRecognitionCategoriesUseCase.filterAllowedCategories(
                        childProfileId, List.of(trackingCategory));

        if (allowed.isEmpty()) {
            return List.of();
        }

        RecognitionType recognitionType = RecognitionType.valueOf(category.name());

        List<Topic> topics;
        if (category == RecognitionCategory.ANIMAL
                && launchContext != null
                && launchContext.getHabitatTag() != null
                && !launchContext.getHabitatTag().isBlank()) {
            Biome biome = Biome.valueOf(launchContext.getHabitatTag());
            topics = topicUseCase.listTopicsByRecognitionTypeAndHabitat(recognitionType, biome);
        } else {
            topics = topicUseCase.listTopicsByRecognitionType(recognitionType);
        }

        return topics.stream()
                .map(t -> String.valueOf(t.getId()))
                .toList();
    }

    private RecognitionCategory resolveRecognitionCategory(Activity activity) {
        List<Long> topicIds = activity.getTopicIds();
        if (topicIds == null || topicIds.isEmpty()) {
            return null;
        }
        Topic firstTopic = topicUseCase.getTopic(topicIds.get(0));
        if (firstTopic == null || firstTopic.getRecognitionType() == null) {
            return null;
        }
        return RecognitionCategory.valueOf(firstTopic.getRecognitionType().name());
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

    private void publishGameCompletedEvent(Long gameId, Long childSessionId, Long activityId, GameSessionFinalStatus status) {
        try {
            GameSessionCompletedEvent event = new GameSessionCompletedEvent(
                gameId,
                childSessionId,
                activityId,
                status,
                LocalDateTime.now()
            );
            eventPublisher.publishEvent(event);
            log.debug("Published GameSessionCompletedEvent: gameId={}, status={}", gameId, status);
        } catch (Exception e) {
            log.warn("Failed to publish GameSessionCompletedEvent: {}", e.getMessage());
        }
    }
}
