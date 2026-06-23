package es.vargontoc.educational.framework.content.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorldDiscoveryElementJpaRepository extends JpaRepository<WorldDiscoveryElementJpaEntity, Long> {
    boolean existsByCode(String code);
}
