package es.vargontoc.educational.framework.content.ports.in;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.Story;

import java.util.List;

public interface StoryUseCase {

    Story createStory(String title, String description, Integer minAge, Integer maxAge, Integer estimatedDurationMinutes, List<Long> topicIds, String backgroundMusicResourceRef, ContentStatus status);

    Story getStory(Long id);

    List<Story> listStories();

    List<Story> listStoriesByStatus(ContentStatus status);

    Story updateStory(Long id, String title, String description, Integer minAge, Integer maxAge, Integer estimatedDurationMinutes, List<Long> topicIds, String backgroundMusicResourceRef, ContentStatus status);
}
