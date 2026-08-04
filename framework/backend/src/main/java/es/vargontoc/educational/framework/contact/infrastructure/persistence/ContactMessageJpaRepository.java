package es.vargontoc.educational.framework.contact.infrastructure.persistence;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactMessageJpaRepository extends JpaRepository<ContactMessageJpaEntity, Long> {
    
    long countByClientIpAndCreatedAtAfter(String clientIp, LocalDateTime since);
}
