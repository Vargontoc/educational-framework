package es.vargontoc.educational.framework.content.infrastructure.web;

import es.vargontoc.educational.framework.content.infrastructure.dto.StoryDetailResponse;
import es.vargontoc.educational.framework.content.infrastructure.dto.StoryPageResponse;
import es.vargontoc.educational.framework.content.infrastructure.dto.StoryResponse;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.Story;
import es.vargontoc.educational.framework.content.model.StoryPage;
import es.vargontoc.educational.framework.content.ports.in.StoryPageUseCase;
import es.vargontoc.educational.framework.content.ports.in.StoryUseCase;
import es.vargontoc.educational.framework.shared.api.ApiResponse;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/content/stories")
public class ProductiveStoryController {

    private final StoryUseCase storyUseCase;
    private final StoryPageUseCase storyPageUseCase;

    public ProductiveStoryController(StoryUseCase storyUseCase, StoryPageUseCase storyPageUseCase) {
        this.storyUseCase = storyUseCase;
        this.storyPageUseCase = storyPageUseCase;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StoryResponse>>> listActiveStories() {
        var stories = storyUseCase.listStoriesByStatus(ContentStatus.ACTIVE);
        var responses = stories.stream()
            .map(ProductiveStoryController::toResponse)
            .toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StoryDetailResponse>> getStoryDetail(@PathVariable Long id) {
        var story = storyUseCase.getStory(id);
        if (story.getStatus() != ContentStatus.ACTIVE) {
            throw new ResourceNotFoundException("Story not found with id: " + id);
        }
        var activePages = storyPageUseCase.listActivePagesByStory(id);

        var pageResponses = activePages.stream()
            .map(ProductiveStoryController::toPageResponse)
            .toList();

        var detailResponse = new StoryDetailResponse(
            story.getId(),
            story.getTitle(),
            story.getDescription(),
            story.getMinAge(),
            story.getMaxAge(),
            story.getEstimatedDurationMinutes(),
            story.getTopicIds(),
            story.getBackgroundMusicResourceRef(),
            story.getStatus().name(),
            pageResponses
        );

        return ResponseEntity.ok(ApiResponse.ok(detailResponse));
    }

    private static StoryResponse toResponse(Story story) {
        return new StoryResponse(
            story.getId(),
            story.getTitle(),
            story.getDescription(),
            story.getMinAge(),
            story.getMaxAge(),
            story.getEstimatedDurationMinutes(),
            story.getTopicIds(),
            story.getBackgroundMusicResourceRef(),
            story.getStatus().name(),
            story.getCreatedAt(),
            story.getUpdatedAt()
        );
    }

    private static StoryPageResponse toPageResponse(StoryPage page) {
        return new StoryPageResponse(
            page.getId(),
            page.getStoryId(),
            page.getPageOrder(),
            page.getText(),
            page.getImageResourceRef(),
            page.getAudioResourceRef(),
            page.getStatus().name(),
            page.getCreatedAt(),
            page.getUpdatedAt()
        );
    }
}
