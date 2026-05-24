package es.vargontoc.educational.framework.content.infrastructure.web;

import es.vargontoc.educational.framework.content.infrastructure.dto.CreateTopicRequest;
import es.vargontoc.educational.framework.content.infrastructure.dto.TopicResponse;
import es.vargontoc.educational.framework.content.infrastructure.dto.UpdateTopicRequest;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.Topic;
import es.vargontoc.educational.framework.content.ports.in.TopicUseCase;
import es.vargontoc.educational.framework.shared.api.ApiResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Profile("dev")
@RequestMapping("/api/v1/dev/content/topics")
public class TopicController {

    private final TopicUseCase topicUseCase;

    public TopicController(TopicUseCase topicUseCase) {
        this.topicUseCase = topicUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TopicResponse>> createTopic(@RequestBody CreateTopicRequest request) {
        var topic = topicUseCase.createTopic(
            request.name(),
            request.description(),
            request.categoryId(),
            ContentStatus.valueOf(request.status()),
            request.minAge(),
            request.maxAge(),
            request.compatibleVariants()
        );
        return ResponseEntity.status(201).body(ApiResponse.created(toResponse(topic)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TopicResponse>>> listTopics(
        @RequestParam(required = false) Long categoryId
    ) {
        List<Topic> topics = categoryId != null
            ? topicUseCase.listTopicsByCategory(categoryId)
            : topicUseCase.listTopics();

        var responses = topics.stream()
            .map(TopicController::toResponse)
            .toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TopicResponse>> getTopic(@PathVariable Long id) {
        var topic = topicUseCase.getTopic(id);
        return ResponseEntity.ok(ApiResponse.ok(toResponse(topic)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TopicResponse>> updateTopic(
        @PathVariable Long id,
        @RequestBody UpdateTopicRequest request
    ) {
        var topic = topicUseCase.updateTopic(
            id,
            request.name(),
            request.description(),
            request.categoryId(),
            ContentStatus.valueOf(request.status()),
            request.minAge(),
            request.maxAge(),
            request.compatibleVariants()
        );
        return ResponseEntity.ok(ApiResponse.ok(toResponse(topic)));
    }

    private static TopicResponse toResponse(Topic topic) {
        return new TopicResponse(
            topic.getId(),
            topic.getName(),
            topic.getDescription(),
            topic.getCategoryId(),
            topic.getStatus().name(),
            topic.getMinAge(),
            topic.getMaxAge(),
            topic.getCompatibleVariants(),
            topic.getCreatedAt(),
            topic.getUpdatedAt()
        );
    }
}
