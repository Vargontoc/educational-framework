package es.vargontoc.educational.framework.world.service;

import es.vargontoc.educational.framework.world.model.WorldEngagementThresholdConfig;
import es.vargontoc.educational.framework.world.model.WorldEngineAbandonmentSignal;
import es.vargontoc.educational.framework.world.model.WorldEngineEngagementPattern;
import es.vargontoc.educational.framework.world.model.WorldEnginePriorityAdjustment;
import es.vargontoc.educational.framework.world.model.WorldEngagementWindow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldEngagementEvaluatorTest {

    private WorldEngagementEvaluator evaluator;
    private WorldEngagementThresholdConfig config;

    @BeforeEach
    void setUp() {
        evaluator = new WorldEngagementEvaluator();
        config = new WorldEngagementThresholdConfig();
        config.setWindowSize(3);
        config.setAbandonmentThreshold(2);
    }

    private WorldEngagementWindow createWindow(WorldEngineAbandonmentSignal... signals) {
        WorldEngagementWindow window = new WorldEngagementWindow();
        window.setChildSessionId(100L);
        for (WorldEngineAbandonmentSignal signal : signals) {
            window.addSignal(signal);
        }
        return window;
    }

    private WorldEngineAbandonmentSignal createSignal(String engineType, String finalStatus) {
        WorldEngineAbandonmentSignal signal = new WorldEngineAbandonmentSignal();
        signal.setEngineType(engineType);
        signal.setFinalStatus(finalStatus);
        signal.setOccurredAt(LocalDateTime.now());
        return signal;
    }

    @Test
    void evaluate_oneAbandonment_doesNotTriggerPattern() {
        WorldEngagementWindow window = createWindow(createSignal("PUZZLE", "ABANDONED"));
        List<WorldEngineEngagementPattern> patterns = evaluator.evaluatePatterns(window, config);

        assertEquals(1, patterns.size());
        assertFalse(patterns.get(0).isPatternDetected());
        assertEquals(1, patterns.get(0).getAbandonmentCount());
    }

    @Test
    void evaluate_twoOfThreeAbandonments_triggersPattern() {
        WorldEngagementWindow window = createWindow(
            createSignal("PUZZLE", "ABANDONED"),
            createSignal("PUZZLE", "ABANDONED")
        );
        List<WorldEngineEngagementPattern> patterns = evaluator.evaluatePatterns(window, config);

        assertEquals(1, patterns.size());
        assertTrue(patterns.get(0).isPatternDetected());
        assertEquals(2, patterns.get(0).getAbandonmentCount());
    }

    @Test
    void evaluate_ignoredProposals_doNotCount() {
        WorldEngagementWindow window = createWindow(
            createSignal("PUZZLE", "IGNORED"),
            createSignal("PUZZLE", "IGNORED"),
            createSignal("PUZZLE", "IGNORED")
        );
        List<WorldEngineEngagementPattern> patterns = evaluator.evaluatePatterns(window, config);

        assertEquals(0, patterns.size());
    }

    @Test
    void evaluate_allAbandonmentsSameEngine_reducesPriority() {
        WorldEngagementWindow window = createWindow(
            createSignal("PUZZLE", "ABANDONED"),
            createSignal("PUZZLE", "ABANDONED")
        );
        List<WorldEngineEngagementPattern> patterns = evaluator.evaluatePatterns(window, config);
        List<WorldEnginePriorityAdjustment> adjustments = evaluator.evaluateAdjustments(patterns);

        assertEquals(1, adjustments.size());
        assertTrue(adjustments.get(0).getPriorityMultiplier().compareTo(BigDecimal.ONE) < 0);
    }

    @Test
    void evaluate_differentEngines_evaluatedSeparately() {
        WorldEngagementWindow window = createWindow(
            createSignal("PUZZLE", "ABANDONED"),
            createSignal("PUZZLE", "ABANDONED"),
            createSignal("STORY", "ABANDONED")
        );
        List<WorldEngineEngagementPattern> patterns = evaluator.evaluatePatterns(window, config);

        assertEquals(2, patterns.size());
    }

    @Test
    void evaluate_priorityMultiplierNeverExceedsOne() {
        WorldEngagementWindow window = createWindow(
            createSignal("PUZZLE", "ABANDONED")
        );
        List<WorldEngineEngagementPattern> patterns = evaluator.evaluatePatterns(window, config);
        List<WorldEnginePriorityAdjustment> adjustments = evaluator.evaluateAdjustments(patterns);

        for (WorldEnginePriorityAdjustment adj : adjustments) {
            assertTrue(adj.getPriorityMultiplier().compareTo(BigDecimal.ONE) <= 0);
        }
    }

    @Test
    void evaluate_mixedStatuses_countsOnlyAbandoned() {
        WorldEngagementWindow window = createWindow(
            createSignal("PUZZLE", "ABANDONED"),
            createSignal("PUZZLE", "COMPLETED"),
            createSignal("PUZZLE", "IGNORED")
        );
        List<WorldEngineEngagementPattern> patterns = evaluator.evaluatePatterns(window, config);

        assertEquals(1, patterns.size());
        assertFalse(patterns.get(0).isPatternDetected());
        assertEquals(1, patterns.get(0).getAbandonmentCount());
    }
}