package es.vargontoc.educational.framework.content.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityJpaRepository extends JpaRepository<ActivityJpaEntity, Long> {
}
