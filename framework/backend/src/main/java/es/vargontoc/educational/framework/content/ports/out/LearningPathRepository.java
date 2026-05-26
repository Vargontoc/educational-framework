package es.vargontoc.educational.framework.content.ports.out;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.LearningPath;

import java.util.List;
import java.util.Optional;

public interface LearningPathRepository {

    Optional<LearningPath> findById(Long id);

    List<LearningPath> findAll();

    List<LearningPath> findByStatus(ContentStatus status);

    LearningPath save(LearningPath learningPath);
}
