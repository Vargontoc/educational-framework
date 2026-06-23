package es.vargontoc.educational.framework.tracking.service;

import es.vargontoc.educational.framework.tracking.model.ActivityEngagementSummary;
import es.vargontoc.educational.framework.tracking.model.ActivityEngagementSummaryResult;
import es.vargontoc.educational.framework.tracking.model.ActivityProposalLog;
import es.vargontoc.educational.framework.tracking.model.ActivityProposalOutcome;
import es.vargontoc.educational.framework.tracking.model.GameSessionSummary;
import es.vargontoc.educational.framework.tracking.model.GameSessionFinalStatus;
import es.vargontoc.educational.framework.tracking.ports.out.ActivityInformationPort;
import es.vargontoc.educational.framework.tracking.ports.out.ActivityProposalLogRepository;
import es.vargontoc.educational.framework.tracking.ports.out.GameSessionSummaryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityEngagementSummaryServiceTest {

    @Mock
    private ActivityProposalLogRepository proposalLogRepository;

    @Mock
    private GameSessionSummaryRepository sessionSummaryRepository;

    @Mock
    private ActivityInformationPort activityInformationPort;

    @InjectMocks
    private ActivityEngagementSummaryService service;

    @Test
    void getActivityEngagementSummary_withProposalsAndSessions_aggregatesCorrectly() {
        var proposal1 = createProposal(1L, 10L, 30L, ActivityProposalOutcome.STARTED);
        var proposal2 = createProposal(2L, 10L, 30L, ActivityProposalOutcome.IGNORED);
        var session1 = createSession(1L, 10L, 30L, GameSessionFinalStatus.COMPLETED);
        var session2 = createSession(2L, 10L, 30L, GameSessionFinalStatus.ABANDONED);

        when(proposalLogRepository.findByChildProfileId(10L)).thenReturn(List.of(proposal1, proposal2));
        when(sessionSummaryRepository.findByChildProfileId(10L)).thenReturn(List.of(session1, session2));
        when(activityInformationPort.getGameEngineTypeByActivityIds(Set.of(30L)))
            .thenReturn(Map.of(30L, "PUZZLE"));

        var result = service.getActivityEngagementSummary(10L);

        assertEquals(10L, result.childProfileId());
        assertEquals(1, result.items().size());

        var summary = result.items().get(0);
        assertEquals("PUZZLE", summary.gameEngineType());
        assertEquals(1, summary.startedCount());
        assertEquals(1, summary.ignoredCount());
        assertEquals(1, summary.completedCount());
        assertEquals(1, summary.abandonedCount());
    }

    @Test
    void getActivityEngagementSummary_withMultipleEngineTypes_groupsSeparately() {
        var proposal1 = createProposal(1L, 10L, 30L, ActivityProposalOutcome.STARTED);
        var proposal2 = createProposal(2L, 10L, 40L, ActivityProposalOutcome.IGNORED);

        when(proposalLogRepository.findByChildProfileId(10L)).thenReturn(List.of(proposal1, proposal2));
        when(sessionSummaryRepository.findByChildProfileId(10L)).thenReturn(List.of());
        when(activityInformationPort.getGameEngineTypeByActivityIds(Set.of(30L, 40L)))
            .thenReturn(Map.of(30L, "PUZZLE", 40L, "QUIZ"));

        var result = service.getActivityEngagementSummary(10L);

        assertEquals(2, result.items().size());
    }

    @Test
    void getActivityEngagementSummary_withNoData_returnsEmptyList() {
        when(proposalLogRepository.findByChildProfileId(10L)).thenReturn(List.of());
        when(sessionSummaryRepository.findByChildProfileId(10L)).thenReturn(List.of());
        when(activityInformationPort.getGameEngineTypeByActivityIds(Set.of()))
            .thenReturn(Map.of());

        var result = service.getActivityEngagementSummary(10L);

        assertEquals(10L, result.childProfileId());
        assertEquals(0, result.items().size());
    }

    @Test
    void getActivityEngagementSummary_withMissingActivity_omitsFromSummary() {
        var proposal = createProposal(1L, 10L, 999L, ActivityProposalOutcome.STARTED);

        when(proposalLogRepository.findByChildProfileId(10L)).thenReturn(List.of(proposal));
        when(sessionSummaryRepository.findByChildProfileId(10L)).thenReturn(List.of());
        when(activityInformationPort.getGameEngineTypeByActivityIds(Set.of(999L)))
            .thenReturn(Map.of());

        var result = service.getActivityEngagementSummary(10L);

        assertEquals(0, result.items().size());
    }

    @Test
    void getActivityEngagementSummary_onlyProposals_calculatesProposalCounts() {
        var proposal1 = createProposal(1L, 10L, 30L, ActivityProposalOutcome.STARTED);
        var proposal2 = createProposal(2L, 10L, 30L, ActivityProposalOutcome.STARTED);
        var proposal3 = createProposal(3L, 10L, 30L, ActivityProposalOutcome.IGNORED);

        when(proposalLogRepository.findByChildProfileId(10L)).thenReturn(List.of(proposal1, proposal2, proposal3));
        when(sessionSummaryRepository.findByChildProfileId(10L)).thenReturn(List.of());
        when(activityInformationPort.getGameEngineTypeByActivityIds(Set.of(30L)))
            .thenReturn(Map.of(30L, "PUZZLE"));

        var result = service.getActivityEngagementSummary(10L);

        assertEquals(1, result.items().size());
        var summary = result.items().get(0);
        assertEquals(2, summary.startedCount());
        assertEquals(1, summary.ignoredCount());
        assertEquals(0, summary.completedCount());
        assertEquals(0, summary.abandonedCount());
    }

    @Test
    void getActivityEngagementSummary_onlySessions_calculatesSessionCounts() {
        var session1 = createSession(1L, 10L, 30L, GameSessionFinalStatus.COMPLETED);
        var session2 = createSession(2L, 10L, 30L, GameSessionFinalStatus.COMPLETED);
        var session3 = createSession(3L, 10L, 30L, GameSessionFinalStatus.ABANDONED);

        when(proposalLogRepository.findByChildProfileId(10L)).thenReturn(List.of());
        when(sessionSummaryRepository.findByChildProfileId(10L)).thenReturn(List.of(session1, session2, session3));
        when(activityInformationPort.getGameEngineTypeByActivityIds(Set.of(30L)))
            .thenReturn(Map.of(30L, "QUIZ"));

        var result = service.getActivityEngagementSummary(10L);

        assertEquals(1, result.items().size());
        var summary = result.items().get(0);
        assertEquals(0, summary.startedCount());
        assertEquals(0, summary.ignoredCount());
        assertEquals(2, summary.completedCount());
        assertEquals(1, summary.abandonedCount());
    }

    private ActivityProposalLog createProposal(Long id, Long childProfileId, Long activityId, ActivityProposalOutcome outcome) {
        var proposal = new ActivityProposalLog();
        proposal.setId(id);
        proposal.setChildProfileId(childProfileId);
        proposal.setChildSessionId(100L);
        proposal.setActivityId(activityId);
        proposal.setOutcome(outcome);
        proposal.setProposedAt(LocalDateTime.now());
        return proposal;
    }

    private GameSessionSummary createSession(Long id, Long childProfileId, Long activityId, GameSessionFinalStatus status) {
        var session = new GameSessionSummary();
        session.setId(id);
        session.setChildProfileId(childProfileId);
        session.setChildSessionId(100L);
        session.setActivityId(activityId);
        session.setFinalStatus(status);
        session.setStartedAt(LocalDateTime.now().minusMinutes(10));
        session.setEndedAt(LocalDateTime.now());
        return session;
    }
}
