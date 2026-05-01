package es.vargontoc.educational.framework.family.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChildProfileJpaRepository extends JpaRepository<ChildProfileJpaEntity, Long> {

    List<ChildProfileJpaEntity> findByFamilyId(Long familyId);
}
