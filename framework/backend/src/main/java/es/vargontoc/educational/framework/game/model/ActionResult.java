package es.vargontoc.educational.framework.game.model;

public class ActionResult {

    private ActionResultType resultType;
    private Integer responseTimeMs;
    private String recommendedAvatarEventType;
    private GameState newState;
    private boolean isCompleted;
    private String attemptContext;

    public ActionResultType getResultType() {
        return resultType;
    }

    public void setResultType(ActionResultType resultType) {
        this.resultType = resultType;
    }

    public Integer getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(Integer responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public String getRecommendedAvatarEventType() {
        return recommendedAvatarEventType;
    }

    public void setRecommendedAvatarEventType(String recommendedAvatarEventType) {
        this.recommendedAvatarEventType = recommendedAvatarEventType;
    }

    public GameState getNewState() {
        return newState;
    }

    public void setNewState(GameState newState) {
        this.newState = newState;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public String getAttemptContext() {
        return attemptContext;
    }

    public void setAttemptContext(String attemptContext) {
        this.attemptContext = attemptContext;
    }
}
