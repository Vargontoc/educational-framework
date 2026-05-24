package es.vargontoc.educational.framework.shared.retention;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public abstract class AbstractRetentionJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRetentionJob.class);

    protected abstract List<RetentionPolicy> policies();

    protected abstract String jobName();

    @Transactional
    public void execute() {
        var now = LocalDateTime.now();
        LOGGER.info("Starting retention job: {}", jobName());

        for (var policy : policies()) {
            var cutoff = now.minusDays(policy.retentionDays());
            var deleted = policy.deleteExpired(cutoff);

            LOGGER.info(
                "RetentionPolicy [{}]: deleted={}, retentionDays={}, cutoff={}",
                policy.name(),
                deleted,
                policy.retentionDays(),
                cutoff
            );
        }

        LOGGER.info("Completed retention job: {}", jobName());
    }
}
