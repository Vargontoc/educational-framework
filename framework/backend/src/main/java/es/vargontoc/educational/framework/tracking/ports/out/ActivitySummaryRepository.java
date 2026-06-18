package es.vargontoc.educational.framework.tracking.ports.out;

import es.vargontoc.educational.framework.tracking.model.ActivitySummary;

import java.util.List;
import java.util.Optional;

public interface ActivitySummaryRepository {

    Optional<ActivitySummary> findByChildProfileIdAndActivityId(Long childProfileId, Long activityId);

    List<ActivitySummary> findByChildProfileId(Long childProfileId);

    ActivitySummary save(ActivitySummary summary);
}
