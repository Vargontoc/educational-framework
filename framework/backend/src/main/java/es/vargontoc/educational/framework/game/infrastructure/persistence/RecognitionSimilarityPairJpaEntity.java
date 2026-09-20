package es.vargontoc.educational.framework.game.infrastructure.persistence;

import es.vargontoc.educational.framework.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "recognition_similarity_pair")
public class RecognitionSimilarityPairJpaEntity extends BaseEntity {

    @Column(nullable = false, length = 20)
    private String category;

    @Column(name = "element_code_a", nullable = false, length = 50)
    private String elementCodeA;

    @Column(name = "element_code_b", nullable = false, length = 50)
    private String elementCodeB;

    @Column(nullable = false, length = 20)
    private String strength;

    public RecognitionSimilarityPairJpaEntity() {
    }

    public RecognitionSimilarityPairJpaEntity(String category, String elementCodeA, String elementCodeB, String strength) {
        this.category = category;
        this.elementCodeA = elementCodeA;
        this.elementCodeB = elementCodeB;
        this.strength = strength;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getElementCodeA() {
        return elementCodeA;
    }

    public void setElementCodeA(String elementCodeA) {
        this.elementCodeA = elementCodeA;
    }

    public String getElementCodeB() {
        return elementCodeB;
    }

    public void setElementCodeB(String elementCodeB) {
        this.elementCodeB = elementCodeB;
    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }
}
