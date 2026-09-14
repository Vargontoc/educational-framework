package es.vargontoc.educational.framework.content.ports.out;

import java.util.List;
import java.util.Optional;

import es.vargontoc.educational.framework.audio.domain.enums.TonePreset;
import es.vargontoc.educational.framework.avatar.domain.enums.AvatarEventType;
import es.vargontoc.educational.framework.content.model.AvatarEventCatalog;

public interface AvatarEventCatalogRepository {

    Optional<AvatarEventCatalog> findById(Long id);

    List<AvatarEventCatalog> findAll();

    List<AvatarEventCatalog> findEventsWithNamePlaceholder();

    List<AvatarEventCatalog> findByEventType(AvatarEventType eventType);

    List<AvatarEventCatalog> findActiveByEventTypeAndBiome(AvatarEventType eventType, String biome);

    List<AvatarEventCatalog> findActiveByFilters(AvatarEventType eventType, TonePreset tone, String locale);

    AvatarEventCatalog save(AvatarEventCatalog event);
}
