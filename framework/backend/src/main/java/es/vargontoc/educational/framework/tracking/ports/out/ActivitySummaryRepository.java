package es.vargontoc.educational.framework.tracking.ports.out;

import es.vargontoc.educational.framework.tracking.model.ActivitySummary;

import java.util.Optional;

public interface ActivitySummaryRepository {

    Optional<ActivitySummary> findByChildProfileIdAndActivityId(Long childProfileId, Long activityId);

    ActivitySummary save(ActivitySummary summary);
}
