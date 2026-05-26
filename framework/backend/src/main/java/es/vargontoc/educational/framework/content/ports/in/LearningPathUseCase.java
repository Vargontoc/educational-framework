package es.vargontoc.educational.framework.content.ports.in;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.LearningPath;

import java.util.List;

public interface LearningPathUseCase {

    LearningPath createLearningPath(String name, String description, Integer minAge, Integer maxAge, String locale, ContentStatus status);

    LearningPath getLearningPath(Long id);

    List<LearningPath> listLearningPaths();

    List<LearningPath> listLearningPathsByStatus(ContentStatus status);

    LearningPath updateLearningPath(Long id, String name, String description, Integer minAge, Integer maxAge, String locale, ContentStatus status);
}
