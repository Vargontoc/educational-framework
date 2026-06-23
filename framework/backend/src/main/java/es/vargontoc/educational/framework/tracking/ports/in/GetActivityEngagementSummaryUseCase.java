package es.vargontoc.educational.framework.tracking.ports.in;

import es.vargontoc.educational.framework.tracking.model.ActivityEngagementSummaryResult;

public interface GetActivityEngagementSummaryUseCase {

    ActivityEngagementSummaryResult getActivityEngagementSummary(Long childProfileId);
}
