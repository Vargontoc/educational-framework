package es.vargontoc.educational.framework.content.infrastructure.persistence;

import es.vargontoc.educational.framework.content.model.AvatarEventCatalog;
import es.vargontoc.educational.framework.content.model.AvatarEventType;
import es.vargontoc.educational.framework.content.model.AvatarTone;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvatarEventCatalogPersistenceAdapterTest {

    @Mock
    private AvatarEventCatalogJpaRepository jpaRepository;

    private AvatarEventCatalogPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new AvatarEventCatalogPersistenceAdapter(jpaRepository);
    }

    @Test
    void save_mapsDomainToJpa_correctly() {
        var event = buildDomain();
        when(jpaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = adapter.save(event);

        assertEquals(AvatarEventType.ACTIVITY_COMPLETED, result.getEventType());
        assertEquals(AvatarTone.JOYFUL, result.getTone());
        assertEquals("es-ES", result.getLocale());
        assertEquals("Has completado la actividad!", result.getMessageText());
        assertEquals(ContentStatus.ACTIVE, result.getStatus());
    }

    @Test
    void findById_mapsJpaToDomain_correctly() {
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(buildJpaEntity()));

        var result = adapter.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(AvatarEventType.ACTIVITY_COMPLETED, result.get().getEventType());
        assertEquals(AvatarTone.JOYFUL, result.get().getTone());
        assertEquals("es-ES", result.get().getLocale());
        assertEquals("Has completado la actividad!", result.get().getMessageText());
        assertEquals(ContentStatus.ACTIVE, result.get().getStatus());
    }

    private AvatarEventCatalog buildDomain() {
        var event = new AvatarEventCatalog();
        event.setEventType(AvatarEventType.ACTIVITY_COMPLETED);
        event.setTone(AvatarTone.JOYFUL);
        event.setLocale("es-ES");
        event.setMessageText("Has completado la actividad!");
        event.setStatus(ContentStatus.ACTIVE);
        event.setCreatedAt(LocalDateTime.now());
        return event;
    }

    private AvatarEventCatalogJpaEntity buildJpaEntity() {
        var entity = new AvatarEventCatalogJpaEntity();
        entity.setId(1L);
        entity.setEventType("ACTIVITY_COMPLETED");
        entity.setTone("JOYFUL");
        entity.setLocale("es-ES");
        entity.setMessageText("Has completado la actividad!");
        entity.setStatus("ACTIVE");
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }
}
