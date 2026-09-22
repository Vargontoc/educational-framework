package es.vargontoc.educational.framework.game.model.memory;

import es.vargontoc.educational.framework.content.model.DifficultyCode;

/**
 * Difficulty ladder of the memory game (ADR-030): only the board size and the time a non-matching pair stays
 * visible change; in HARD the elements also share a thematic group.
 */
public final class MemoryDifficultyLadder {

    public static final MemoryBoardConfig EASY = new MemoryBoardConfig(2, 2, 2000, MemoryContentMode.HIGH_CONTRAST);
    public static final MemoryBoardConfig MEDIUM = new MemoryBoardConfig(2, 3, 1500, MemoryContentMode.STANDARD);
    public static final MemoryBoardConfig HARD = new MemoryBoardConfig(2, 4, 1000, MemoryContentMode.SAME_CATEGORY);

    private MemoryDifficultyLadder() {
    }

    public static MemoryBoardConfig forDifficulty(DifficultyCode difficultyCode) {
        if (difficultyCode == null) {
            return EASY;
        }
        return switch (difficultyCode) {
            case EASY -> EASY;
            case MEDIUM -> MEDIUM;
            case HARD -> HARD;
        };
    }
}
