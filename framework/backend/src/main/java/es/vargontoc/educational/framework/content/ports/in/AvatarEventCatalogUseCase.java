package es.vargontoc.educational.framework.content.ports.in;

import es.vargontoc.educational.framework.content.model.AvatarEventCatalog;
import es.vargontoc.educational.framework.content.model.AvatarEventType;
import es.vargontoc.educational.framework.content.model.AvatarTone;
import es.vargontoc.educational.framework.content.model.ContentStatus;

import java.util.List;

public interface AvatarEventCatalogUseCase {

    AvatarEventCatalog createAvatarEvent(AvatarEventType eventType, AvatarTone tone, String locale, String messageText, ContentStatus status);

    AvatarEventCatalog getAvatarEvent(Long id);

    List<AvatarEventCatalog> listAvatarEvents();

    List<AvatarEventCatalog> listAvatarEventsByEventType(AvatarEventType eventType);

    List<AvatarEventCatalog> listActiveAvatarEventsByFilters(AvatarEventType eventType, AvatarTone tone, String locale);

    AvatarEventCatalog updateAvatarEvent(Long id, AvatarEventType eventType, AvatarTone tone, String locale, String messageText, ContentStatus status);
}
