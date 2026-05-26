package es.vargontoc.educational.framework.content.infrastructure.web;

import es.vargontoc.educational.framework.content.infrastructure.dto.CreateStoryPageRequest;
import es.vargontoc.educational.framework.content.infrastructure.dto.CreateStoryRequest;
import es.vargontoc.educational.framework.content.infrastructure.dto.StoryPageResponse;
import es.vargontoc.educational.framework.content.infrastructure.dto.StoryResponse;
import es.vargontoc.educational.framework.content.infrastructure.dto.UpdateStoryPageRequest;
import es.vargontoc.educational.framework.content.infrastructure.dto.UpdateStoryRequest;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.Story;
import es.vargontoc.educational.framework.content.model.StoryPage;
import es.vargontoc.educational.framework.content.ports.in.StoryPageUseCase;
import es.vargontoc.educational.framework.content.ports.in.StoryUseCase;
import es.vargontoc.educational.framework.shared.api.ApiResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Profile("dev")
@RequestMapping("/api/v1/dev/content/stories")
public class StoryController {

    private final StoryUseCase storyUseCase;
    private final StoryPageUseCase storyPageUseCase;

    public StoryController(StoryUseCase storyUseCase, StoryPageUseCase storyPageUseCase) {
        this.storyUseCase = storyUseCase;
        this.storyPageUseCase = storyPageUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StoryResponse>> createStory(@RequestBody CreateStoryRequest request) {
        var story = storyUseCase.createStory(
            request.title(),
            request.description(),
            request.minAge(),
            request.maxAge(),
            request.estimatedDurationMinutes(),
            request.topicIds(),
            request.backgroundMusicResourceRef(),
            ContentStatus.valueOf(request.status())
        );
        return ResponseEntity.status(201).body(ApiResponse.created(toResponse(story)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StoryResponse>>> listStories() {
        var stories = storyUseCase.listStories();
        var responses = stories.stream()
            .map(StoryController::toResponse)
            .toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StoryResponse>> getStory(@PathVariable Long id) {
        var story = storyUseCase.getStory(id);
        return ResponseEntity.ok(ApiResponse.ok(toResponse(story)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StoryResponse>> updateStory(
        @PathVariable Long id,
        @RequestBody UpdateStoryRequest request
    ) {
        var story = storyUseCase.updateStory(
            id,
            request.title(),
            request.description(),
            request.minAge(),
            request.maxAge(),
            request.estimatedDurationMinutes(),
            request.topicIds(),
            request.backgroundMusicResourceRef(),
            ContentStatus.valueOf(request.status())
        );
        return ResponseEntity.ok(ApiResponse.ok(toResponse(story)));
    }

    @PostMapping("/{storyId}/pages")
    public ResponseEntity<ApiResponse<StoryPageResponse>> createPage(
        @PathVariable Long storyId,
        @RequestBody CreateStoryPageRequest request
    ) {
        var page = storyPageUseCase.createPage(
            storyId,
            request.pageOrder(),
            request.text(),
            request.imageResourceRef(),
            request.audioResourceRef(),
            ContentStatus.valueOf(request.status())
        );
        return ResponseEntity.status(201).body(ApiResponse.created(toPageResponse(page)));
    }

    @GetMapping("/{storyId}/pages")
    public ResponseEntity<ApiResponse<List<StoryPageResponse>>> listPages(@PathVariable Long storyId) {
        var pages = storyPageUseCase.listPagesByStory(storyId);
        var responses = pages.stream()
            .map(StoryController::toPageResponse)
            .toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/{storyId}/pages/{pageId}")
    public ResponseEntity<ApiResponse<StoryPageResponse>> getPage(
        @PathVariable Long storyId,
        @PathVariable Long pageId
    ) {
        var page = storyPageUseCase.getPage(pageId);
        return ResponseEntity.ok(ApiResponse.ok(toPageResponse(page)));
    }

    @PutMapping("/{storyId}/pages/{pageId}")
    public ResponseEntity<ApiResponse<StoryPageResponse>> updatePage(
        @PathVariable Long storyId,
        @PathVariable Long pageId,
        @RequestBody UpdateStoryPageRequest request
    ) {
        var page = storyPageUseCase.updatePage(
            pageId,
            storyId,
            request.pageOrder(),
            request.text(),
            request.imageResourceRef(),
            request.audioResourceRef(),
            ContentStatus.valueOf(request.status())
        );
        return ResponseEntity.ok(ApiResponse.ok(toPageResponse(page)));
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
