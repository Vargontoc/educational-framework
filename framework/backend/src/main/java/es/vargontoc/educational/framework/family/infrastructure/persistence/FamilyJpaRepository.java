package es.vargontoc.educational.framework.family.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyJpaRepository extends JpaRepository<FamilyJpaEntity, Long> {
}
