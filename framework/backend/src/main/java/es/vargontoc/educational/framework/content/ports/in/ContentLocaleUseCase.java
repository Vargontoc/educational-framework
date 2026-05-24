package es.vargontoc.educational.framework.content.ports.in;

import es.vargontoc.educational.framework.content.model.ContentLocale;
import es.vargontoc.educational.framework.content.model.EntityType;

import java.util.List;

public interface ContentLocaleUseCase {

    ContentLocale createLocale(EntityType entityType, Long entityId, String localeCode, String name, String description);

    List<ContentLocale> listByEntity(EntityType entityType, Long entityId);

    ContentLocale updateLocale(Long id, String name, String description);
}
