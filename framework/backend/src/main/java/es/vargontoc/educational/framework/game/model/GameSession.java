package es.vargontoc.educational.framework.game.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GameSession {

    private Long sessionId;
    private Long childProfileId;
    private Long activityId;
    private Long difficultyLevelStartId;
    private Long difficultyLevelEndId;
    private GameStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime endedAt;
    private LocalDateTime lastActivityAt;
    private List<GameState> gameStates = new ArrayList<>();
    private BigDecimal totalScore;
    private Integer totalAttempts;
    private Integer totalCorrect;
    private Integer totalIncorrect;
    private Integer totalTimeouts;

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
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

    public Long getDifficultyLevelStartId() {
        return difficultyLevelStartId;
    }

    public void setDifficultyLevelStartId(Long difficultyLevelStartId) {
        this.difficultyLevelStartId = difficultyLevelStartId;
    }

    public Long getDifficultyLevelEndId() {
        return difficultyLevelEndId;
    }

    public void setDifficultyLevelEndId(Long difficultyLevelEndId) {
        this.difficultyLevelEndId = difficultyLevelEndId;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public LocalDateTime getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(LocalDateTime lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    public List<GameState> getGameStates() {
        return gameStates;
    }

    public void setGameStates(List<GameState> gameStates) {
        this.gameStates = gameStates;
    }

    public BigDecimal getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(BigDecimal totalScore) {
        this.totalScore = totalScore;
    }

    public Integer getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(Integer totalAttempts) {
        this.totalAttempts = totalAttempts;
    }

    public Integer getTotalCorrect() {
        return totalCorrect;
    }

    public void setTotalCorrect(Integer totalCorrect) {
        this.totalCorrect = totalCorrect;
    }

    public Integer getTotalIncorrect() {
        return totalIncorrect;
    }

    public void setTotalIncorrect(Integer totalIncorrect) {
        this.totalIncorrect = totalIncorrect;
    }

    public Integer getTotalTimeouts() {
        return totalTimeouts;
    }

    public void setTotalTimeouts(Integer totalTimeouts) {
        this.totalTimeouts = totalTimeouts;
    }

    public void addGameState(GameState state) {
        this.gameStates.add(state);
    }
}
