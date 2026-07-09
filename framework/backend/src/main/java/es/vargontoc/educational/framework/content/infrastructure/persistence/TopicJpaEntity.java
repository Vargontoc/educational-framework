package es.vargontoc.educational.framework.content.infrastructure.persistence;

import es.vargontoc.educational.framework.content.model.Biome;
import es.vargontoc.educational.framework.content.model.RecognitionType;
import es.vargontoc.educational.framework.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "topic")
public class TopicJpaEntity extends BaseEntity {

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "min_age")
    private Integer minAge;

    @Column(name = "max_age")
    private Integer maxAge;

    @Column(name = "compatible_variants", columnDefinition = "TEXT")
    private String compatibleVariants;

    @Column(name = "recognition_type", length = 20)
    @Enumerated(EnumType.STRING)
    private RecognitionType recognitionType;

    @Column(name = "habitat_tag", length = 20)
    @Enumerated(EnumType.STRING)
    private Biome habitatTag;

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getMinAge() {
        return minAge;
    }

    public void setMinAge(Integer minAge) {
        this.minAge = minAge;
    }

    public Integer getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
    }

    public String getCompatibleVariants() {
        return compatibleVariants;
    }

    public void setCompatibleVariants(String compatibleVariants) {
        this.compatibleVariants = compatibleVariants;
    }

    public RecognitionType getRecognitionType() {
        return recognitionType;
    }

    public void setRecognitionType(RecognitionType recognitionType) {
        this.recognitionType = recognitionType;
    }

    public Biome getHabitatTag() {
        return habitatTag;
    }

    public void setHabitatTag(Biome habitatTag) {
        this.habitatTag = habitatTag;
    }
}
