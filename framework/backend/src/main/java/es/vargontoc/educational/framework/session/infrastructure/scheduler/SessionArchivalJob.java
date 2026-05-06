package es.vargontoc.educational.framework.session.infrastructure.scheduler;

import es.vargontoc.educational.framework.session.ports.out.ChildSessionRepository;
import es.vargontoc.educational.framework.session.ports.out.FamilySessionRepository;
import es.vargontoc.educational.framework.shared.config.SessionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@ConditionalOnProperty(name = "app.session.scheduling.enabled", matchIfMissing = true)
public class SessionArchivalJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionArchivalJob.class);

    private final ChildSessionRepository childSessionRepository;
    private final FamilySessionRepository familySessionRepository;
    private final SessionProperties sessionProperties;

    public SessionArchivalJob(
        ChildSessionRepository childSessionRepository,
        FamilySessionRepository familySessionRepository,
        SessionProperties sessionProperties
    ) {
        this.childSessionRepository = childSessionRepository;
        this.familySessionRepository = familySessionRepository;
        this.sessionProperties = sessionProperties;
    }

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void archiveOldSessions() {
        var cutoff = LocalDateTime.now().minusDays(sessionProperties.getRetentionDays());
        var deletedChildSessions = childSessionRepository.deleteEndedBefore(cutoff);
        var deletedFamilySessions = familySessionRepository.deleteInactiveUpdatedBefore(cutoff);

        LOGGER.info(
            "Archived old sessions: childSessions={}, familySessions={}, cutoff={}",
            deletedChildSessions,
            deletedFamilySessions,
            cutoff
        );
    }
}
