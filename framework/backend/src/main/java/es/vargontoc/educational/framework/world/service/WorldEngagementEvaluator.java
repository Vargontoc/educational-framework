package es.vargontoc.educational.framework.world.service;

import es.vargontoc.educational.framework.world.model.WorldEngagementThresholdConfig;
import es.vargontoc.educational.framework.world.model.WorldEngineAbandonmentSignal;
import es.vargontoc.educational.framework.world.model.WorldEngineEngagementPattern;
import es.vargontoc.educational.framework.world.model.WorldEnginePriorityAdjustment;
import es.vargontoc.educational.framework.world.model.WorldEngagementWindow;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldEngagementEvaluator {

    private static final BigDecimal MIN_MULTIPLIER = new BigDecimal("0.1");
    private static final BigDecimal REDUCTION_STEP = new BigDecimal("0.2");

    public List<WorldEngineEngagementPattern> evaluatePatterns(
            WorldEngagementWindow window,
            WorldEngagementThresholdConfig config) {

        List<WorldEngineEngagementPattern> patterns = new ArrayList<>();
        Map<String, Integer> abandonmentByEngine = countAbandonmentsByEngine(window);

        for (Map.Entry<String, Integer> entry : abandonmentByEngine.entrySet()) {
            String engineType = entry.getKey();
            int abandonmentCount = entry.getValue();
            boolean patternDetected = abandonmentCount >= config.getAbandonmentThreshold();

            WorldEngineEngagementPattern pattern = new WorldEngineEngagementPattern(
                engineType,
                config.getWindowSize(),
                abandonmentCount,
                patternDetected
            );
            patterns.add(pattern);
        }

        return patterns;
    }

    public List<WorldEnginePriorityAdjustment> evaluateAdjustments(
            List<WorldEngineEngagementPattern> patterns) {

        List<WorldEnginePriorityAdjustment> adjustments = new ArrayList<>();

        for (WorldEngineEngagementPattern pattern : patterns) {
            if (pattern.isPatternDetected()) {
                BigDecimal multiplier = calculateMultiplier(pattern.getAbandonmentCount());
                WorldEnginePriorityAdjustment adjustment = new WorldEnginePriorityAdjustment(
                    pattern.getEngineType(),
                    multiplier,
                    "engagement_pattern_detected"
                );
                adjustments.add(adjustment);
            }
        }

        return adjustments;
    }

    private Map<String, Integer> countAbandonmentsByEngine(WorldEngagementWindow window) {
        Map<String, Integer> counts = new HashMap<>();

        if (window == null || window.getSignals() == null) {
            return counts;
        }

        for (WorldEngineAbandonmentSignal signal : window.getSignals()) {
            if ("ABANDONED".equals(signal.getFinalStatus())) {
                String engineType = signal.getEngineType();
                counts.merge(engineType, 1, (a, b) -> Integer.sum(a, b));
            }
        }

        return counts;
    }

    private BigDecimal calculateMultiplier(int abandonmentCount) {
        BigDecimal multiplier = BigDecimal.ONE.subtract(
            REDUCTION_STEP.multiply(BigDecimal.valueOf(abandonmentCount))
        );

        if (multiplier.compareTo(MIN_MULTIPLIER) < 0) {
            multiplier = MIN_MULTIPLIER;
        }

        return multiplier.setScale(1, RoundingMode.HALF_UP);
    }
}