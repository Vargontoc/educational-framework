package es.vargontoc.educational.framework.content.ports.out;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.Story;

import java.util.List;
import java.util.Optional;

public interface StoryRepository {

    Optional<Story> findById(Long id);

    List<Story> findAll();

    List<Story> findByStatus(ContentStatus status);

    Story save(Story story);
}
