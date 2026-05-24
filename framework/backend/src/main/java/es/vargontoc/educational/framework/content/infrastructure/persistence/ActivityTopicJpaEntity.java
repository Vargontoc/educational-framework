package es.vargontoc.educational.framework.content.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;

@Entity
@Table(name = "activity_topic")
@IdClass(ActivityTopicJpaEntity.ActivityTopicId.class)
public class ActivityTopicJpaEntity {

    @Id
    @Column(name = "activity_id", nullable = false)
    private Long activityId;

    @Id
    @Column(name = "topic_id", nullable = false)
    private Long topicId;

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public static class ActivityTopicId implements Serializable {
        private Long activityId;
        private Long topicId;

        public ActivityTopicId() {}

        public ActivityTopicId(Long activityId, Long topicId) {
            this.activityId = activityId;
            this.topicId = topicId;
        }

        public Long getActivityId() {
            return activityId;
        }

        public void setActivityId(Long activityId) {
            this.activityId = activityId;
        }

        public Long getTopicId() {
            return topicId;
        }

        public void setTopicId(Long topicId) {
            this.topicId = topicId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ActivityTopicId that = (ActivityTopicId) o;
            return java.util.Objects.equals(activityId, that.activityId) &&
                   java.util.Objects.equals(topicId, that.topicId);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(activityId, topicId);
        }
    }
}
