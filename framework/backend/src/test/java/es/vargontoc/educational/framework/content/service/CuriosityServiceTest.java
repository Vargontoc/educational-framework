package es.vargontoc.educational.framework.content.service;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.Curiosity;
import es.vargontoc.educational.framework.content.model.Topic;
import es.vargontoc.educational.framework.content.ports.out.CuriosityRepository;
import es.vargontoc.educational.framework.content.ports.out.TopicRepository;
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
class CuriosityServiceTest {

    @Mock
    private CuriosityRepository curiosityRepository;

    @Mock
    private TopicRepository topicRepository;

    private CuriosityService curiosityService;

    @BeforeEach
    void setUp() {
        curiosityService = new CuriosityService(curiosityRepository, topicRepository);
    }

    @Test
    void createCuriosity_happyPath() {
        when(topicRepository.findById(1L)).thenReturn(Optional.of(new Topic()));
        when(curiosityRepository.save(any(Curiosity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = curiosityService.createCuriosity("Las mariposas prueban con sus patas", 1L, 3, 6, List.of("animales"), "es-ES", "mariposas (mah-ree-POH-sas)", ContentStatus.ACTIVE);

        assertEquals("Las mariposas prueban con sus patas", result.getText());
        assertEquals(1L, result.getTopicId());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void createCuriosity_withoutTopic_happyPath() {
        when(curiosityRepository.save(any(Curiosity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = curiosityService.createCuriosity("Some curiosity", null, 3, 6, null, "es-ES", null, ContentStatus.ACTIVE);

        assertEquals("Some curiosity", result.getText());
    }

    @Test
    void createCuriosity_topicNotFound_throwsResourceNotFound() {
        when(topicRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            curiosityService.createCuriosity("Some curiosity", 99L, 3, 6, null, "es-ES", null, ContentStatus.ACTIVE));
    }

    @Test
    void createCuriosity_blankText_throwsValidation() {
        assertThrows(ValidationException.class, () ->
            curiosityService.createCuriosity(" ", 1L, 3, 6, null, "es-ES", null, ContentStatus.ACTIVE));
    }

    @Test
    void getCuriosity_notFound_throwsResourceNotFound() {
        when(curiosityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> curiosityService.getCuriosity(99L));
    }

    @Test
    void listCuriositiesByTopic_returnsFiltered() {
        when(curiosityRepository.findByTopicId(1L)).thenReturn(List.of(new Curiosity(), new Curiosity()));

        var result = curiosityService.listCuriositiesByTopic(1L);

        assertEquals(2, result.size());
    }

    @Test
    void listActiveCuriositiesByFilters_returnsFiltered() {
        when(curiosityRepository.findActiveByFilters(1L, 5, "es-ES")).thenReturn(List.of(new Curiosity()));

        var result = curiosityService.listActiveCuriositiesByFilters(1L, 5, "es-ES");

        assertEquals(1, result.size());
    }
}
