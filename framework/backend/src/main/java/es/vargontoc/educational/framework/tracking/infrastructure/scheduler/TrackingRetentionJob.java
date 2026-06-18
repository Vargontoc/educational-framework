package es.vargontoc.educational.framework.tracking.infrastructure.scheduler;

import es.vargontoc.educational.framework.tracking.ports.out.ActivityAttemptRepository;
import es.vargontoc.educational.framework.shared.retention.AbstractRetentionJob;
import es.vargontoc.educational.framework.shared.retention.RetentionPolicy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@ConditionalOnProperty(name = "app.tracking.scheduling.enabled", matchIfMissing = true)
public class TrackingRetentionJob extends AbstractRetentionJob {

    static final int RETENTION_DAYS = 180;

    private final ActivityAttemptRepository activityAttemptRepository;

    public TrackingRetentionJob(ActivityAttemptRepository activityAttemptRepository) {
        this.activityAttemptRepository = activityAttemptRepository;
    }

    @Override
    protected String jobName() {
        return "tracking-retention";
    }

    @Override
    protected List<RetentionPolicy> policies() {
        return List.of(activityAttemptPolicy());
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Override
    public void execute() {
        super.execute();
    }

    private RetentionPolicy activityAttemptPolicy() {
        return new RetentionPolicy() {
            @Override
            public String name() {
                return "activity-attempt";
            }

            @Override
            public int retentionDays() {
                return RETENTION_DAYS;
            }

            @Override
            public int deleteExpired(LocalDateTime cutoff) {
                return activityAttemptRepository.deleteCreatedAtBefore(cutoff);
            }
        };
    }
}
