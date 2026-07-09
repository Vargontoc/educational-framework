package es.vargontoc.educational.framework.content.infrastructure.persistence;

import es.vargontoc.educational.framework.family.model.ColorVisionMode;
import es.vargontoc.educational.framework.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "accessible_color_palette")
public class AccessibleColorPaletteJpaEntity extends BaseEntity {

    @Column(name = "accessible_color_id", nullable = false)
    private Long accessibleColorId;

    @Column(name = "color_vision_mode", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ColorVisionMode colorVisionMode;

    @Column(name = "accessible_color_value", nullable = false, length = 100)
    private String accessibleColorValue;

    @Column(name = "accessible_label_key", length = 100)
    private String accessibleLabelKey;

    public Long getAccessibleColorId() {
        return accessibleColorId;
    }

    public void setAccessibleColorId(Long accessibleColorId) {
        this.accessibleColorId = accessibleColorId;
    }

    public ColorVisionMode getColorVisionMode() {
        return colorVisionMode;
    }

    public void setColorVisionMode(ColorVisionMode colorVisionMode) {
        this.colorVisionMode = colorVisionMode;
    }

    public String getAccessibleColorValue() {
        return accessibleColorValue;
    }

    public void setAccessibleColorValue(String accessibleColorValue) {
        this.accessibleColorValue = accessibleColorValue;
    }

    public String getAccessibleLabelKey() {
        return accessibleLabelKey;
    }

    public void setAccessibleLabelKey(String accessibleLabelKey) {
        this.accessibleLabelKey = accessibleLabelKey;
    }
}
