package es.vargontoc.educational.framework.game.model.recognition;

public record RoundParameters(
        int optionCount,
        DistractorStrategy distractorStrategy,
        boolean guideChromEnabled,
        int touchEnableDelayMs,
        boolean nonChromaticKeyRequired) {
}
