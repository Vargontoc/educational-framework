package es.vargontoc.educational.framework.session.infrastructure.scheduler;

import es.vargontoc.educational.framework.session.ports.in.ChildSessionUseCase;
import es.vargontoc.educational.framework.shared.config.SessionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@ConditionalOnProperty(name = "app.session.scheduling.enabled", matchIfMissing = true)
public class SessionExpirationJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionExpirationJob.class);

    private final ChildSessionUseCase childSessionUseCase;
    private final SessionProperties sessionProperties;

    public SessionExpirationJob(ChildSessionUseCase childSessionUseCase, SessionProperties sessionProperties) {
        this.childSessionUseCase = childSessionUseCase;
        this.sessionProperties = sessionProperties;
    }

    @Scheduled(cron = "0 */5 * * * *")
    public void expireInactiveSessions() {
        var graceSeconds = sessionProperties.getDefaultHeartbeatIntervalSeconds()
            * sessionProperties.getHeartbeatGraceMultiplier();
        var cutoff = LocalDateTime.now().minusSeconds(graceSeconds);
        var expired = childSessionUseCase.expireInactiveSessions(cutoff);

        LOGGER.info("Expired inactive child sessions: count={}, cutoff={}", expired, cutoff);
    }
}
