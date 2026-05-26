package es.vargontoc.educational.framework.content.service;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.Story;
import es.vargontoc.educational.framework.content.ports.in.StoryUseCase;
import es.vargontoc.educational.framework.content.ports.out.StoryRepository;
import es.vargontoc.educational.framework.content.validation.StoryValidator;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Transactional
public class StoryService implements StoryUseCase {

    private final StoryRepository storyRepository;
    private final StoryValidator storyValidator;

    public StoryService(StoryRepository storyRepository) {
        this.storyRepository = storyRepository;
        this.storyValidator = new StoryValidator();
    }

    @Override
    public Story createStory(String title, String description, Integer minAge, Integer maxAge, Integer estimatedDurationMinutes, List<Long> topicIds, String backgroundMusicResourceRef, ContentStatus status) {
        storyValidator.validateForCreate(title, minAge, maxAge, estimatedDurationMinutes, status);

        var story = new Story();
        story.setTitle(title);
        story.setDescription(description);
        story.setMinAge(minAge);
        story.setMaxAge(maxAge);
        story.setEstimatedDurationMinutes(estimatedDurationMinutes);
        story.setTopicIds(topicIds != null ? topicIds : List.of());
        story.setBackgroundMusicResourceRef(backgroundMusicResourceRef);
        story.setStatus(status);
        story.setCreatedAt(LocalDateTime.now());

        return storyRepository.save(story);
    }

    @Override
    @Transactional(readOnly = true)
    public Story getStory(Long id) {
        return storyRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Story not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Story> listStories() {
        return storyRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Story> listStoriesByStatus(ContentStatus status) {
        return storyRepository.findByStatus(status);
    }

    @Override
    public Story updateStory(Long id, String title, String description, Integer minAge, Integer maxAge, Integer estimatedDurationMinutes, List<Long> topicIds, String backgroundMusicResourceRef, ContentStatus status) {
        storyValidator.validateForUpdate(title, minAge, maxAge, estimatedDurationMinutes, status);

        var existing = storyRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Story not found with id: " + id));

        existing.setTitle(title);
        existing.setDescription(description);
        existing.setMinAge(minAge);
        existing.setMaxAge(maxAge);
        existing.setEstimatedDurationMinutes(estimatedDurationMinutes);
        existing.setTopicIds(topicIds != null ? topicIds : List.of());
        existing.setBackgroundMusicResourceRef(backgroundMusicResourceRef);
        existing.setStatus(status);
        existing.setUpdatedAt(LocalDateTime.now());

        return storyRepository.save(existing);
    }
}
