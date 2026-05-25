package es.vargontoc.educational.framework.content.service;

import es.vargontoc.educational.framework.content.model.AvatarEventCatalog;
import es.vargontoc.educational.framework.content.model.AvatarEventType;
import es.vargontoc.educational.framework.content.model.AvatarTone;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.ports.out.AvatarEventCatalogRepository;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvatarEventCatalogServiceTest {

    @Mock
    private AvatarEventCatalogRepository avatarEventCatalogRepository;

    private AvatarEventCatalogService avatarEventCatalogService;

    @BeforeEach
    void setUp() {
        avatarEventCatalogService = new AvatarEventCatalogService(avatarEventCatalogRepository);
    }

    @Test
    void createAvatarEvent_happyPath() {
        when(avatarEventCatalogRepository.save(any(AvatarEventCatalog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = avatarEventCatalogService.createAvatarEvent(
            AvatarEventType.ACTIVITY_COMPLETED, AvatarTone.JOYFUL, "es-ES", "Has completado la actividad!", ContentStatus.ACTIVE);

        assertEquals(AvatarEventType.ACTIVITY_COMPLETED, result.getEventType());
        assertEquals(AvatarTone.JOYFUL, result.getTone());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void createAvatarEvent_nullEventType_throwsValidation() {
        assertThrows(ValidationException.class, () ->
            avatarEventCatalogService.createAvatarEvent(null, AvatarTone.JOYFUL, "es-ES", "Some message", ContentStatus.ACTIVE));
    }

    @Test
    void createAvatarEvent_blankMessage_throwsValidation() {
        assertThrows(ValidationException.class, () ->
            avatarEventCatalogService.createAvatarEvent(AvatarEventType.ACTIVITY_COMPLETED, AvatarTone.JOYFUL, "es-ES", " ", ContentStatus.ACTIVE));
    }

    @Test
    void getAvatarEvent_notFound_throwsResourceNotFound() {
        when(avatarEventCatalogRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> avatarEventCatalogService.getAvatarEvent(99L));
    }

    @Test
    void listAvatarEventsByEventType_returnsFiltered() {
        when(avatarEventCatalogRepository.findByEventType(AvatarEventType.ACTIVITY_COMPLETED))
            .thenReturn(List.of(new AvatarEventCatalog(), new AvatarEventCatalog()));

        var result = avatarEventCatalogService.listAvatarEventsByEventType(AvatarEventType.ACTIVITY_COMPLETED);

        assertEquals(2, result.size());
    }

    @Test
    void listActiveAvatarEventsByFilters_returnsFiltered() {
        when(avatarEventCatalogRepository.findActiveByFilters(AvatarEventType.ACTIVITY_COMPLETED, AvatarTone.JOYFUL, "es-ES"))
            .thenReturn(List.of(new AvatarEventCatalog()));

        var result = avatarEventCatalogService.listActiveAvatarEventsByFilters(AvatarEventType.ACTIVITY_COMPLETED, AvatarTone.JOYFUL, "es-ES");

        assertEquals(1, result.size());
    }
}
