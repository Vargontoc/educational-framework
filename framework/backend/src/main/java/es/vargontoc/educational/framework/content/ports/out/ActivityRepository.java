package es.vargontoc.educational.framework.content.ports.out;

import es.vargontoc.educational.framework.content.model.Activity;
import es.vargontoc.educational.framework.content.model.ContentStatus;

import java.util.List;
import java.util.Optional;

public interface ActivityRepository {

    Optional<Activity> findById(Long id);

    Optional<Activity> findByIdAndStatus(Long id, ContentStatus status);

    List<Activity> findAll();

    List<Activity> findByTopicId(Long topicId);

    Activity save(Activity activity);

    List<Activity> findByStatusAndTopicId(Long topicId, ContentStatus status, Integer targetAge);
}
