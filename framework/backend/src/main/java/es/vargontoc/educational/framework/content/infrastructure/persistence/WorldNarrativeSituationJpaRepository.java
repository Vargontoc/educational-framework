package es.vargontoc.educational.framework.content.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorldNarrativeSituationJpaRepository extends JpaRepository<WorldNarrativeSituationJpaEntity, Long> {
    Optional<WorldNarrativeSituationJpaEntity> findByCode(String code);
    boolean existsByCode(String code);

    @Query("SELECT s FROM WorldNarrativeSituationJpaEntity s WHERE s.status = :status AND s.minAge <= :targetAge AND s.maxAge >= :targetAge")
    List<WorldNarrativeSituationJpaEntity> findByStatusAndAgeRange(@Param("status") String status, @Param("targetAge") Integer targetAge);
}
