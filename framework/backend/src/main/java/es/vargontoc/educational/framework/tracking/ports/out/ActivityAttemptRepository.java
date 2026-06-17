package es.vargontoc.educational.framework.tracking.ports.out;

import es.vargontoc.educational.framework.tracking.model.ActivityAttempt;

import java.util.List;

public interface ActivityAttemptRepository {

    ActivityAttempt save(ActivityAttempt attempt);

    List<ActivityAttempt> findRecentByChildAndActivity(Long childProfileId, Long activityId, int limit);
}
