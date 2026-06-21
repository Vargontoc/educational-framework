package es.vargontoc.educational.framework.content.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ActivityJpaRepository extends JpaRepository<ActivityJpaEntity, Long> {

    Optional<ActivityJpaEntity> findByIdAndStatus(Long id, String status);
}
