package es.vargontoc.educational.framework.tracking.model;

import java.util.List;

public record ActivityEngagementSummaryResult(
        Long childProfileId,
        List<ActivityEngagementSummary> items
) {
}