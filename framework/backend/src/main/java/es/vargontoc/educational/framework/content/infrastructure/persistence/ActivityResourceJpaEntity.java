package es.vargontoc.educational.framework.content.infrastructure.persistence;

import es.vargontoc.educational.framework.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "activity_resource")
public class ActivityResourceJpaEntity extends BaseEntity {

    @Column(name = "activity_id", nullable = false)
    private Long activityId;

    @Column(name = "topic_id")
    private Long topicId;

    @Column(name = "resource_type", nullable = false, length = 20)
    private String resourceType;

    @Column(nullable = false, length = 500)
    private String path;

    @Column(columnDefinition = "JSONB")
    private String metadata;

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

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}
