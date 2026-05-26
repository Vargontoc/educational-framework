package es.vargontoc.educational.framework.content.service;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.Story;
import es.vargontoc.educational.framework.content.ports.out.StoryRepository;
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
class StoryServiceTest {

    @Mock
    private StoryRepository storyRepository;

    private StoryService storyService;

    @BeforeEach
    void setUp() {
        storyService = new StoryService(storyRepository);
    }

    @Test
    void createStory_happyPath() {
        when(storyRepository.save(any(Story.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = storyService.createStory("The Little Star", "A story about a star", 3, 6, 5, List.of(1L), "music.mp3", ContentStatus.ACTIVE);

        assertEquals("The Little Star", result.getTitle());
        assertEquals(3, result.getMinAge());
        assertEquals(6, result.getMaxAge());
        assertEquals(5, result.getEstimatedDurationMinutes());
        assertEquals(List.of(1L), result.getTopicIds());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void createStory_blankTitle_throwsValidation() {
        assertThrows(ValidationException.class, () ->
            storyService.createStory(" ", "Desc", 3, 6, 5, null, null, ContentStatus.ACTIVE));
    }

    @Test
    void getStory_notFound_throwsResourceNotFound() {
        when(storyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> storyService.getStory(99L));
    }

    @Test
    void listStories_returnsAll() {
        when(storyRepository.findAll()).thenReturn(List.of(new Story(), new Story()));

        var result = storyService.listStories();

        assertEquals(2, result.size());
    }

    @Test
    void listStoriesByStatus_returnsFiltered() {
        when(storyRepository.findByStatus(ContentStatus.ACTIVE)).thenReturn(List.of(new Story()));

        var result = storyService.listStoriesByStatus(ContentStatus.ACTIVE);

        assertEquals(1, result.size());
    }

    @Test
    void updateStory_happyPath() {
        var existing = new Story();
        existing.setId(1L);
        existing.setTitle("Old Title");

        when(storyRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(storyRepository.save(any(Story.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = storyService.updateStory(1L, "New Title", "New Desc", 5, 8, 10, List.of(2L), "new.mp3", ContentStatus.DRAFT);

        assertEquals("New Title", result.getTitle());
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    void updateStory_notFound_throwsResourceNotFound() {
        when(storyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            storyService.updateStory(99L, "Title", "Desc", 3, 6, 5, null, null, ContentStatus.ACTIVE));
    }
}
