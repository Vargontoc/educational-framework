package es.vargontoc.educational.framework.world.model;

public class WorldNarrativeCompletionResult {

    private Long childSessionId;
    private Long learningPathId;
    private Long learningPathStepId;
    private boolean progressAdvanced;
    private WorldNarrativeCompletionStatus status;

    public WorldNarrativeCompletionResult() {
    }

    public WorldNarrativeCompletionResult(Long childSessionId, Long learningPathId, Long learningPathStepId,
                                      boolean progressAdvanced, WorldNarrativeCompletionStatus status) {
        this.childSessionId = childSessionId;
        this.learningPathId = learningPathId;
        this.learningPathStepId = learningPathStepId;
        this.progressAdvanced = progressAdvanced;
        this.status = status;
    }

    public Long getChildSessionId() {
        return childSessionId;
    }

    public void setChildSessionId(Long childSessionId) {
        this.childSessionId = childSessionId;
    }

    public Long getLearningPathId() {
        return learningPathId;
    }

    public void setLearningPathId(Long learningPathId) {
        this.learningPathId = learningPathId;
    }

    public Long getLearningPathStepId() {
        return learningPathStepId;
    }

    public void setLearningPathStepId(Long learningPathStepId) {
        this.learningPathStepId = learningPathStepId;
    }

    public boolean isProgressAdvanced() {
        return progressAdvanced;
    }

    public void setProgressAdvanced(boolean progressAdvanced) {
        this.progressAdvanced = progressAdvanced;
    }

    public WorldNarrativeCompletionStatus getStatus() {
        return status;
    }

    public void setStatus(WorldNarrativeCompletionStatus status) {
        this.status = status;
    }
}