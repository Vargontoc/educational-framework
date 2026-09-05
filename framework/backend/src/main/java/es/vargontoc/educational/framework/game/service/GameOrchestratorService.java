package es.vargontoc.educational.framework.game.service;

import es.vargontoc.educational.framework.content.model.Activity;
import es.vargontoc.educational.framework.content.model.Biome;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.DifficultyLevel;
import es.vargontoc.educational.framework.content.model.GameCatalogReadiness;
import es.vargontoc.educational.framework.content.model.RecognitionElement;
import es.vargontoc.educational.framework.content.model.RecognitionType;
import es.vargontoc.educational.framework.content.model.Topic;
import es.vargontoc.educational.framework.content.ports.in.GameCatalogUseCase;
import es.vargontoc.educational.framework.content.ports.in.TopicUseCase;
import es.vargontoc.educational.framework.content.ports.out.RecognitionElementRepository;
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
import es.vargontoc.educational.framework.game.model.recognition.RecognitionDefaults;
import es.vargontoc.educational.framework.game.model.recognition.RecognitionState;
import es.vargontoc.educational.framework.game.ports.in.GameEnginePort;
import es.vargontoc.educational.framework.game.ports.in.GameOrchestrator;
import es.vargontoc.educational.framework.game.ports.out.GameStateRegistry;
import es.vargontoc.educational.framework.game.ports.out.SessionAntiRepetitionRegistry;
import es.vargontoc.educational.framework.tracking.model.AttemptRegistrationResult;
import es.vargontoc.educational.framework.tracking.model.AttemptResult;
import es.vargontoc.educational.framework.tracking.model.GameSessionFinalStatus;
import es.vargontoc.educational.framework.tracking.model.UnlockedAchievement;
import es.vargontoc.educational.framework.tracking.ports.in.EvaluateGameCompletionAchievementsUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.FilterAllowedRecognitionCategoriesUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.RegisterActivityAttemptUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.RegisterGameSessionSummaryUseCase;
import es.vargontoc.educational.framework.tracking.ports.out.ElementProgressPort;
import es.vargontoc.educational.framework.tracking.model.ElementMasteryState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class GameOrchestratorService implements GameOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(GameOrchestratorService.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final GameCatalogUseCase gameCatalogUseCase;
    private final GameStateRegistry gameStateRegistry;
    private final SessionAntiRepetitionRegistry sessionAntiRepetitionRegistry;
    private final RegisterActivityAttemptUseCase registerActivityAttemptUseCase;
    private final EvaluateGameCompletionAchievementsUseCase evaluateGameCompletionAchievementsUseCase;
    private final RegisterGameSessionSummaryUseCase registerGameSessionSummaryUseCase;
    private final ApplicationEventPublisher eventPublisher;
    private final TopicUseCase topicUseCase;
    private final FilterAllowedRecognitionCategoriesUseCase filterAllowedRecognitionCategoriesUseCase;
    private final ElementProgressPort elementProgressPort;
    private final RecognitionElementRepository recognitionElementRepository;
    private final Map<String, GameEnginePort> engineInstances = new ConcurrentHashMap<>();
    private final Map<Long, ReentrantLock> gameLocks = new ConcurrentHashMap<>();

    public GameOrchestratorService(
            GameCatalogUseCase gameCatalogUseCase,
            GameStateRegistry gameStateRegistry,
            SessionAntiRepetitionRegistry sessionAntiRepetitionRegistry,
            RegisterActivityAttemptUseCase registerActivityAttemptUseCase,
            EvaluateGameCompletionAchievementsUseCase evaluateGameCompletionAchievementsUseCase,
            RegisterGameSessionSummaryUseCase registerGameSessionSummaryUseCase,
            ApplicationEventPublisher eventPublisher,
            TopicUseCase topicUseCase,
            FilterAllowedRecognitionCategoriesUseCase filterAllowedRecognitionCategoriesUseCase,
            ElementProgressPort elementProgressPort,
            RecognitionElementRepository recognitionElementRepository) {
        this.gameCatalogUseCase = gameCatalogUseCase;
        this.gameStateRegistry = gameStateRegistry;
        this.sessionAntiRepetitionRegistry = sessionAntiRepetitionRegistry;
        this.registerActivityAttemptUseCase = registerActivityAttemptUseCase;
        this.evaluateGameCompletionAchievementsUseCase = evaluateGameCompletionAchievementsUseCase;
        this.registerGameSessionSummaryUseCase = registerGameSessionSummaryUseCase;
        this.eventPublisher = eventPublisher;
        this.topicUseCase = topicUseCase;
        this.filterAllowedRecognitionCategoriesUseCase = filterAllowedRecognitionCategoriesUseCase;
        this.elementProgressPort = elementProgressPort;
        this.recognitionElementRepository = recognitionElementRepository;

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
        state.setChildProfileId(childProfileId);
        state.setActivityId(activityId);
        state.setDifficultyLevelId(difficultyLevel.getId());
        state.setEngine(resolveEngineType(activity));
        state.setStatus(GameStatus.WAITING);
        state.setStartedAt(LocalDateTime.now());
        state.setLastActivityAt(LocalDateTime.now());

        if (state.getEngine() == EngineType.RECOGNITION) {
            List<String> candidates = resolveCandidates(childProfileId, activity, launchContext);
            state.setCandidates(candidates);
            state.setRecognitionCategory(resolveRecognitionCategory(activity));
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

            String targetBeforeAction = null;
            if (state.getEngine() == EngineType.RECOGNITION && state.getEnginePayload() != null) {
                RecognitionState recStateBefore = deserializeRecognitionState(state.getEnginePayload());
                targetBeforeAction = recStateBefore.getTargetElementId();
            }

            ActionResult engineResult = engine.processAction(state, actionPayload);

            state.setLastActivityAt(LocalDateTime.now());

            AttemptResult trackingResult = mapToTrackingResult(engineResult.getResultType());

            Long elementId = null;
            if (targetBeforeAction != null) {
                try {
                    elementId = Long.parseLong(targetBeforeAction);
                } catch (NumberFormatException e) {
                    log.debug("targetElementId '{}' is not a numeric element ID, skipping element tracking", targetBeforeAction);
                }
            }

            if (state.getEngine() == EngineType.RECOGNITION && elementId != null) {
                List<RecognitionElement> resolvedElements = recognitionElementRepository.findAllById(List.of(elementId));
                if (!resolvedElements.isEmpty()) {
                    topicId = resolvedElements.get(0).getTopicId();
                } else {
                    log.debug("No RecognitionElement found for elementId={}, keeping client-supplied topicId", elementId);
                }
            }

            List<UnlockedAchievement> allUnlockedAchievements = new ArrayList<>();
            boolean difficultyChanged = false;
            Long newDifficultyLevelId = null;

            try {
                AttemptRegistrationResult attemptResult = registerActivityAttemptUseCase.register(
                    state.getChildProfileId(),
                    state.getActivityId(),
                    state.getChildSessionId(),
                    topicId,
                    elementId,
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

                    if (state.getEngine() == EngineType.RECOGNITION) {
                        applyDeferredDifficulty(state, newDifficultyLevelId);
                    } else {
                        state.setDifficultyLevelId(newDifficultyLevelId);
                    }
                }
            } catch (Exception e) {
                log.warn("Tracking operation failed, continuing without tracking update: {}", e.getMessage());
            }

            if (engineResult.getResultType() == ActionResultType.CORRECT
                    && state.getEngine() == EngineType.RECOGNITION) {
                if (targetBeforeAction != null && topicId != null) {
                    sessionAntiRepetitionRegistry.registerRecentElement(
                            state.getChildSessionId(), topicId, targetBeforeAction);
                }
                promotePendingDifficulty(state);
            }

            boolean gameCompleted = engineResult.isCompleted();

            if (gameCompleted) {
                state.setStatus(GameStatus.COMPLETED);
                state.setCompletedAt(LocalDateTime.now());

                try {
                    List<UnlockedAchievement> completionAchievements = evaluateGameCompletionAchievementsUseCase.evaluate(
                        state.getChildProfileId(),
                        state.getActivityId(),
                        topicId
                    );
                    if (completionAchievements != null) {
                        allUnlockedAchievements.addAll(completionAchievements);
                    }

                    registerGameSessionSummaryUseCase.registerGameSessionSummary(
                        state.getChildProfileId(),
                        state.getChildSessionId(),
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
                    state.getChildProfileId(),
                    state.getChildSessionId(),
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
                    state.getChildProfileId(),
                    state.getChildSessionId(),
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

    @Override
    public void clearSessionData(Long childSessionId) {
        sessionAntiRepetitionRegistry.clearSession(childSessionId);
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
        sb.append("]");
        if (state.getRecognitionCategory() != null) {
            sb.append(",\"recognitionCategory\":\"").append(state.getRecognitionCategory().name()).append("\"");
        }
        sb.append("}");
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

        List<String> candidates = new ArrayList<>();
        for (Topic topic : topics) {
            List<RecognitionElement> elements = recognitionElementRepository.findByTopicIdAndStatus(topic.getId(), ContentStatus.ACTIVE);
            if (elements.isEmpty()) {
                log.warn("Topic {} has zero active recognition elements, skipping", topic.getId());
                continue;
            }
            for (RecognitionElement element : elements) {
                candidates.add(String.valueOf(element.getId()));
            }
        }

        Long topicKey = activity.getTopicIds() != null && !activity.getTopicIds().isEmpty()
                ? activity.getTopicIds().get(0) : null;
        if (topicKey != null) {
            List<String> recentElements = sessionAntiRepetitionRegistry.getRecentElements(childProfileId, topicKey);
            if (!recentElements.isEmpty()) {
                List<String> filtered = candidates.stream()
                        .filter(c -> !recentElements.contains(c))
                        .toList();
                if (filtered.size() >= RecognitionDefaults.MIN_OPTIONS_PER_ROUND) {
                    candidates = filtered;
                } else {
                    log.warn("Anti-repetition filtering left too few candidates ({}) for childSessionId={}, topicId={}. Using all candidates.",
                            filtered.size(), childProfileId, topicKey);
                }
            }
        }

        candidates = prioritizeByMastery(childProfileId, topicKey, candidates);

        return candidates;
    }

    private List<String> prioritizeByMastery(Long childProfileId, Long topicId, List<String> candidates) {
        if (topicId == null || candidates.isEmpty()) {
            return candidates;
        }
        var summaries = elementProgressPort.getElementSummariesForChildInTopic(childProfileId, topicId);
        if (summaries.isEmpty()) {
            return candidates;
        }
        Map<String, ElementMasteryState> masteryByElementId = new java.util.HashMap<>();
        for (var s : summaries) {
            masteryByElementId.put(String.valueOf(s.getElementId()), s.getMasteryState());
        }
        return candidates.stream()
                .sorted((a, b) -> {
                    int orderA = masteryOrder(masteryByElementId.getOrDefault(a, ElementMasteryState.NOT_STARTED));
                    int orderB = masteryOrder(masteryByElementId.getOrDefault(b, ElementMasteryState.NOT_STARTED));
                    return Integer.compare(orderA, orderB);
                })
                .toList();
    }

    private int masteryOrder(ElementMasteryState state) {
        return switch (state) {
            case NOT_STARTED -> 0;
            case LEARNING -> 1;
            case MASTERED -> 2;
        };
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

    private void applyDeferredDifficulty(GameState state, Long newDifficultyLevelId) {
        try {
            RecognitionState recState = deserializeRecognitionState(state.getEnginePayload());
            recState.setPendingDifficultyLevel(newDifficultyLevelId.intValue());
            state.setEnginePayload(serializeRecognitionState(recState));
        } catch (Exception e) {
            log.warn("Failed to apply deferred difficulty: {}", e.getMessage());
            state.setDifficultyLevelId(newDifficultyLevelId);
        }
    }

    private void promotePendingDifficulty(GameState state) {
        try {
            RecognitionState recState = deserializeRecognitionState(state.getEnginePayload());
            if (recState.getPendingDifficultyLevel() != null) {
                recState.setCurrentDifficultyLevel(recState.getPendingDifficultyLevel());
                state.setDifficultyLevelId(recState.getPendingDifficultyLevel().longValue());
                recState.setPendingDifficultyLevel(null);
                state.setEnginePayload(serializeRecognitionState(recState));
            }
        } catch (Exception e) {
            log.warn("Failed to promote pending difficulty: {}", e.getMessage());
        }
    }

    private RecognitionState deserializeRecognitionState(String payload) {
        try {
            return OBJECT_MAPPER.readValue(payload, RecognitionState.class);
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to deserialize RecognitionState", e);
        }
    }

    private String serializeRecognitionState(RecognitionState state) {
        try {
            return OBJECT_MAPPER.writeValueAsString(state);
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to serialize RecognitionState", e);
        }
    }
}
