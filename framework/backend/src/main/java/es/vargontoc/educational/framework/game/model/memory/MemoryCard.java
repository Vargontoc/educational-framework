package es.vargontoc.educational.framework.game.model.memory;

/**
 * One card of the memory board. Two cards share the same {@code elementId} (they are a pair).
 * {@code faceUp} and {@code matched} are the only mutable facets: a matched card stays face up until the game ends.
 */
public class MemoryCard {

    private String cardId;
    private String elementId;
    private boolean faceUp;
    private boolean matched;
    private int row;
    private int column;

    public MemoryCard() {
    }

    public MemoryCard(String cardId, String elementId, int row, int column) {
        this.cardId = cardId;
        this.elementId = elementId;
        this.row = row;
        this.column = column;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    public boolean isFaceUp() {
        return faceUp;
    }

    public void setFaceUp(boolean faceUp) {
        this.faceUp = faceUp;
    }

    public boolean isMatched() {
        return matched;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }
}
