package es.vargontoc.educational.framework.tracking.infrastructure.scheduler;

import es.vargontoc.educational.framework.tracking.ports.out.ActivityAttemptRepository;
import es.vargontoc.educational.framework.shared.retention.RetentionPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrackingRetentionJobTest {

    @Mock
    private ActivityAttemptRepository activityAttemptRepository;

    private TrackingRetentionJob job;

    @BeforeEach
    void setUp() {
        job = new TrackingRetentionJob(activityAttemptRepository);
    }

    @Test
    void jobName_returnsTrackingRetention() {
        assertEquals("tracking-retention", job.jobName());
    }

    @Test
    void policies_returnsOnePolicy() {
        List<RetentionPolicy> policies = job.policies();

        assertEquals(1, policies.size());
        assertEquals("activity-attempt", policies.get(0).name());
    }

    @Test
    void policy_uses180DaysRetention() {
        List<RetentionPolicy> policies = job.policies();
        RetentionPolicy policy = policies.get(0);

        assertEquals(180, policy.retentionDays());
    }

    @Test
    void policy_delegatesToRepository() {
        when(activityAttemptRepository.deleteCreatedAtBefore(any(LocalDateTime.class))).thenReturn(10);

        List<RetentionPolicy> policies = job.policies();
        var cutoff = LocalDateTime.now().minusDays(180);

        int deleted = policies.get(0).deleteExpired(cutoff);

        assertEquals(10, deleted);
        verify(activityAttemptRepository).deleteCreatedAtBefore(any(LocalDateTime.class));
    }

    @Test
    void execute_callsRepository() {
        when(activityAttemptRepository.deleteCreatedAtBefore(any(LocalDateTime.class))).thenReturn(5);

        job.execute();

        verify(activityAttemptRepository).deleteCreatedAtBefore(any(LocalDateTime.class));
    }
}
