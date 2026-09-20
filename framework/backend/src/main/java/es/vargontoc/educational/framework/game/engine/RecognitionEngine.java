package es.vargontoc.educational.framework.game.engine;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import es.vargontoc.educational.framework.family.model.ColorVisionMode;
import es.vargontoc.educational.framework.game.model.ActionResult;
import es.vargontoc.educational.framework.game.model.ActionResultType;
import es.vargontoc.educational.framework.game.model.GameState;
import es.vargontoc.educational.framework.game.model.GameStatus;
import es.vargontoc.educational.framework.game.model.enums.EngineType;
import es.vargontoc.educational.framework.game.model.enums.RecognitionCategory;
import es.vargontoc.educational.framework.game.model.recognition.CandidateMetadata;
import es.vargontoc.educational.framework.game.model.recognition.DistractorStrategy;
import es.vargontoc.educational.framework.game.model.recognition.RecognitionAttemptContext;
import es.vargontoc.educational.framework.game.model.recognition.RecognitionDefaults;
import es.vargontoc.educational.framework.game.model.recognition.RecognitionState;
import es.vargontoc.educational.framework.game.model.recognition.RoundParameters;
import es.vargontoc.educational.framework.game.ports.in.GameEnginePort;
import es.vargontoc.educational.framework.game.service.ColorSimilarityValidator;
import es.vargontoc.educational.framework.game.service.DistractorSelector;
import es.vargontoc.educational.framework.game.service.RecognitionSimilarityService;

public class RecognitionEngine implements GameEnginePort {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final Random random;
    private final DistractorSelector distractorSelector;
    private final RecognitionSimilarityService recognitionSimilarityService;
    private final ColorSimilarityValidator colorSimilarityValidator;
    private final ColorVisionMode colorVisionMode;

    public RecognitionEngine() {
        this(new Random(), null, null, null);
    }

    public RecognitionEngine(Random random) {
        this(random, null, null, null);
    }

    public RecognitionEngine(RecognitionSimilarityService recognitionSimilarityService) {
        this(new Random(), recognitionSimilarityService, null, null);
    }

    public RecognitionEngine(Random random, RecognitionSimilarityService recognitionSimilarityService) {
        this(random, recognitionSimilarityService, null, null);
    }

    public RecognitionEngine(Random random, RecognitionSimilarityService recognitionSimilarityService,
                             ColorSimilarityValidator colorSimilarityValidator, ColorVisionMode colorVisionMode) {
        this.random = random;
        this.recognitionSimilarityService = recognitionSimilarityService;
        this.colorSimilarityValidator = colorSimilarityValidator;
        this.colorVisionMode = colorVisionMode;
        this.distractorSelector = new DistractorSelector(random, recognitionSimilarityService,
                colorSimilarityValidator, colorVisionMode);
    }

    @Override
    public void initGame(GameState gameState, String engineParams) {
        gameState.setStatus(GameStatus.IN_PROGRESS);
        gameState.setAttempts(0);
        gameState.setCorrectAttempts(0);
        gameState.setIncorrectAttempts(0);
        gameState.setTimeoutAttempts(0);
        gameState.setCurrentScore(BigDecimal.ZERO);
        gameState.setCurrentStreak(0);
        gameState.setStarsEarned(0);
        gameState.setSequenceNumber(0);
        gameState.setSystemEventPending(false);
        gameState.setStartedAt(LocalDateTime.now());
        gameState.setEngine(EngineType.RECOGNITION);

        List<String> candidates = parseCandidates(engineParams);
        RoundParameters roundParameters = parseRoundParameters(engineParams);
        List<CandidateMetadata> candidateMetadata = parseCandidateMetadata(engineParams);
        RecognitionState state = buildInitialState(
                candidates, roundParameters, candidateMetadata, parseRecognitionCategory(engineParams));
        gameState.setEnginePayload(serializeState(state));
    }

    @Override
    public ActionResult processAction(GameState gameState, String actionPayload) {
        RecognitionState state = deserializeState(gameState.getEnginePayload());

        if (state.getRoundIndex() >= state.getTotalRounds()) {
            return buildAlreadyCompleteResult(gameState, state);
        }

        String selectedOptionId = parseSelectedOptionId(actionPayload);
        Integer responseTimeMs = parseResponseTimeMs(actionPayload);

        boolean correct = selectedOptionId != null
                && selectedOptionId.equals(state.getTargetElementId());

        state.setCurrentRoundAttemptCount(state.getCurrentRoundAttemptCount() + 1);
        state.setSelectedOptionId(selectedOptionId);
        state.setLastActionAt(LocalDateTime.now());

        if (responseTimeMs != null) {
            state.setTotalResponseTimeMs(state.getTotalResponseTimeMs() + responseTimeMs);
        }

        if (correct) {
            state.setCurrentRoundConsecutiveFailures(0);
            if (state.getCurrentRoundAttemptCount() == 1) {
                state.setTotalCorrectFirstTry(state.getTotalCorrectFirstTry() + 1);
            }
        } else {
            state.setCurrentRoundConsecutiveFailures(
                    state.getCurrentRoundConsecutiveFailures() + 1);
            state.setTotalIncorrectAttempts(state.getTotalIncorrectAttempts() + 1);

            if (state.getCurrentRoundConsecutiveFailures()
                    >= RecognitionDefaults.HINT_ACTIVATION_THRESHOLD) {
                if (!state.isHintActive()) {
                    state.setHintTriggeredAtAttempt(state.getCurrentRoundAttemptCount());
                }
                state.setHintActive(true);
            }
        }

        String attemptContextJson = serializeAttemptContext(state, responseTimeMs);

        if (correct) {
            advanceRound(state);
        }

        gameState.setEnginePayload(serializeState(state));

        ActionResult result = new ActionResult();
        result.setResultType(correct ? ActionResultType.CORRECT : ActionResultType.INCORRECT);
        result.setResponseTimeMs(responseTimeMs);
        result.setNewState(gameState);
        result.setAttemptContext(attemptContextJson);
        result.setCompleted(state.getRoundIndex() >= state.getTotalRounds());

        return result;
    }

    @Override
    public String getNextElement(GameState gameState) {
        RecognitionState state = deserializeState(gameState.getEnginePayload());
        if (state.getRoundIndex() >= state.getTotalRounds()) {
            return null;
        }
        try {
            var map = new java.util.LinkedHashMap<String, Object>();
            map.put("targetElementId", state.getTargetElementId());
            map.put("optionIds", state.getOptionIds());
            map.put("roundIndex", state.getRoundIndex());
            map.put("guideChromEnabled", state.isGuideChromEnabled());
            map.put("touchEnableDelayMs", state.getTouchEnableDelayMs());
            map.put("nonChromaticKeyRequired", state.isNonChromaticKeyRequired());
            return OBJECT_MAPPER.writeValueAsString(map);
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to serialize next element", e);
        }
    }

    @Override
    public boolean isGameComplete(GameState gameState) {
        RecognitionState state = deserializeState(gameState.getEnginePayload());
        return state.getRoundIndex() >= state.getTotalRounds();
    }

    @Override
    public ActionResult buildSummary(GameState gameState) {
        RecognitionState state = deserializeState(gameState.getEnginePayload());

        int stars = calculateStars(state);

        gameState.setStatus(GameStatus.COMPLETED);
        gameState.setStarsEarned(stars);
        gameState.setCompletedAt(LocalDateTime.now());
        gameState.setIncorrectAttempts(state.getTotalIncorrectAttempts());
        gameState.setAttempts(state.getTotalIncorrectAttempts() + state.getTotalRounds());
        gameState.setEnginePayload(serializeState(state));

        ActionResult result = new ActionResult();
        result.setResultType(ActionResultType.CORRECT);
        result.setNewState(gameState);
        result.setCompleted(true);
        return result;
    }

    private void advanceRound(RecognitionState state) {
        state.setRoundIndex(state.getRoundIndex() + 1);
        if (state.getRoundIndex() >= state.getTotalRounds()) {
            return;
        }
        List<String> candidates = state.getCandidateElementIds();
        String target = selectTarget(candidates, state.getRoundsShownElementIds());
        state.setTargetElementId(target);
        state.setOptionIds(buildOptionsForState(state, candidates, target));
        state.getRoundsShownElementIds().add(target);
        state.setCurrentRoundAttemptCount(0);
        state.setCurrentRoundConsecutiveFailures(0);
        state.setHintActive(false);
        state.setHintTriggeredAtAttempt(null);
        state.setSelectedOptionId(null);
        state.setRoundStartedAt(LocalDateTime.now());
    }

    int calculateStars(RecognitionState state) {
        int totalRounds = state.getTotalRounds();
        int totalActions = state.getTotalIncorrectAttempts() + totalRounds;
        long avgResponseTime = totalActions > 0
                ? state.getTotalResponseTimeMs() / totalActions
                : 0L;

        if (state.getTotalCorrectFirstTry() >= 4
                && avgResponseTime <= RecognitionDefaults.GOOD_RESPONSE_TIME_THRESHOLD_MS) {
            return 3;
        }

        double avgAttemptsPerRound = totalRounds > 0
                ? (double) totalActions / totalRounds
                : 0.0;
        if (avgAttemptsPerRound <= 2.0) {
            return 2;
        }

        return 1;
    }

    private ActionResult buildAlreadyCompleteResult(GameState gameState, RecognitionState state) {
        ActionResult result = new ActionResult();
        result.setResultType(ActionResultType.INCORRECT);
        result.setNewState(gameState);
        result.setCompleted(true);
        return result;
    }

    private List<String> parseCandidates(String engineParams) {
        if (engineParams == null || engineParams.isBlank()) {
            return List.of();
        }
        try {
            var node = OBJECT_MAPPER.readTree(engineParams);
            var candidatesNode = node.get("candidates");
            if (candidatesNode == null || !candidatesNode.isArray()) {
                return List.of();
            }
            return OBJECT_MAPPER.convertValue(candidatesNode, new TypeReference<List<String>>() {});
        } catch (JacksonException e) {
            return List.of();
        }
    }

    private RecognitionCategory parseRecognitionCategory(String engineParams) {
        if (engineParams == null || engineParams.isBlank()) {
            return null;
        }
        try {
            var node = OBJECT_MAPPER.readTree(engineParams);
            var categoryNode = node.get("recognitionCategory");
            if (categoryNode == null || categoryNode.isNull()) {
                return null;
            }
            return RecognitionCategory.valueOf(categoryNode.asString());
        } catch (JacksonException | IllegalArgumentException e) {
            return null;
        }
    }

    private RoundParameters parseRoundParameters(String engineParams) {
        if (engineParams == null || engineParams.isBlank()) {
            return null;
        }
        try {
            var node = OBJECT_MAPPER.readTree(engineParams);
            var roundParametersNode = node.get("roundParameters");
            if (roundParametersNode == null || roundParametersNode.isNull()) {
                return null;
            }
            // Engine params written before showIcon existed do not carry it; default to true like RoundParameters does.
            if (roundParametersNode instanceof ObjectNode roundParametersObject && !roundParametersObject.has("showIcon")) {
                roundParametersObject.put("showIcon", true);
            }
            return OBJECT_MAPPER.convertValue(roundParametersNode, RoundParameters.class);
        } catch (JacksonException | IllegalArgumentException e) {
            return null;
        }
    }

    private List<CandidateMetadata> parseCandidateMetadata(String engineParams) {
        if (engineParams == null || engineParams.isBlank()) {
            return List.of();
        }
        try {
            var node = OBJECT_MAPPER.readTree(engineParams);
            var metadataNode = node.get("candidateMetadata");
            if (metadataNode == null || !metadataNode.isArray()) {
                return List.of();
            }
            return OBJECT_MAPPER.convertValue(metadataNode, new TypeReference<List<CandidateMetadata>>() {});
        } catch (JacksonException e) {
            return List.of();
        }
    }

    private RecognitionState buildInitialState(
            List<String> candidates,
            RoundParameters roundParameters,
            List<CandidateMetadata> candidateMetadata,
            RecognitionCategory category) {
        RecognitionState state = new RecognitionState();
        // The category must be known before the first round's options are built: category-specific
        // distractor selection (colour / letter / number similarity) depends on it.
        state.setRecognitionCategory(category);
        state.setRoundIndex(0);
        state.setTotalRounds(RecognitionDefaults.DEFAULT_TOTAL_ROUNDS);
        state.setRoundStartedAt(LocalDateTime.now());
        state.setRoundsShownElementIds(new ArrayList<>());
        state.setCurrentRoundAttemptCount(0);
        state.setCurrentRoundConsecutiveFailures(0);
        state.setTotalIncorrectAttempts(0);
        state.setTotalCorrectFirstTry(0);
        state.setHintActive(false);
        state.setSelectedOptionId(null);
        state.setTotalResponseTimeMs(0L);
        state.setCandidateElementIds(new ArrayList<>(candidates));
        state.setCandidateMetadata(candidateMetadata != null ? candidateMetadata : List.of());

        if (roundParameters != null) {
            state.setOptionCount(roundParameters.optionCount());
            state.setDistractorStrategy(roundParameters.distractorStrategy());
            state.setGuideChromEnabled(roundParameters.guideChromEnabled());
            state.setTouchEnableDelayMs(roundParameters.touchEnableDelayMs());
            state.setNonChromaticKeyRequired(roundParameters.nonChromaticKeyRequired());
            state.setShowIcon(roundParameters.showIcon());
        }

        String target = selectTarget(candidates, state.getRoundsShownElementIds());
        state.setTargetElementId(target);
        state.setOptionIds(buildOptionsForState(state, candidates, target));
        state.getRoundsShownElementIds().add(target);

        return state;
    }

    String selectTarget(List<String> candidates, List<String> shownElementIds) {
        if (candidates.isEmpty()) {
            return null;
        }
        if (candidates.size() == 1) {
            return candidates.get(0);
        }

        List<String> unshown = candidates.stream()
                .filter(c -> !shownElementIds.contains(c))
                .toList();

        List<String> pool = unshown.isEmpty() ? candidates : unshown;
        return pool.get(random.nextInt(pool.size()));
    }

    List<String> buildOptions(List<String> candidates, String target) {
        return buildOptions(candidates, target, DistractorStrategy.SEMANTICALLY_FAR, null, null, id -> null);
    }

    private List<String> buildOptionsForState(RecognitionState state, List<String> candidates, String target) {
        List<CandidateMetadata> metadata = state.getCandidateMetadata();
        java.util.Map<String, CandidateMetadata> byId = metadata == null
                ? java.util.Map.of()
                : metadata.stream().collect(java.util.stream.Collectors.toMap(
                        c -> c.id(), java.util.function.Function.identity(), (a, b) -> a));
        return buildOptions(candidates, target, state.getDistractorStrategy(), state.getOptionCount(),
                state.getRecognitionCategory(), byId::get);
    }

    List<String> buildOptions(
            List<String> candidates,
            String target,
            DistractorStrategy strategy,
            Integer optionCount,
            RecognitionCategory category,
            Function<String, CandidateMetadata> elementResolver) {
        if (target == null || candidates.isEmpty()) {
            return new ArrayList<>();
        }

        int resolvedOptionCount = optionCount != null
                ? Math.min(optionCount, candidates.size())
                : clampToDefaultRange(candidates.size());
        int distractorCount = Math.max(0, resolvedOptionCount - 1);

        List<String> distractors = distractorSelector.select(
                target,
                candidates,
                strategy != null ? strategy : DistractorStrategy.SEMANTICALLY_FAR,
                distractorCount,
                category,
                elementResolver);

        List<String> options = new ArrayList<>();
        options.add(target);
        options.addAll(distractors);
        Collections.shuffle(options, random);
        return options;
    }

    private int clampToDefaultRange(int candidatesSize) {
        int optionCount = Math.min(RecognitionDefaults.MAX_OPTIONS_PER_ROUND, candidatesSize);
        optionCount = Math.max(optionCount, RecognitionDefaults.MIN_OPTIONS_PER_ROUND);
        return Math.min(optionCount, candidatesSize);
    }

    private String serializeState(RecognitionState state) {
        try {
            return OBJECT_MAPPER.writeValueAsString(state);
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to serialize RecognitionState", e);
        }
    }

    private RecognitionState deserializeState(String payload) {
        if (payload == null || payload.isBlank()) {
            return new RecognitionState();
        }
        try {
            return OBJECT_MAPPER.readValue(payload, RecognitionState.class);
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to deserialize RecognitionState", e);
        }
    }

    String parseSelectedOptionId(String actionPayload) {
        if (actionPayload == null || actionPayload.isBlank()) {
            return null;
        }
        try {
            var node = OBJECT_MAPPER.readTree(actionPayload);
            var selectedNode = node.get("selectedOptionId");
            if (selectedNode == null || selectedNode.isNull()) {
                return null;
            }
            return selectedNode.asString();
        } catch (JacksonException e) {
            return null;
        }
    }

    Integer parseResponseTimeMs(String actionPayload) {
        if (actionPayload == null || actionPayload.isBlank()) {
            return null;
        }
        try {
            var node = OBJECT_MAPPER.readTree(actionPayload);
            var timeNode = node.get("responseTimeMs");
            if (timeNode == null || timeNode.isNull()) {
                return null;
            }
            return timeNode.asInt();
        } catch (JacksonException e) {
            return null;
        }
    }

    private String serializeAttemptContext(RecognitionState state, Integer responseTimeMs) {
        try {
            RecognitionAttemptContext ctx = new RecognitionAttemptContext();
            ctx.setRecognitionCategory(state.getRecognitionCategory());
            ctx.setRoundIndex(state.getRoundIndex());
            ctx.setTargetElementId(state.getTargetElementId());
            ctx.setSelectedOptionId(state.getSelectedOptionId());
            ctx.setOptionIds(state.getOptionIds());
            ctx.setFirstTry(state.getCurrentRoundAttemptCount() == 1);
            ctx.setHintActive(state.isHintActive());
            ctx.setHintTriggeredBeforeAnswer(
                    state.getHintTriggeredAtAttempt() != null
                            && state.getHintTriggeredAtAttempt() < state.getCurrentRoundAttemptCount());
            ctx.setAttemptNumberInRound(state.getCurrentRoundAttemptCount());
            ctx.setResponseTimeMs(responseTimeMs != null ? responseTimeMs : 0L);
            return OBJECT_MAPPER.writeValueAsString(ctx);
        } catch (JacksonException e) {
            return null;
        }
    }
}
