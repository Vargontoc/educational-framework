package es.vargontoc.educational.framework.tracking.infrastructure.persistence;

import es.vargontoc.educational.framework.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "activity_attempt")
public class ActivityAttemptJpaEntity extends BaseEntity {

    @Column(name = "child_profile_id", nullable = false)
    private Long childProfileId;

    @Column(name = "activity_id", nullable = false)
    private Long activityId;

    @Column(name = "child_session_id")
    private Long childSessionId;

    @Column(name = "topic_id", nullable = false)
    private Long topicId;

    @Column(name = "element_id")
    private Long elementId;

    @Column(name = "difficulty_level_id", nullable = false)
    private Long difficultyLevelId;

    @Column(nullable = false, length = 20)
    private String result;

    @Column(name = "response_time_ms")
    private Integer responseTimeMs;

    @Column(name = "attempt_context", columnDefinition = "TEXT")
    private String attemptContext;

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

    public Long getChildSessionId() {
        return childSessionId;
    }

    public void setChildSessionId(Long childSessionId) {
        this.childSessionId = childSessionId;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public Long getElementId() {
        return elementId;
    }

    public void setElementId(Long elementId) {
        this.elementId = elementId;
    }

    public Long getDifficultyLevelId() {
        return difficultyLevelId;
    }

    public void setDifficultyLevelId(Long difficultyLevelId) {
        this.difficultyLevelId = difficultyLevelId;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Integer getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(Integer responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public String getAttemptContext() {
        return attemptContext;
    }

    public void setAttemptContext(String attemptContext) {
        this.attemptContext = attemptContext;
    }
}
