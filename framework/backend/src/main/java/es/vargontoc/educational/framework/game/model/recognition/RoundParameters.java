package es.vargontoc.educational.framework.game.model.recognition;

import java.util.List;

public record RoundParameters(
        int optionCount,
        DistractorStrategy distractorStrategy,
        boolean guideChromEnabled,
        int touchEnableDelayMs,
        boolean nonChromaticKeyRequired,
        boolean showIcon,
        List<Double> comparisonScales) {

    /**
     * Backward-compatible constructor without comparisonScales (only the comparison game has them).
     */
    public RoundParameters(
            int optionCount,
            DistractorStrategy distractorStrategy,
            boolean guideChromEnabled,
            int touchEnableDelayMs,
            boolean nonChromaticKeyRequired,
            boolean showIcon) {
        this(optionCount, distractorStrategy, guideChromEnabled, touchEnableDelayMs, nonChromaticKeyRequired, showIcon, null);
    }

    /**
     * Backward-compatible constructor without showIcon.
     */
    public RoundParameters(
            int optionCount,
            DistractorStrategy distractorStrategy,
            boolean guideChromEnabled,
            int touchEnableDelayMs,
            boolean nonChromaticKeyRequired) {
        this(optionCount, distractorStrategy, guideChromEnabled, touchEnableDelayMs, nonChromaticKeyRequired, true);
    }
}
