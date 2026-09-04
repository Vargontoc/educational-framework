package es.vargontoc.educational.framework.tracking.service;

import es.vargontoc.educational.framework.tracking.model.ActivityEngagementSummary;
import es.vargontoc.educational.framework.tracking.model.ActivityEngagementSummaryResult;
import es.vargontoc.educational.framework.tracking.model.ActivityProposalLog;
import es.vargontoc.educational.framework.tracking.model.ActivityProposalOutcome;
import es.vargontoc.educational.framework.tracking.model.GameSessionSummary;
import es.vargontoc.educational.framework.tracking.model.GameSessionFinalStatus;
import es.vargontoc.educational.framework.tracking.ports.in.GetActivityEngagementSummaryUseCase;
import es.vargontoc.educational.framework.tracking.ports.out.ActivityInformationPort;
import es.vargontoc.educational.framework.tracking.ports.out.ActivityProposalLogRepository;
import es.vargontoc.educational.framework.tracking.ports.out.GameSessionSummaryRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
public class ActivityEngagementSummaryService implements GetActivityEngagementSummaryUseCase {

    private final ActivityProposalLogRepository proposalLogRepository;
    private final GameSessionSummaryRepository sessionSummaryRepository;
    private final ActivityInformationPort activityInformationPort;

    public ActivityEngagementSummaryService(
            ActivityProposalLogRepository proposalLogRepository,
            GameSessionSummaryRepository sessionSummaryRepository,
            ActivityInformationPort activityInformationPort) {
        this.proposalLogRepository = proposalLogRepository;
        this.sessionSummaryRepository = sessionSummaryRepository;
        this.activityInformationPort = activityInformationPort;
    }

    @Override
    public ActivityEngagementSummaryResult getActivityEngagementSummary(Long childProfileId) {
        var proposals = proposalLogRepository.findByChildProfileId(childProfileId);
        var sessions = sessionSummaryRepository.findByChildProfileId(childProfileId);

        Set<Long> activityIds = collectActivityIds(proposals, sessions);

        Map<Long, String> engineTypesByActivity = activityInformationPort.getGameEngineTypeByActivityIds(activityIds);

        Map<String, ActivityEngagementSummaryBuilder> buildersByEngineType = new HashMap<>();

        for (var proposal : proposals) {
            String engineType = engineTypesByActivity.get(proposal.getActivityId());
            if (engineType == null) {
                continue;
            }
            ActivityEngagementSummaryBuilder builder = buildersByEngineType.computeIfAbsent(engineType, ActivityEngagementSummaryBuilder::new);
            builder.addStarted(proposal.getOutcome() == ActivityProposalOutcome.STARTED);
            builder.addIgnored(proposal.getOutcome() == ActivityProposalOutcome.IGNORED);
        }

        for (var session : sessions) {
            String engineType = engineTypesByActivity.get(session.getActivityId());
            if (engineType == null) {
                continue;
            }
            ActivityEngagementSummaryBuilder builder = buildersByEngineType.computeIfAbsent(engineType, ActivityEngagementSummaryBuilder::new);
            builder.addCompleted(session.getFinalStatus() == GameSessionFinalStatus.COMPLETED);
            builder.addAbandoned(session.getFinalStatus() == GameSessionFinalStatus.ABANDONED);
        }

        List<ActivityEngagementSummary> items = buildersByEngineType.values().stream()
            .map(b -> b.build())
            .toList();

        return new ActivityEngagementSummaryResult(childProfileId, items);
    }

    private Set<Long> collectActivityIds(List<ActivityProposalLog> proposals, List<GameSessionSummary> sessions) {
        Set<Long> activityIds = proposals.stream()
            .map(p -> p.getActivityId())
            .collect(Collectors.toSet());
        activityIds.addAll(sessions.stream()
            .map(s -> s.getActivityId())
            .collect(Collectors.toSet()));
        return activityIds;
    }

    private static class ActivityEngagementSummaryBuilder {
        private final String gameEngineType;
        private long startedCount;
        private long ignoredCount;
        private long completedCount;
        private long abandonedCount;

        ActivityEngagementSummaryBuilder(String gameEngineType) {
            this.gameEngineType = gameEngineType;
        }

        void addStarted(boolean value) {
            if (value) startedCount++;
        }

        void addIgnored(boolean value) {
            if (value) ignoredCount++;
        }

        void addCompleted(boolean value) {
            if (value) completedCount++;
        }

        void addAbandoned(boolean value) {
            if (value) abandonedCount++;
        }

        ActivityEngagementSummary build() {
            return new ActivityEngagementSummary(gameEngineType, startedCount, ignoredCount, completedCount, abandonedCount);
        }
    }
}
