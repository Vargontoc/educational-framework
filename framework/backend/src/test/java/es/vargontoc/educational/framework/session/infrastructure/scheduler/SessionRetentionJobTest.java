package es.vargontoc.educational.framework.session.infrastructure.scheduler;

import es.vargontoc.educational.framework.session.ports.out.ChildSessionRepository;
import es.vargontoc.educational.framework.session.ports.out.FamilySessionRepository;
import es.vargontoc.educational.framework.shared.config.SessionProperties;
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
class SessionRetentionJobTest {

    @Mock
    private ChildSessionRepository childSessionRepository;

    @Mock
    private FamilySessionRepository familySessionRepository;

    @Mock
    private SessionProperties sessionProperties;

    private SessionRetentionJob job;

    @BeforeEach
    void setUp() {
        job = new SessionRetentionJob(
            childSessionRepository,
            familySessionRepository,
            sessionProperties
        );
    }

    @Test
    void jobName_returnsSessionRetention() {
        assertEquals("session-retention", job.jobName());
    }

    @Test
    void policies_returnsTwoPolicies() {
        List<RetentionPolicy> policies = job.policies();

        assertEquals(2, policies.size());
        assertEquals("child-session", policies.get(0).name());
        assertEquals("family-session", policies.get(1).name());
    }

    @Test
    void childSessionPolicy_usesRetentionDaysFromProperties() {
        when(sessionProperties.getRetentionDays()).thenReturn(15);

        List<RetentionPolicy> policies = job.policies();
        RetentionPolicy childPolicy = policies.get(0);

        assertEquals(15, childPolicy.retentionDays());
    }

    @Test
    void familySessionPolicy_usesRetentionDaysFromProperties() {
        when(sessionProperties.getRetentionDays()).thenReturn(20);

        List<RetentionPolicy> policies = job.policies();
        RetentionPolicy familyPolicy = policies.get(1);

        assertEquals(20, familyPolicy.retentionDays());
    }

    @Test
    void policies_delegateToRepositories() {
        when(childSessionRepository.deleteEndedBefore(any(LocalDateTime.class))).thenReturn(5);
        when(familySessionRepository.deleteInactiveUpdatedBefore(any(LocalDateTime.class))).thenReturn(3);

        List<RetentionPolicy> policies = job.policies();
        var cutoff = LocalDateTime.now().minusDays(30);

        int childDeleted = policies.get(0).deleteExpired(cutoff);
        int familyDeleted = policies.get(1).deleteExpired(cutoff);

        assertEquals(5, childDeleted);
        assertEquals(3, familyDeleted);
        verify(childSessionRepository).deleteEndedBefore(any(LocalDateTime.class));
        verify(familySessionRepository).deleteInactiveUpdatedBefore(any(LocalDateTime.class));
    }

    @Test
    void execute_callsBothRepositories() {
        when(sessionProperties.getRetentionDays()).thenReturn(30);
        when(childSessionRepository.deleteEndedBefore(any(LocalDateTime.class))).thenReturn(5);
        when(familySessionRepository.deleteInactiveUpdatedBefore(any(LocalDateTime.class))).thenReturn(3);

        job.execute();

        verify(childSessionRepository).deleteEndedBefore(any(LocalDateTime.class));
        verify(familySessionRepository).deleteInactiveUpdatedBefore(any(LocalDateTime.class));
    }
}
