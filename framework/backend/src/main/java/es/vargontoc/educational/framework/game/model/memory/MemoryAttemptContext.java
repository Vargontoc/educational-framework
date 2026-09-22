package es.vargontoc.educational.framework.game.model.memory;

import es.vargontoc.educational.framework.game.model.enums.EngineType;

/**
 * What one action of the memory game did, serialised as the attempt context. It is built for every processed
 * action; only the ones that resolve a pair (MATCH / MISMATCH) are attempts that get buffered for tracking.
 *
 * <p>Neutral by design (ADR-030): it describes the pair that was turned, it never grades the child.
 */
public class MemoryAttemptContext {

    private EngineType engineType = EngineType.MEMORY;
    /** Thematic group of the board's elements, "MIXED" when the board mixes several groups. */
    private String memoryCategory;
    /** FIRST_FLIP, MATCH, MISMATCH or IGNORED. */
    private String event;
    /** The first card of the pair (for FIRST_FLIP and IGNORED, the card touched). */
    private String cardId1;
    /** The second card of the pair; null until the pair is complete. */
    private String cardId2;
    /** Element of the pair; only known (and only set) when the pair matches. */
    private String elementId;
    private boolean isMatch;
    /** Number of the pair attempt (the one in progress for a FIRST_FLIP). */
    private int attemptNumber;
    private long responseTimeMs;
    /** A match whose element had not been part of any earlier non-matching pair. */
    private boolean isFirstTry;

    public EngineType getEngineType() {
        return engineType;
    }

    public void setEngineType(EngineType engineType) {
        this.engineType = engineType;
    }

    public String getMemoryCategory() {
        return memoryCategory;
    }

    public void setMemoryCategory(String memoryCategory) {
        this.memoryCategory = memoryCategory;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getCardId1() {
        return cardId1;
    }

    public void setCardId1(String cardId1) {
        this.cardId1 = cardId1;
    }

    public String getCardId2() {
        return cardId2;
    }

    public void setCardId2(String cardId2) {
        this.cardId2 = cardId2;
    }

    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    public boolean isMatch() {
        return isMatch;
    }

    public void setMatch(boolean match) {
        isMatch = match;
    }

    public int getAttemptNumber() {
        return attemptNumber;
    }

    public void setAttemptNumber(int attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

    public long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public boolean isFirstTry() {
        return isFirstTry;
    }

    public void setFirstTry(boolean firstTry) {
        isFirstTry = firstTry;
    }
}
