package es.vargontoc.educational.framework.content.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, Long> {

    Optional<CategoryJpaEntity> findByName(String name);
}
