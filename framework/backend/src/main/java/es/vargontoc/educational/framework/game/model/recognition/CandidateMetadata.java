package es.vargontoc.educational.framework.game.model.recognition;

public record CandidateMetadata(String id, Long topicId, String similarityGroup, String code) {

    /**
     * Backward-compatible constructor for code that does not need the code field.
     */
    public CandidateMetadata(String id, Long topicId, String similarityGroup) {
        this(id, topicId, similarityGroup, null);
    }
}
