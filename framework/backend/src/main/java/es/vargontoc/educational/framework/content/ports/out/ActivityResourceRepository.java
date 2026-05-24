package es.vargontoc.educational.framework.content.ports.out;

import es.vargontoc.educational.framework.content.model.ActivityResource;

import java.util.List;
import java.util.Optional;

public interface ActivityResourceRepository {

    Optional<ActivityResource> findById(Long id);

    List<ActivityResource> findByActivityId(Long activityId);

    ActivityResource save(ActivityResource resource);
}
