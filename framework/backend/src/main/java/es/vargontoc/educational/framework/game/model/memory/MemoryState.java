package es.vargontoc.educational.framework.game.model.memory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Engine state of a memory game, serialised in {@code GameState.enginePayload}. */
public class MemoryState {

    private int rows;
    private int columns;
    private int totalPairs;
    private int matchedPairs;
    private int flipDelayMs;
    private MemoryContentMode contentMode;
    private List<MemoryCard> cards = new ArrayList<>();
    /** The card turned first of the pair being played, null when no pair is in progress. */
    private String firstFlippedCardId;
    /** True while a non-matching pair is face up waiting to turn back. */
    private boolean waitingForFlipBack;
    private List<String> flipBackCardIds = new ArrayList<>();
    private LocalDateTime flipBackAt;
    /** Pairs of cards turned so far (matching or not). */
    private int pairAttempts;
    private int mismatchedAttempts;
    private long totalResponseTimeMs;
    private LocalDateTime lastActionAt;
    /** Matches of elements that had not been in an earlier non-matching pair. */
    private int totalCorrectFirstTry;
    /** Elements that took part in a non-matching pair so far (to tell a first-try match). */
    private List<String> mismatchedElementIds = new ArrayList<>();
    /** Thematic group of the board's elements, "MIXED" when several. */
    private String memoryCategory;
    /** Pair attempts buffered until the game completes; nothing is persisted before that. */
    private List<MemoryRoundAttemptRecord> roundAttempts = new ArrayList<>();

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public int getTotalPairs() {
        return totalPairs;
    }

    public void setTotalPairs(int totalPairs) {
        this.totalPairs = totalPairs;
    }

    public int getMatchedPairs() {
        return matchedPairs;
    }

    public void setMatchedPairs(int matchedPairs) {
        this.matchedPairs = matchedPairs;
    }

    public int getFlipDelayMs() {
        return flipDelayMs;
    }

    public void setFlipDelayMs(int flipDelayMs) {
        this.flipDelayMs = flipDelayMs;
    }

    public MemoryContentMode getContentMode() {
        return contentMode;
    }

    public void setContentMode(MemoryContentMode contentMode) {
        this.contentMode = contentMode;
    }

    public List<MemoryCard> getCards() {
        return cards;
    }

    public void setCards(List<MemoryCard> cards) {
        this.cards = cards;
    }

    public String getFirstFlippedCardId() {
        return firstFlippedCardId;
    }

    public void setFirstFlippedCardId(String firstFlippedCardId) {
        this.firstFlippedCardId = firstFlippedCardId;
    }

    public boolean isWaitingForFlipBack() {
        return waitingForFlipBack;
    }

    public void setWaitingForFlipBack(boolean waitingForFlipBack) {
        this.waitingForFlipBack = waitingForFlipBack;
    }

    public List<String> getFlipBackCardIds() {
        return flipBackCardIds;
    }

    public void setFlipBackCardIds(List<String> flipBackCardIds) {
        this.flipBackCardIds = flipBackCardIds;
    }

    public LocalDateTime getFlipBackAt() {
        return flipBackAt;
    }

    public void setFlipBackAt(LocalDateTime flipBackAt) {
        this.flipBackAt = flipBackAt;
    }

    public int getPairAttempts() {
        return pairAttempts;
    }

    public void setPairAttempts(int pairAttempts) {
        this.pairAttempts = pairAttempts;
    }

    public int getMismatchedAttempts() {
        return mismatchedAttempts;
    }

    public void setMismatchedAttempts(int mismatchedAttempts) {
        this.mismatchedAttempts = mismatchedAttempts;
    }

    public long getTotalResponseTimeMs() {
        return totalResponseTimeMs;
    }

    public void setTotalResponseTimeMs(long totalResponseTimeMs) {
        this.totalResponseTimeMs = totalResponseTimeMs;
    }

    public LocalDateTime getLastActionAt() {
        return lastActionAt;
    }

    public void setLastActionAt(LocalDateTime lastActionAt) {
        this.lastActionAt = lastActionAt;
    }

    public int getTotalCorrectFirstTry() {
        return totalCorrectFirstTry;
    }

    public void setTotalCorrectFirstTry(int totalCorrectFirstTry) {
        this.totalCorrectFirstTry = totalCorrectFirstTry;
    }

    public List<String> getMismatchedElementIds() {
        return mismatchedElementIds;
    }

    public void setMismatchedElementIds(List<String> mismatchedElementIds) {
        this.mismatchedElementIds = mismatchedElementIds;
    }

    public String getMemoryCategory() {
        return memoryCategory;
    }

    public void setMemoryCategory(String memoryCategory) {
        this.memoryCategory = memoryCategory;
    }

    public List<MemoryRoundAttemptRecord> getRoundAttempts() {
        return roundAttempts;
    }

    public void setRoundAttempts(List<MemoryRoundAttemptRecord> roundAttempts) {
        this.roundAttempts = roundAttempts;
    }
}
