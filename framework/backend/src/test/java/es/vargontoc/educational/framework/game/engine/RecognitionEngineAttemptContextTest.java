package es.vargontoc.educational.framework.game.engine;

import java.util.List;
import java.util.Random;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import es.vargontoc.educational.framework.game.model.ActionResult;
import es.vargontoc.educational.framework.game.model.ActionResultType;
import es.vargontoc.educational.framework.game.model.GameState;
import es.vargontoc.educational.framework.game.model.enums.EngineType;
import es.vargontoc.educational.framework.game.model.recognition.RecognitionState;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecognitionEngineAttemptContextTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private GameState createGameState() {
        GameState gs = new GameState();
        gs.setGameId(1L);
        gs.setChildSessionId(10L);
        gs.setActivityId(100L);
        return gs;
    }

    private String buildEngineParams(List<String> candidates) {
        try {
            return MAPPER.writeValueAsString(java.util.Map.of("candidates", candidates));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String buildActionPayload(String selectedOptionId, Integer responseTimeMs) {
        try {
            var map = new java.util.LinkedHashMap<String, Object>();
            map.put("selectedOptionId", selectedOptionId);
            if (responseTimeMs != null) {
                map.put("responseTimeMs", responseTimeMs);
            }
            return MAPPER.writeValueAsString(map);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private RecognitionState deserializeState(String payload) throws Exception {
        return MAPPER.readValue(payload, RecognitionState.class);
    }

    private JsonNode parseContext(String attemptContext) throws Exception {
        return MAPPER.readTree(attemptContext);
    }

    private GameState initEngine() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(42));
        GameState gs = createGameState();
        List<String> candidates = List.of("elem-1", "elem-2", "elem-3");
        engine.initGame(gs, buildEngineParams(candidates));
        return gs;
    }

    private String findWrongOption(RecognitionState state) {
        for (String opt : state.getOptionIds()) {
            if (!opt.equals(state.getTargetElementId())) {
                return opt;
            }
        }
        return null;
    }

    @Test
    void incorrectAttempt_contextContainsAllFields() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(42));
        GameState gs = initEngine();
        RecognitionState stateBefore = deserializeState(gs.getEnginePayload());
        String wrong = findWrongOption(stateBefore);
        String target = stateBefore.getTargetElementId();

        ActionResult result = engine.processAction(gs, buildActionPayload(wrong, 2000));

        assertEquals(ActionResultType.INCORRECT, result.getResultType());
        assertNotNull(result.getAttemptContext());

        JsonNode ctx = parseContext(result.getAttemptContext());
        assertEquals(EngineType.RECOGNITION.name(), ctx.get("engineType").asString());
        assertEquals(0, ctx.get("roundIndex").asInt());
        assertEquals(target, ctx.get("targetElementId").asString());
        assertEquals(wrong, ctx.get("selectedOptionId").asString());
        assertTrue(ctx.get("optionIds").isArray());
        assertEquals(stateBefore.getOptionIds().size(), ctx.get("optionIds").size());
        assertTrue(ctx.get("firstTry").asBoolean());
        assertFalse(ctx.get("hintActive").asBoolean());
        assertFalse(ctx.get("hintTriggeredBeforeAnswer").asBoolean());
        assertEquals(1, ctx.get("attemptNumberInRound").asInt());
        assertEquals(2000, ctx.get("responseTimeMs").asLong());
    }

    @Test
    void firstCorrectAttempt_isFirstTryTrueAndAttemptNumberOne() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(42));
        GameState gs = initEngine();
        RecognitionState stateBefore = deserializeState(gs.getEnginePayload());
        String target = stateBefore.getTargetElementId();

        ActionResult result = engine.processAction(gs, buildActionPayload(target, 1500));

        assertEquals(ActionResultType.CORRECT, result.getResultType());
        assertNotNull(result.getAttemptContext());

        JsonNode ctx = parseContext(result.getAttemptContext());
        assertTrue(ctx.get("firstTry").asBoolean());
        assertEquals(1, ctx.get("attemptNumberInRound").asInt());
        assertEquals(0, ctx.get("roundIndex").asInt());
        assertEquals(target, ctx.get("targetElementId").asString());
    }

    @Test
    void retryCorrectAttempt_isFirstTryFalseAndCorrectAttemptNumber() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(42));
        GameState gs = initEngine();
        RecognitionState stateBefore = deserializeState(gs.getEnginePayload());
        String target = stateBefore.getTargetElementId();
        String wrong = findWrongOption(stateBefore);

        engine.processAction(gs, buildActionPayload(wrong, 1000));
        ActionResult result = engine.processAction(gs, buildActionPayload(target, 3000));

        assertEquals(ActionResultType.CORRECT, result.getResultType());
        assertNotNull(result.getAttemptContext());

        JsonNode ctx = parseContext(result.getAttemptContext());
        assertFalse(ctx.get("firstTry").asBoolean());
        assertEquals(2, ctx.get("attemptNumberInRound").asInt());
        assertEquals(0, ctx.get("roundIndex").asInt());
        assertEquals(target, ctx.get("targetElementId").asString());
    }

    @Test
    void beforeHintActivation_hintFlagsAreFalse() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(42));
        GameState gs = initEngine();
        RecognitionState stateBefore = deserializeState(gs.getEnginePayload());
        String wrong = findWrongOption(stateBefore);

        ActionResult result = engine.processAction(gs, buildActionPayload(wrong, 1000));

        assertNotNull(result.getAttemptContext());
        JsonNode ctx = parseContext(result.getAttemptContext());
        assertFalse(ctx.get("hintActive").asBoolean());
        assertFalse(ctx.get("hintTriggeredBeforeAnswer").asBoolean());
    }

    @Test
    void afterHintActivation_hintFlagsAreCorrect() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(42));
        GameState gs = initEngine();
        RecognitionState stateBefore = deserializeState(gs.getEnginePayload());
        String wrong = findWrongOption(stateBefore);

        engine.processAction(gs, buildActionPayload(wrong, 1000));
        ActionResult hintActivationResult = engine.processAction(gs, buildActionPayload(wrong, 1500));

        JsonNode ctxAtActivation = parseContext(hintActivationResult.getAttemptContext());
        assertTrue(ctxAtActivation.get("hintActive").asBoolean());
        assertFalse(ctxAtActivation.get("hintTriggeredBeforeAnswer").asBoolean());

        ActionResult resultAfterHint = engine.processAction(gs, buildActionPayload(wrong, 2000));

        JsonNode ctxAfterHint = parseContext(resultAfterHint.getAttemptContext());
        assertTrue(ctxAfterHint.get("hintActive").asBoolean());
        assertTrue(ctxAfterHint.get("hintTriggeredBeforeAnswer").asBoolean());
        assertEquals(3, ctxAfterHint.get("attemptNumberInRound").asInt());
    }

    @Test
    void contextJsonStructure_matchesExpectedFieldNames() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(42));
        GameState gs = initEngine();
        RecognitionState stateBefore = deserializeState(gs.getEnginePayload());
        String wrong = findWrongOption(stateBefore);

        ActionResult result = engine.processAction(gs, buildActionPayload(wrong, 500));

        assertNotNull(result.getAttemptContext());
        JsonNode ctx = parseContext(result.getAttemptContext());

        assertTrue(ctx.has("engineType"));
        assertTrue(ctx.has("recognitionCategory"));
        assertTrue(ctx.has("roundIndex"));
        assertTrue(ctx.has("targetElementId"));
        assertTrue(ctx.has("selectedOptionId"));
        assertTrue(ctx.has("optionIds"));
        assertTrue(ctx.has("firstTry"));
        assertTrue(ctx.has("hintActive"));
        assertTrue(ctx.has("hintTriggeredBeforeAnswer"));
        assertTrue(ctx.has("attemptNumberInRound"));
        assertTrue(ctx.has("responseTimeMs"));
        assertFalse(ctx.has("topicId"));
    }

    @Test
    void contextDoesNotContainTopicId() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(42));
        GameState gs = initEngine();
        RecognitionState stateBefore = deserializeState(gs.getEnginePayload());
        String target = stateBefore.getTargetElementId();

        ActionResult result = engine.processAction(gs, buildActionPayload(target, 1000));

        assertNotNull(result.getAttemptContext());
        JsonNode ctx = parseContext(result.getAttemptContext());
        assertFalse(ctx.has("topicId"), "RecognitionAttemptContext must NOT contain topicId");
    }

    @Test
    void responseTimeMsNull_defaultsToZero() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(42));
        GameState gs = initEngine();
        RecognitionState stateBefore = deserializeState(gs.getEnginePayload());
        String target = stateBefore.getTargetElementId();

        ActionResult result = engine.processAction(gs, buildActionPayload(target, null));

        assertNotNull(result.getAttemptContext());
        JsonNode ctx = parseContext(result.getAttemptContext());
        assertEquals(0, ctx.get("responseTimeMs").asLong());
    }

    @Test
    void correctAnswer_contextReflectsCurrentRoundNotNext() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(42));
        GameState gs = initEngine();
        RecognitionState stateBefore = deserializeState(gs.getEnginePayload());
        String target = stateBefore.getTargetElementId();
        List<String> optionsBefore = stateBefore.getOptionIds();

        ActionResult result = engine.processAction(gs, buildActionPayload(target, 1000));

        JsonNode ctx = parseContext(result.getAttemptContext());
        assertEquals(0, ctx.get("roundIndex").asInt(),
                "Context must reflect the round the attempt was made in, not the next round");
        assertEquals(target, ctx.get("targetElementId").asString(),
                "Context must contain the target of the round the attempt was made in");
        assertEquals(optionsBefore.size(), ctx.get("optionIds").size(),
                "Context must contain the options of the round the attempt was made in");
    }

    @Test
    void multipleRetries_attemptNumberIncrements() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(42));
        GameState gs = initEngine();
        RecognitionState stateBefore = deserializeState(gs.getEnginePayload());
        String wrong = findWrongOption(stateBefore);
        String target = stateBefore.getTargetElementId();

        ActionResult r1 = engine.processAction(gs, buildActionPayload(wrong, 1000));
        JsonNode ctx1 = parseContext(r1.getAttemptContext());
        assertEquals(1, ctx1.get("attemptNumberInRound").asInt());
        assertTrue(ctx1.get("firstTry").asBoolean());

        ActionResult r2 = engine.processAction(gs, buildActionPayload(wrong, 1000));
        JsonNode ctx2 = parseContext(r2.getAttemptContext());
        assertEquals(2, ctx2.get("attemptNumberInRound").asInt());
        assertFalse(ctx2.get("firstTry").asBoolean());

        ActionResult r3 = engine.processAction(gs, buildActionPayload(target, 1000));
        JsonNode ctx3 = parseContext(r3.getAttemptContext());
        assertEquals(3, ctx3.get("attemptNumberInRound").asInt());
        assertFalse(ctx3.get("firstTry").asBoolean());
    }

    @Test
    void secondRound_attemptContextResetsCorrectly() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(42));
        GameState gs = initEngine();

        RecognitionState state0 = deserializeState(gs.getEnginePayload());
        String target0 = state0.getTargetElementId();
        engine.processAction(gs, buildActionPayload(target0, 1000));

        RecognitionState state1 = deserializeState(gs.getEnginePayload());
        String target1 = state1.getTargetElementId();

        ActionResult result = engine.processAction(gs, buildActionPayload(target1, 2000));

        JsonNode ctx = parseContext(result.getAttemptContext());
        assertEquals(1, ctx.get("roundIndex").asInt());
        assertEquals(1, ctx.get("attemptNumberInRound").asInt());
        assertTrue(ctx.get("firstTry").asBoolean());
        assertEquals(target1, ctx.get("targetElementId").asString());
        assertFalse(ctx.get("hintActive").asBoolean());
        assertFalse(ctx.get("hintTriggeredBeforeAnswer").asBoolean());
    }
}
