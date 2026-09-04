package es.vargontoc.educational.framework.content.infrastructure.persistence;

import es.vargontoc.educational.framework.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "recognition_element")
public class RecognitionElementJpaEntity extends BaseEntity {

    @Column(name = "topic_id", nullable = false)
    private Long topicId;

    @Column(nullable = false, length = 100)
    private String code;

    @Column(name = "display_value", length = 50)
    private String displayValue;

    @Column(name = "resource_refs", columnDefinition = "TEXT")
    private String resourceRefs;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(nullable = false, length = 20)
    private String status;

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public void setDisplayValue(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getResourceRefs() {
        return resourceRefs;
    }

    public void setResourceRefs(String resourceRefs) {
        this.resourceRefs = resourceRefs;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
