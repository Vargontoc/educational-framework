package es.vargontoc.educational.framework.game.engine;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import es.vargontoc.educational.framework.game.model.ActionResult;
import es.vargontoc.educational.framework.game.model.ActionResultType;
import es.vargontoc.educational.framework.game.model.GameState;
import es.vargontoc.educational.framework.game.model.GameStatus;
import es.vargontoc.educational.framework.game.model.enums.EngineType;
import es.vargontoc.educational.framework.game.model.recognition.RecognitionAttemptContext;
import es.vargontoc.educational.framework.game.model.recognition.RecognitionDefaults;
import es.vargontoc.educational.framework.game.model.recognition.RecognitionState;
import es.vargontoc.educational.framework.game.ports.in.GameEnginePort;

public class RecognitionEngine implements GameEnginePort {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final Random random;

    public RecognitionEngine() {
        this(new Random());
    }

    public RecognitionEngine(Random random) {
        this.random = random;
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
        RecognitionState state = buildInitialState(candidates);
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

        if (correct) {
            advanceRound(state);
        }

        gameState.setEnginePayload(serializeState(state));

        ActionResult result = new ActionResult();
        result.setResultType(correct ? ActionResultType.CORRECT : ActionResultType.INCORRECT);
        result.setResponseTimeMs(responseTimeMs);
        result.setNewState(gameState);
        result.setAttemptContext(serializeAttemptContext(state, responseTimeMs));
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
        state.setOptionIds(buildOptions(candidates, target));
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

    private RecognitionState buildInitialState(List<String> candidates) {
        RecognitionState state = new RecognitionState();
        state.setRoundIndex(0);
        state.setTotalRounds(RecognitionDefaults.DEFAULT_TOTAL_ROUNDS);
        state.setCurrentDifficultyLevel(RecognitionDefaults.DEFAULT_DIFFICULTY_LEVEL);
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

        String target = selectTarget(candidates, state.getRoundsShownElementIds());
        state.setTargetElementId(target);
        state.setOptionIds(buildOptions(candidates, target));
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
        if (target == null || candidates.isEmpty()) {
            return new ArrayList<>();
        }

        int optionCount = Math.min(RecognitionDefaults.MAX_OPTIONS_PER_ROUND, candidates.size());
        optionCount = Math.max(optionCount, RecognitionDefaults.MIN_OPTIONS_PER_ROUND);
        optionCount = Math.min(optionCount, candidates.size());

        List<String> distractors = new ArrayList<>();
        for (String c : candidates) {
            if (!c.equals(target)) {
                distractors.add(c);
            }
        }
        Collections.shuffle(distractors, random);

        int distractorCount = Math.min(optionCount - 1, distractors.size());
        List<String> options = new ArrayList<>();
        options.add(target);
        for (int i = 0; i < distractorCount; i++) {
            options.add(distractors.get(i));
        }
        Collections.shuffle(options, random);
        return options;
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
