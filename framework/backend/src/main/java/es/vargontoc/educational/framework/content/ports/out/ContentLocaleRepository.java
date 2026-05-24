package es.vargontoc.educational.framework.content.ports.out;

import es.vargontoc.educational.framework.content.model.ContentLocale;
import es.vargontoc.educational.framework.content.model.EntityType;

import java.util.List;
import java.util.Optional;

public interface ContentLocaleRepository {

    Optional<ContentLocale> findById(Long id);

    List<ContentLocale> findByEntityTypeAndEntityId(EntityType entityType, Long entityId);

    ContentLocale save(ContentLocale contentLocale);

    void deleteByEntityTypeAndEntityIdAndLocaleCode(EntityType entityType, Long entityId, String localeCode);
}
