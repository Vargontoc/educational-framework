package es.vargontoc.educational.framework.tracking.infrastructure.persistence;

import es.vargontoc.educational.framework.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(name = "curiosity_viewed", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_curiosity_viewed_child_topic_curiosity_cycle",
                columnNames = {"child_profile_id", "topic_id", "curiosity_id", "cycle_number"}
        )
})
public class CuriosityViewedJpaEntity extends BaseEntity {

    @Column(name = "child_profile_id", nullable = false)
    private Long childProfileId;

    @Column(name = "topic_id", nullable = false)
    private Long topicId;

    @Column(name = "curiosity_id", nullable = false)
    private Long curiosityId;

    @Column(name = "cycle_number", nullable = false)
    private Integer cycleNumber;

    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;

    public Long getChildProfileId() {
        return childProfileId;
    }

    public void setChildProfileId(Long childProfileId) {
        this.childProfileId = childProfileId;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public Long getCuriosityId() {
        return curiosityId;
    }

    public void setCuriosityId(Long curiosityId) {
        this.curiosityId = curiosityId;
    }

    public Integer getCycleNumber() {
        return cycleNumber;
    }

    public void setCycleNumber(Integer cycleNumber) {
        this.cycleNumber = cycleNumber;
    }

    public LocalDateTime getViewedAt() {
        return viewedAt;
    }

    public void setViewedAt(LocalDateTime viewedAt) {
        this.viewedAt = viewedAt;
    }
}
