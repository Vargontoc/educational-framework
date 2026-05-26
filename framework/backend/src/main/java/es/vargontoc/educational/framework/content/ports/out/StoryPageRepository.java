package es.vargontoc.educational.framework.content.ports.out;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.StoryPage;

import java.util.List;
import java.util.Optional;

public interface StoryPageRepository {

    Optional<StoryPage> findById(Long id);

    List<StoryPage> findByStoryId(Long storyId);

    List<StoryPage> findByStoryIdAndStatus(Long storyId, ContentStatus status);

    boolean existsByStoryIdAndPageOrder(Long storyId, Integer pageOrder);

    StoryPage save(StoryPage storyPage);
}
