package es.vargontoc.educational.framework.game.model.recognition;

public record RoundParameters(
        int optionCount,
        DistractorStrategy distractorStrategy,
        boolean guideChromEnabled,
        int touchEnableDelayMs,
        boolean nonChromaticKeyRequired,
        boolean showIcon) {

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
