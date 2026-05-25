package es.vargontoc.educational.framework.content.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AvatarEventCatalogJpaRepository extends JpaRepository<AvatarEventCatalogJpaEntity, Long> {

    List<AvatarEventCatalogJpaEntity> findByEventType(String eventType);

    List<AvatarEventCatalogJpaEntity> findByStatusAndEventTypeAndToneAndLocale(
        String status, String eventType, String tone, String locale);
}
