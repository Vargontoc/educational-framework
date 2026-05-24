package es.vargontoc.educational.framework.content.service;

import es.vargontoc.educational.framework.content.model.ContentLocale;
import es.vargontoc.educational.framework.content.model.EntityType;
import es.vargontoc.educational.framework.content.ports.out.ContentLocaleRepository;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContentLocaleServiceTest {

    @Mock
    private ContentLocaleRepository contentLocaleRepository;

    private ContentLocaleService contentLocaleService;

    @BeforeEach
    void setUp() {
        contentLocaleService = new ContentLocaleService(contentLocaleRepository);
    }

    @Test
    void createLocale_happyPath() {
        when(contentLocaleRepository.save(any(ContentLocale.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = contentLocaleService.createLocale(EntityType.CATEGORY, 1L, "es-ES", "Matemáticas", "Conceptos matemáticos básicos");

        assertEquals(EntityType.CATEGORY, result.getEntityType());
        assertEquals(1L, result.getEntityId());
        assertEquals("es-ES", result.getLocaleCode());
        assertEquals("Matemáticas", result.getName());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void listByEntity_returnsFiltered() {
        when(contentLocaleRepository.findByEntityTypeAndEntityId(EntityType.CATEGORY, 1L))
            .thenReturn(List.of(new ContentLocale(), new ContentLocale()));

        var result = contentLocaleService.listByEntity(EntityType.CATEGORY, 1L);

        assertEquals(2, result.size());
    }

    @Test
    void updateLocale_happyPath() {
        var existing = new ContentLocale();
        existing.setId(1L);
        existing.setEntityType(EntityType.CATEGORY);
        existing.setEntityId(1L);
        existing.setLocaleCode("es-ES");
        existing.setName("Old Name");

        when(contentLocaleRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(contentLocaleRepository.save(any(ContentLocale.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = contentLocaleService.updateLocale(1L, "New Name", "New Description");

        assertEquals("New Name", result.getName());
        assertEquals("New Description", result.getDescription());
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    void updateLocale_notFound_throwsResourceNotFound() {
        when(contentLocaleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            contentLocaleService.updateLocale(99L, "New Name", "New Description"));
    }
}
