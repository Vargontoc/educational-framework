package es.vargontoc.educational.framework.game.model.recognition;

public record CandidateMetadata(String id, Long topicId, String similarityGroup, String code, String colorHex) {

    /**
     * Backward-compatible constructor without colorHex.
     */
    public CandidateMetadata(String id, Long topicId, String similarityGroup, String code) {
        this(id, topicId, similarityGroup, code, null);
    }

    /**
     * Backward-compatible constructor for code that does not need the code or colorHex fields.
     */
    public CandidateMetadata(String id, Long topicId, String similarityGroup) {
        this(id, topicId, similarityGroup, null, null);
    }
}
