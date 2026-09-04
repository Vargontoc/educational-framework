package es.vargontoc.educational.framework.game.ports.in;

import es.vargontoc.educational.framework.game.model.ActionResult;
import es.vargontoc.educational.framework.game.model.ActionResultType;
import es.vargontoc.educational.framework.game.model.GameState;
import es.vargontoc.educational.framework.game.model.GameStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FakeGameEngineTest {

    private FakeGameEngine engine;
    private GameState gameState;

    @BeforeEach
    void setUp() {
        engine = new FakeGameEngine(3);
        gameState = new GameState();
        gameState.setGameId(1L);
        gameState.setChildSessionId(10L);
        gameState.setActivityId(100L);
        gameState.setDifficultyLevelId(5L);
    }

    @Test
    void initGame_setsInitialState() {
        engine.initGame(gameState, "{\"difficulty\":\"easy\"}");

        assertEquals(GameStatus.IN_PROGRESS, gameState.getStatus());
        assertEquals(0, gameState.getAttempts());
        assertEquals(0, gameState.getCorrectAttempts());
        assertEquals(0, gameState.getIncorrectAttempts());
        assertEquals(BigDecimal.ZERO, gameState.getCurrentScore());
        assertEquals(0, gameState.getCurrentStreak());
        assertEquals(0, gameState.getSequenceNumber());
        assertFalse(gameState.isSystemEventPending());
        assertNotNull(gameState.getStartedAt());
        assertEquals("{\"difficulty\":\"easy\"}", gameState.getEnginePayload());
    }

    @Test
    void processAction_correctAction_incrementsCorrectAndScore() {
        engine.initGame(gameState, null);

        ActionResult result = engine.processAction(gameState, "CORRECT:answer");

        assertEquals(ActionResultType.CORRECT, result.getResultType());
        assertEquals(2000, result.getResponseTimeMs());
        assertEquals("CORRECT_ANSWER", result.getRecommendedAvatarEventType());
        assertNotNull(result.getAttemptContext());
        assertTrue(result.getAttemptContext().startsWith("fake-engine-context-"));
        assertFalse(result.isCompleted());
        assertEquals(1, gameState.getCorrectAttempts());
        assertEquals(BigDecimal.TEN, gameState.getCurrentScore());
        assertEquals(1, gameState.getCurrentStreak());
    }

    @Test
    void processAction_incorrectAction_incrementsIncorrectAndResetsStreak() {
        engine.initGame(gameState, null);

        ActionResult result = engine.processAction(gameState, "INCORRECT:wrong");

        assertEquals(ActionResultType.INCORRECT, result.getResultType());
        assertEquals(1500, result.getResponseTimeMs());
        assertEquals("INCORRECT_HINT", result.getRecommendedAvatarEventType());
        assertEquals(1, gameState.getIncorrectAttempts());
        assertEquals(0, gameState.getCurrentStreak());
        assertFalse(result.isCompleted());
    }

    @Test
    void processAction_timeoutAction_incrementsTimeout() {
        engine.initGame(gameState, null);

        ActionResult result = engine.processAction(gameState, null);

        assertEquals(ActionResultType.TIMEOUT, result.getResultType());
        assertEquals(30000, result.getResponseTimeMs());
        assertEquals("TRY_AGAIN", result.getRecommendedAvatarEventType());
        assertEquals(1, gameState.getTimeoutAttempts());
        assertFalse(result.isCompleted());
    }

    @Test
    void processAction_completesAfterConfiguredCorrectActions() {
        engine.initGame(gameState, null);

        ActionResult result1 = engine.processAction(gameState, "CORRECT:1");
        assertFalse(result1.isCompleted());

        ActionResult result2 = engine.processAction(gameState, "CORRECT:2");
        assertFalse(result2.isCompleted());

        ActionResult result3 = engine.processAction(gameState, "CORRECT:3");
        assertTrue(result3.isCompleted());
        assertEquals(GameStatus.COMPLETED, gameState.getStatus());
        assertNotNull(gameState.getCompletedAt());
        assertEquals("GAME_COMPLETE", result3.getRecommendedAvatarEventType());
    }

    @Test
    void getNextElement_returnsEnginePayload() {
        engine.initGame(gameState, null);

        String element = engine.getNextElement(gameState);

        assertNotNull(element);
        assertTrue(element.contains("question"));
        assertTrue(element.contains("Fake Question 1"), "Expected interpolated sequence number, got: " + element);
    }

    @Test
    void isGameComplete_returnsFalseWhenNotEnoughCorrect() {
        engine.initGame(gameState, null);

        assertFalse(engine.isGameComplete(gameState));

        engine.processAction(gameState, "CORRECT:1");

        assertFalse(engine.isGameComplete(gameState));
    }

    @Test
    void isGameComplete_returnsTrueWhenEnoughCorrect() {
        engine.initGame(gameState, null);

        engine.processAction(gameState, "CORRECT:1");
        engine.processAction(gameState, "CORRECT:2");
        engine.processAction(gameState, "CORRECT:3");

        assertTrue(engine.isGameComplete(gameState));
    }

    @Test
    void buildSummary_returnsSummaryResult() {
        engine.initGame(gameState, null);
        engine.processAction(gameState, "CORRECT:1");

        ActionResult summary = engine.buildSummary(gameState);

        assertTrue(summary.isCompleted());
        assertEquals(ActionResultType.CORRECT, summary.getResultType());
        assertNotNull(summary.getNewState());
        assertEquals("fake-engine-summary", summary.getAttemptContext());
    }

    @Test
    void engineContract_hasNoAdaptiveDifficultyMethod() {
        Method[] methods = GameEnginePort.class.getDeclaredMethods();

        for (Method method : methods) {
            assertFalse(method.getName().contains("Adaptive"),
                "GameEnginePort should not have adaptive difficulty methods");
            assertFalse(method.getName().contains("Difficulty"),
                "GameEnginePort should not have difficulty evaluation methods");
        }
    }

    @Test
    void engineContract_hasRequiredMethods() {
        Method[] methods = GameEnginePort.class.getDeclaredMethods();
        var methodNames = new java.util.HashSet<String>();
        for (Method method : methods) {
            methodNames.add(method.getName());
        }

        assertTrue(methodNames.contains("initGame"));
        assertTrue(methodNames.contains("processAction"));
        assertTrue(methodNames.contains("getNextElement"));
        assertTrue(methodNames.contains("isGameComplete"));
        assertTrue(methodNames.contains("buildSummary"));
    }

    @Test
    void fakeEngine_assignsAttemptContext() {
        engine.initGame(gameState, null);

        ActionResult result1 = engine.processAction(gameState, "CORRECT:1");
        ActionResult result2 = engine.processAction(gameState, "INCORRECT:2");

        assertEquals("fake-engine-context-1", result1.getAttemptContext());
        assertEquals("fake-engine-context-2", result2.getAttemptContext());
    }

    @Test
    void fakeEngine_calculatesStars() {
        engine.initGame(gameState, null);

        engine.processAction(gameState, "CORRECT:1");
        engine.processAction(gameState, "CORRECT:2");
        engine.processAction(gameState, "CORRECT:3");

        assertEquals(3, gameState.getStarsEarned());
    }
}
