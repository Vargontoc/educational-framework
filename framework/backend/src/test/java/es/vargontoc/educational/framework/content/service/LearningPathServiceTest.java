package es.vargontoc.educational.framework.content.service;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.LearningPath;
import es.vargontoc.educational.framework.content.ports.out.LearningPathRepository;
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
class LearningPathServiceTest {

    @Mock
    private LearningPathRepository learningPathRepository;

    private LearningPathService learningPathService;

    @BeforeEach
    void setUp() {
        learningPathService = new LearningPathService(learningPathRepository);
    }

    @Test
    void createLearningPath_happyPath() {
        when(learningPathRepository.save(any(LearningPath.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = learningPathService.createLearningPath("Math Basics", "Learn basic math", 3, 6, "es-ES", ContentStatus.ACTIVE);

        assertEquals("Math Basics", result.getName());
        assertEquals("Learn basic math", result.getDescription());
        assertEquals(3, result.getMinAge());
        assertEquals(6, result.getMaxAge());
        assertEquals("es-ES", result.getLocale());
        assertEquals(ContentStatus.ACTIVE, result.getStatus());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void createLearningPath_blankName_throwsValidation() {
        assertThrows(ValidationException.class, () ->
            learningPathService.createLearningPath(" ", "Description", 3, 6, "es-ES", ContentStatus.ACTIVE));
    }

    @Test
    void getLearningPath_notFound_throwsResourceNotFound() {
        when(learningPathRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> learningPathService.getLearningPath(99L));
    }

    @Test
    void listLearningPaths_returnsAll() {
        when(learningPathRepository.findAll()).thenReturn(List.of(new LearningPath(), new LearningPath()));

        var result = learningPathService.listLearningPaths();

        assertEquals(2, result.size());
    }

    @Test
    void listLearningPathsByStatus_returnsFiltered() {
        when(learningPathRepository.findByStatus(ContentStatus.ACTIVE)).thenReturn(List.of(new LearningPath()));

        var result = learningPathService.listLearningPathsByStatus(ContentStatus.ACTIVE);

        assertEquals(1, result.size());
    }

    @Test
    void updateLearningPath_happyPath() {
        var existing = new LearningPath();
        existing.setId(1L);
        existing.setName("Old Name");

        when(learningPathRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(learningPathRepository.save(any(LearningPath.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = learningPathService.updateLearningPath(1L, "New Name", "New Description", 5, 8, "en-US", ContentStatus.DRAFT);

        assertEquals("New Name", result.getName());
        assertEquals("New Description", result.getDescription());
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    void updateLearningPath_notFound_throwsResourceNotFound() {
        when(learningPathRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            learningPathService.updateLearningPath(99L, "Name", "Desc", 3, 6, "es-ES", ContentStatus.ACTIVE));
    }
}
