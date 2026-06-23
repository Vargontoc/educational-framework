package es.vargontoc.educational.framework.content.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorldNarrativeSituationJpaRepository extends JpaRepository<WorldNarrativeSituationJpaEntity, Long> {
    Optional<WorldNarrativeSituationJpaEntity> findByCode(String code);
    boolean existsByCode(String code);
}
