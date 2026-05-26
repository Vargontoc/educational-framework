package es.vargontoc.educational.framework.content.infrastructure.persistence;

import es.vargontoc.educational.framework.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "story_page", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"story_id", "page_order"})
})
public class StoryPageJpaEntity extends BaseEntity {

    @Column(name = "story_id", nullable = false)
    private Long storyId;

    @Column(name = "page_order", nullable = false)
    private Integer pageOrder;

    @Column(length = 1000)
    private String text;

    @Column(name = "image_resource_ref", length = 500)
    private String imageResourceRef;

    @Column(name = "audio_resource_ref", length = 500)
    private String audioResourceRef;

    @Column(nullable = false, length = 20)
    private String status;

    public Long getStoryId() {
        return storyId;
    }

    public void setStoryId(Long storyId) {
        this.storyId = storyId;
    }

    public Integer getPageOrder() {
        return pageOrder;
    }

    public void setPageOrder(Integer pageOrder) {
        this.pageOrder = pageOrder;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImageResourceRef() {
        return imageResourceRef;
    }

    public void setImageResourceRef(String imageResourceRef) {
        this.imageResourceRef = imageResourceRef;
    }

    public String getAudioResourceRef() {
        return audioResourceRef;
    }

    public void setAudioResourceRef(String audioResourceRef) {
        this.audioResourceRef = audioResourceRef;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
