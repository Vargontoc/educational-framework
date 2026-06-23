package es.vargontoc.educational.framework.content.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorldNarrativeSituationJpaRepository extends JpaRepository<WorldNarrativeSituationJpaEntity, Long> {
    boolean existsByCode(String code);
}
