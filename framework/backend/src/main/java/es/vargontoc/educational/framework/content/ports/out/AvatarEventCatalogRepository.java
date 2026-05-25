package es.vargontoc.educational.framework.content.ports.out;

import es.vargontoc.educational.framework.content.model.AvatarEventCatalog;
import es.vargontoc.educational.framework.content.model.AvatarEventType;
import es.vargontoc.educational.framework.content.model.AvatarTone;

import java.util.List;
import java.util.Optional;

public interface AvatarEventCatalogRepository {

    Optional<AvatarEventCatalog> findById(Long id);

    List<AvatarEventCatalog> findAll();

    List<AvatarEventCatalog> findByEventType(AvatarEventType eventType);

    List<AvatarEventCatalog> findActiveByFilters(AvatarEventType eventType, AvatarTone tone, String locale);

    AvatarEventCatalog save(AvatarEventCatalog event);
}
