package es.vargontoc.educational.framework.content.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AvatarEventCatalogJpaRepository extends JpaRepository<AvatarEventCatalogJpaEntity, Long> {

    List<AvatarEventCatalogJpaEntity> findByEventType(String eventType);

    List<AvatarEventCatalogJpaEntity> findByStatusAndEventTypeAndToneAndLocale(
        String status, String eventType, String tone, String locale);

    List<AvatarEventCatalogJpaEntity> findByStatusAndEventTypeAndBiomeIsNull(
        String status, String eventType);

    List<AvatarEventCatalogJpaEntity> findByStatusAndEventTypeAndBiome(
        String status, String eventType, String biome);

    @Query("SELECT e FROM AvatarEventCatalogJpaEntity e WHERE e.messageText LIKE '%<name>%'")
    List<AvatarEventCatalogJpaEntity> getEventsWithNamePlaceholder();
}
