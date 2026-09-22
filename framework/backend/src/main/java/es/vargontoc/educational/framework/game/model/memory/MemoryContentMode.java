package es.vargontoc.educational.framework.game.model.memory;

/** Which elements go on the board (ADR-030). It is the only content axis of the memory ladder. */
public enum MemoryContentMode {
    /** EASY: the pairs come from different thematic groups, so they are easy to tell apart. */
    HIGH_CONTRAST,
    /** MEDIUM: any elements. */
    STANDARD,
    /** HARD: every pair comes from the same thematic group, which raises the visual interference. */
    SAME_CATEGORY
}
