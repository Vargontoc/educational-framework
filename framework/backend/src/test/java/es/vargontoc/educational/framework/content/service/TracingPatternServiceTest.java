package es.vargontoc.educational.framework.content.service;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.Topic;
import es.vargontoc.educational.framework.content.model.TracingPattern;
import es.vargontoc.educational.framework.content.ports.out.TopicRepository;
import es.vargontoc.educational.framework.content.ports.out.TracingPatternRepository;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TracingPatternServiceTest {

    @Mock
    private TracingPatternRepository tracingPatternRepository;

    @Mock
    private TopicRepository topicRepository;

    private TracingPatternService tracingPatternService;

    @BeforeEach
    void setUp() {
        tracingPatternService = new TracingPatternService(tracingPatternRepository, topicRepository);
    }

    @Test
    void createTracingPattern_happyPath() {
        var points = List.of(List.of(0.0, 0.0), List.of(0.5, 0.5), List.of(1.0, 1.0));
        when(topicRepository.findById(1L)).thenReturn(Optional.of(new Topic()));
        when(tracingPatternRepository.save(any(TracingPattern.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = tracingPatternService.createTracingPattern(1L, "Square", "A square pattern", points, ContentStatus.ACTIVE);

        assertEquals(1L, result.getTopicId());
        assertEquals("Square", result.getName());
        assertEquals(3, result.getPoints().size());
        assertEquals(ContentStatus.ACTIVE, result.getStatus());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void createTracingPattern_topicNotFound_throwsResourceNotFound() {
        var points = List.of(List.of(0.0, 0.0), List.of(1.0, 1.0));
        when(topicRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            tracingPatternService.createTracingPattern(99L, "Pattern", null, points, ContentStatus.ACTIVE));
    }

    @Test
    void createTracingPattern_emptyPoints_throwsValidation() {
        assertThrows(ValidationException.class, () ->
            tracingPatternService.createTracingPattern(1L, "Pattern", null, Collections.emptyList(), ContentStatus.ACTIVE));
    }

    @Test
    void createTracingPattern_blankName_throwsValidation() {
        var points = List.of(List.of(0.0, 0.0), List.of(1.0, 1.0));
        assertThrows(ValidationException.class, () ->
            tracingPatternService.createTracingPattern(1L, " ", null, points, ContentStatus.ACTIVE));
    }

    @Test
    void getTracingPattern_notFound_throwsResourceNotFound() {
        when(tracingPatternRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tracingPatternService.getTracingPattern(99L));
    }

    @Test
    void listTracingPatterns_returnsAll() {
        when(tracingPatternRepository.findAll()).thenReturn(List.of(new TracingPattern(), new TracingPattern()));

        var result = tracingPatternService.listTracingPatterns();

        assertEquals(2, result.size());
    }

    @Test
    void listTracingPatternsByTopic_returnsFiltered() {
        when(tracingPatternRepository.findByTopicId(1L)).thenReturn(List.of(new TracingPattern()));

        var result = tracingPatternService.listTracingPatternsByTopic(1L);

        assertEquals(1, result.size());
    }

    @Test
    void updateTracingPattern_happyPath() {
        var existing = new TracingPattern();
        existing.setId(1L);
        existing.setName("Old Name");

        var points = List.of(List.of(0.0, 0.0), List.of(1.0, 1.0));
        when(tracingPatternRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(topicRepository.findById(1L)).thenReturn(Optional.of(new Topic()));
        when(tracingPatternRepository.save(any(TracingPattern.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = tracingPatternService.updateTracingPattern(1L, 1L, "New Name", "New Desc", points, ContentStatus.DRAFT);

        assertEquals("New Name", result.getName());
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    void updateTracingPattern_notFound_throwsResourceNotFound() {
        var points = List.of(List.of(0.0, 0.0), List.of(1.0, 1.0));
        when(tracingPatternRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            tracingPatternService.updateTracingPattern(99L, 1L, "Name", null, points, ContentStatus.ACTIVE));
    }
}
