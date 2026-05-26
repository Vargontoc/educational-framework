package es.vargontoc.educational.framework.content.service;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.Story;
import es.vargontoc.educational.framework.content.model.StoryPage;
import es.vargontoc.educational.framework.content.ports.out.StoryPageRepository;
import es.vargontoc.educational.framework.content.ports.out.StoryRepository;
import es.vargontoc.educational.framework.shared.exception.ConflictException;
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
class StoryPageServiceTest {

    @Mock
    private StoryPageRepository storyPageRepository;

    @Mock
    private StoryRepository storyRepository;

    private StoryPageService storyPageService;

    @BeforeEach
    void setUp() {
        storyPageService = new StoryPageService(storyPageRepository, storyRepository);
    }

    @Test
    void createPage_happyPath() {
        when(storyRepository.findById(1L)).thenReturn(Optional.of(new Story()));
        when(storyPageRepository.existsByStoryIdAndPageOrder(1L, 1)).thenReturn(false);
        when(storyPageRepository.save(any(StoryPage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = storyPageService.createPage(1L, 1, "Once upon a time...", "img.png", null, ContentStatus.ACTIVE);

        assertEquals(1L, result.getStoryId());
        assertEquals(1, result.getPageOrder());
        assertEquals("Once upon a time...", result.getText());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void createPage_storyNotFound_throwsResourceNotFound() {
        when(storyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            storyPageService.createPage(99L, 1, "Text", null, null, ContentStatus.ACTIVE));
    }

    @Test
    void createPage_duplicateOrder_throwsConflict() {
        when(storyRepository.findById(1L)).thenReturn(Optional.of(new Story()));
        when(storyPageRepository.existsByStoryIdAndPageOrder(1L, 1)).thenReturn(true);

        assertThrows(ConflictException.class, () ->
            storyPageService.createPage(1L, 1, "Text", null, null, ContentStatus.ACTIVE));
    }

    @Test
    void createPage_nullStoryId_throwsValidation() {
        assertThrows(ValidationException.class, () ->
            storyPageService.createPage(null, 1, "Text", null, null, ContentStatus.ACTIVE));
    }

    @Test
    void createPage_pageOrderZero_throwsValidation() {
        assertThrows(ValidationException.class, () ->
            storyPageService.createPage(1L, 0, "Text", null, null, ContentStatus.ACTIVE));
    }

    @Test
    void getPage_notFound_throwsResourceNotFound() {
        when(storyPageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> storyPageService.getPage(99L));
    }

    @Test
    void listPagesByStory_returnsFiltered() {
        when(storyPageRepository.findByStoryId(1L)).thenReturn(List.of(new StoryPage(), new StoryPage()));

        var result = storyPageService.listPagesByStory(1L);

        assertEquals(2, result.size());
    }

    @Test
    void listActivePagesByStory_returnsOnlyActive() {
        when(storyPageRepository.findByStoryIdAndStatus(1L, ContentStatus.ACTIVE)).thenReturn(List.of(new StoryPage()));

        var result = storyPageService.listActivePagesByStory(1L);

        assertEquals(1, result.size());
    }

    @Test
    void updatePage_happyPath() {
        var existing = new StoryPage();
        existing.setStoryId(1L);
        existing.setPageOrder(1);

        when(storyPageRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(storyRepository.findById(1L)).thenReturn(Optional.of(new Story()));
        when(storyPageRepository.save(any(StoryPage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = storyPageService.updatePage(1L, 1L, 1, "Updated text", null, null, ContentStatus.ACTIVE);

        assertEquals("Updated text", result.getText());
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    void updatePage_storyNotFound_throwsResourceNotFound() {
        var existing = new StoryPage();
        existing.setStoryId(1L);
        existing.setPageOrder(1);

        when(storyPageRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(storyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            storyPageService.updatePage(1L, 99L, 1, "Text", null, null, ContentStatus.ACTIVE));
    }

    @Test
    void updatePage_conflictOnDifferentStory_throwsConflict() {
        var existing = new StoryPage();
        existing.setStoryId(1L);
        existing.setPageOrder(1);

        when(storyPageRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(storyRepository.findById(2L)).thenReturn(Optional.of(new Story()));
        when(storyPageRepository.existsByStoryIdAndPageOrder(2L, 1)).thenReturn(true);

        assertThrows(ConflictException.class, () ->
            storyPageService.updatePage(1L, 2L, 1, "Text", null, null, ContentStatus.ACTIVE));
    }

    @Test
    void updatePage_samePositionNoConflict() {
        var existing = new StoryPage();
        existing.setStoryId(1L);
        existing.setPageOrder(1);

        when(storyPageRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(storyRepository.findById(1L)).thenReturn(Optional.of(new Story()));
        when(storyPageRepository.save(any(StoryPage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = storyPageService.updatePage(1L, 1L, 1, "Same position", null, null, ContentStatus.ACTIVE);

        assertEquals("Same position", result.getText());
    }
}
