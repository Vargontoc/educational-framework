package es.vargontoc.educational.framework.game.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecognitionSimilarityPairJpaRepository extends JpaRepository<RecognitionSimilarityPairJpaEntity, Long> {

    @Query("SELECT p FROM RecognitionSimilarityPairJpaEntity p WHERE p.category = :category AND (p.elementCodeA = :code OR p.elementCodeB = :code)")
    List<RecognitionSimilarityPairJpaEntity> findByCategoryAndElementCode(@Param("category") String category, @Param("code") String code);

    @Query("SELECT p FROM RecognitionSimilarityPairJpaEntity p WHERE p.category = :category")
    List<RecognitionSimilarityPairJpaEntity> findAllByCategory(@Param("category") String category);
}
