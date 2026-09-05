package es.vargontoc.educational.framework.game.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import es.vargontoc.educational.framework.game.model.enums.EngineType;
import es.vargontoc.educational.framework.game.model.enums.RecognitionCategory;

public class GameState {

    private Long gameId;
    private Long childSessionId;
    private Long childProfileId;
    private Long activityId;
    private Long difficultyLevelId;
    private GameStatus status;
    private ProgressMetricType metricType;
    private BigDecimal currentScore;
    private Integer currentStreak;
    private Integer starsEarned;
    private Integer attempts;
    private Integer correctAttempts;
    private Integer incorrectAttempts;
    private Integer timeoutAttempts;
    private LocalDateTime startedAt;
    private LocalDateTime lastActivityAt;
    private LocalDateTime completedAt;
    private Integer sequenceNumber;
    private boolean systemEventPending;
    private String enginePayload;
    private EngineType engine;
    private List<String> candidates;
    private RecognitionCategory recognitionCategory;

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public Long getChildSessionId() {
        return childSessionId;
    }

    public void setChildSessionId(Long childSessionId) {
        this.childSessionId = childSessionId;
    }

    public Long getChildProfileId() {
        return childProfileId;
    }

    public void setChildProfileId(Long childProfileId) {
        this.childProfileId = childProfileId;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public Long getDifficultyLevelId() {
        return difficultyLevelId;
    }

    public void setDifficultyLevelId(Long difficultyLevelId) {
        this.difficultyLevelId = difficultyLevelId;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public ProgressMetricType getMetricType() {
        return metricType;
    }

    public void setMetricType(ProgressMetricType metricType) {
        this.metricType = metricType;
    }

    public BigDecimal getCurrentScore() {
        return currentScore;
    }

    public void setCurrentScore(BigDecimal currentScore) {
        this.currentScore = currentScore;
    }

    public Integer getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(Integer currentStreak) {
        this.currentStreak = currentStreak;
    }

    public Integer getStarsEarned() {
        return starsEarned;
    }

    public void setStarsEarned(Integer starsEarned) {
        this.starsEarned = starsEarned;
    }

    public Integer getAttempts() {
        return attempts;
    }

    public void setAttempts(Integer attempts) {
        this.attempts = attempts;
    }

    public Integer getCorrectAttempts() {
        return correctAttempts;
    }

    public void setCorrectAttempts(Integer correctAttempts) {
        this.correctAttempts = correctAttempts;
    }

    public Integer getIncorrectAttempts() {
        return incorrectAttempts;
    }

    public void setIncorrectAttempts(Integer incorrectAttempts) {
        this.incorrectAttempts = incorrectAttempts;
    }

    public Integer getTimeoutAttempts() {
        return timeoutAttempts;
    }

    public void setTimeoutAttempts(Integer timeoutAttempts) {
        this.timeoutAttempts = timeoutAttempts;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(LocalDateTime lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public boolean isSystemEventPending() {
        return systemEventPending;
    }

    public void setSystemEventPending(boolean systemEventPending) {
        this.systemEventPending = systemEventPending;
    }

    public String getEnginePayload() {
        return enginePayload;
    }

    public void setEnginePayload(String enginePayload) {
        this.enginePayload = enginePayload;
    }

    public EngineType getEngine() {
        return engine;
    }

    public void setEngine(EngineType engine) {
        this.engine = engine;
    }

    public List<String> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<String> candidates) {
        this.candidates = candidates;
    }

    public RecognitionCategory getRecognitionCategory() {
        return recognitionCategory;
    }

    public void setRecognitionCategory(RecognitionCategory recognitionCategory) {
        this.recognitionCategory = recognitionCategory;
    }

}
