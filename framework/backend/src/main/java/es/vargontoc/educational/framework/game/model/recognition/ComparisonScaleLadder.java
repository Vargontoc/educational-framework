package es.vargontoc.educational.framework.game.model.recognition;

import java.util.List;

import es.vargontoc.educational.framework.content.model.DifficultyCode;

/**
 * Size ladder of the big/small comparison game (ADR-029): the same object at progressively closer sizes.
 * The child is always asked for the biggest one.
 */
public final class ComparisonScaleLadder {

    /** Used when a round arrives without an explicit ladder. */
    public static final List<Double> DEFAULT = List.of(100.0, 40.0);

    private ComparisonScaleLadder() {
    }

    public static List<Double> forDifficulty(DifficultyCode difficultyCode) {
        if (difficultyCode == null) {
            return DEFAULT;
        }
        return switch (difficultyCode) {
            case EASY -> List.of(100.0, 40.0);
            case MEDIUM -> List.of(100.0, 65.0);
            case HARD -> List.of(100.0, 75.0, 50.0);
        };
    }
}
