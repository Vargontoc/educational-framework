package es.vargontoc.educational.framework.family.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdultProfileJpaRepository extends JpaRepository<AdultProfileJpaEntity, Long> {

    List<AdultProfileJpaEntity> findByFamilyId(Long familyId);
}
