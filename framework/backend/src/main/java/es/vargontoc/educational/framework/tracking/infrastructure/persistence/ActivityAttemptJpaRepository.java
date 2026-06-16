package es.vargontoc.educational.framework.tracking.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityAttemptJpaRepository extends JpaRepository<ActivityAttemptJpaEntity, Long> {
}
