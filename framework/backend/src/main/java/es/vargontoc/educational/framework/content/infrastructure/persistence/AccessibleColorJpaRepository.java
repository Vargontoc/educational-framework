package es.vargontoc.educational.framework.content.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccessibleColorJpaRepository extends JpaRepository<AccessibleColorJpaEntity, Long> {

    List<AccessibleColorJpaEntity> findByStatus(String status);

    boolean existsByConceptualIdentity(String conceptualIdentity);
}
