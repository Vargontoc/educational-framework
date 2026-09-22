package es.vargontoc.educational.framework.game.model.memory;

/**
 * Board of one difficulty level.
 *
 * @param rows        rows of the board
 * @param columns     columns of the board
 * @param flipDelayMs how long a non-matching pair stays face up before turning back
 * @param contentMode which elements are dealt
 */
public record MemoryBoardConfig(int rows, int columns, int flipDelayMs, MemoryContentMode contentMode) {

    public int cardCount() {
        return rows * columns;
    }

    public int pairCount() {
        return cardCount() / 2;
    }
}
