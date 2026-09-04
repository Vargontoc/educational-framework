package es.vargontoc.educational.framework.contact.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TelegramNotificationChatJpaRepository extends JpaRepository<TelegramNotificationChatJpaEntity, Long> {

    Optional<TelegramNotificationChatJpaEntity> findTopByOrderByIdDesc();
}
