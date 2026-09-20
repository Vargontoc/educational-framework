package es.vargontoc.educational.framework.game.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LetterSimilarityPairJpaRepository extends JpaRepository<LetterSimilarityPairJpaEntity, Long> {

    @Query("SELECT p FROM LetterSimilarityPairJpaEntity p WHERE p.letterCodeA = :code OR p.letterCodeB = :code")
    List<LetterSimilarityPairJpaEntity> findByLetterCodeAOrLetterCodeB(@Param("code") String code);
}
