package es.vargontoc.educational.framework.session.infrastructure.scheduler;

import es.vargontoc.educational.framework.session.ports.out.ChildSessionRepository;
import es.vargontoc.educational.framework.session.ports.out.FamilySessionRepository;
import es.vargontoc.educational.framework.shared.config.SessionProperties;
import es.vargontoc.educational.framework.shared.retention.AbstractRetentionJob;
import es.vargontoc.educational.framework.shared.retention.RetentionPolicy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@ConditionalOnProperty(name = "app.session.scheduling.enabled", matchIfMissing = true)
public class SessionRetentionJob extends AbstractRetentionJob {

    private final ChildSessionRepository childSessionRepository;
    private final FamilySessionRepository familySessionRepository;
    private final SessionProperties sessionProperties;

    public SessionRetentionJob(
        ChildSessionRepository childSessionRepository,
        FamilySessionRepository familySessionRepository,
        SessionProperties sessionProperties
    ) {
        this.childSessionRepository = childSessionRepository;
        this.familySessionRepository = familySessionRepository;
        this.sessionProperties = sessionProperties;
    }

    @Override
    protected String jobName() {
        return "session-retention";
    }

    @Override
    protected List<RetentionPolicy> policies() {
        return List.of(
            childSessionPolicy(),
            familySessionPolicy()
        );
    }

    @Scheduled(cron = "0 0 2 * * *")
    @Override
    public void execute() {
        super.execute();
    }

    private RetentionPolicy childSessionPolicy() {
        return new RetentionPolicy() {
            @Override
            public String name() {
                return "child-session";
            }

            @Override
            public int retentionDays() {
                return sessionProperties.getRetentionDays();
            }

            @Override
            public int deleteExpired(LocalDateTime cutoff) {
                return childSessionRepository.deleteEndedBefore(cutoff);
            }
        };
    }

    private RetentionPolicy familySessionPolicy() {
        return new RetentionPolicy() {
            @Override
            public String name() {
                return "family-session";
            }

            @Override
            public int retentionDays() {
                return sessionProperties.getRetentionDays();
            }

            @Override
            public int deleteExpired(LocalDateTime cutoff) {
                return familySessionRepository.deleteInactiveUpdatedBefore(cutoff);
            }
        };
    }
}
