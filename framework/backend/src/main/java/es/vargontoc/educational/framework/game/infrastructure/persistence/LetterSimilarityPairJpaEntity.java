package es.vargontoc.educational.framework.game.infrastructure.persistence;

import es.vargontoc.educational.framework.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "letter_similarity_pair")
public class LetterSimilarityPairJpaEntity extends BaseEntity {

    @Column(name = "letter_code_a", nullable = false, length = 50)
    private String letterCodeA;

    @Column(name = "letter_code_b", nullable = false, length = 50)
    private String letterCodeB;

    @Column(nullable = false, length = 20)
    private String strength;

    public LetterSimilarityPairJpaEntity() {
    }

    public LetterSimilarityPairJpaEntity(String letterCodeA, String letterCodeB, String strength) {
        this.letterCodeA = letterCodeA;
        this.letterCodeB = letterCodeB;
        this.strength = strength;
    }

    public String getLetterCodeA() {
        return letterCodeA;
    }

    public void setLetterCodeA(String letterCodeA) {
        this.letterCodeA = letterCodeA;
    }

    public String getLetterCodeB() {
        return letterCodeB;
    }

    public void setLetterCodeB(String letterCodeB) {
        this.letterCodeB = letterCodeB;
    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }
}
