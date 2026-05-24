package es.vargontoc.educational.framework.content.ports.out;

import es.vargontoc.educational.framework.content.model.Activity;

import java.util.List;
import java.util.Optional;

public interface ActivityRepository {

    Optional<Activity> findById(Long id);

    List<Activity> findAll();

    List<Activity> findByTopicId(Long topicId);

    Activity save(Activity activity);
}
