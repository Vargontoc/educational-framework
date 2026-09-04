package es.vargontoc.educational.framework.game.engine;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import es.vargontoc.educational.framework.game.model.ActionResult;
import es.vargontoc.educational.framework.game.model.GameState;
import es.vargontoc.educational.framework.game.model.GameStatus;
import es.vargontoc.educational.framework.game.model.enums.EngineType;
import es.vargontoc.educational.framework.game.model.recognition.RecognitionDefaults;
import es.vargontoc.educational.framework.game.model.recognition.RecognitionState;
import es.vargontoc.educational.framework.game.ports.in.GameEnginePort;

public class RecognitionEngine implements GameEnginePort {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

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
        throw new UnsupportedOperationException("Unimplemented method 'processAction'");
    }

    @Override
    public String getNextElement(GameState gameState) {
        throw new UnsupportedOperationException("Unimplemented method 'getNextElement'");
    }

    @Override
    public boolean isGameComplete(GameState gameState) {
        throw new UnsupportedOperationException("Unimplemented method 'isGameComplete'");
    }

    @Override
    public ActionResult buildSummary(GameState gameState) {
        throw new UnsupportedOperationException("Unimplemented method 'buildSummary'");
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
        } catch (JsonProcessingException e) {
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
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize RecognitionState", e);
        }
    }
}
