package es.vargontoc.educational.framework.content.infrastructure.persistence;

import es.vargontoc.educational.framework.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "accessible_color")
public class AccessibleColorJpaEntity extends BaseEntity {

    @Column(name = "conceptual_identity", nullable = false, length = 100)
    private String conceptualIdentity;

    @Column(name = "label_key", length = 100)
    private String labelKey;

    @Column(name = "shape_icon", length = 50)
    private String shapeIcon;

    @Column(length = 50)
    private String symbol;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "sort_order")
    private Integer sortOrder;

    public String getConceptualIdentity() {
        return conceptualIdentity;
    }

    public void setConceptualIdentity(String conceptualIdentity) {
        this.conceptualIdentity = conceptualIdentity;
    }

    public String getLabelKey() {
        return labelKey;
    }

    public void setLabelKey(String labelKey) {
        this.labelKey = labelKey;
    }

    public String getShapeIcon() {
        return shapeIcon;
    }

    public void setShapeIcon(String shapeIcon) {
        this.shapeIcon = shapeIcon;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
