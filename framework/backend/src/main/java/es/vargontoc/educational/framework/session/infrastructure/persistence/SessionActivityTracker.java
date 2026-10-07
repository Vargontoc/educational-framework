package es.vargontoc.educational.framework.session.infrastructure.persistence;

import es.vargontoc.educational.framework.session.ports.out.ChildSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionActivityTracker {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionActivityTracker.class);

    private final Map<Long, LocalDateTime> activityBySession = new ConcurrentHashMap<>();
    private final ChildSessionRepository childSessionRepository;

    public SessionActivityTracker(ChildSessionRepository childSessionRepository) {
        this.childSessionRepository = childSessionRepository;
    }

    public void recordActivity(Long childSessionId) {
        activityBySession.put(childSessionId, LocalDateTime.now());
    }

    public LocalDateTime getLastActivity(Long childSessionId) {
        return activityBySession.get(childSessionId);
    }

    public boolean hasActivity(Long childSessionId) {
        return activityBySession.containsKey(childSessionId);
    }

    public void removeSession(Long childSessionId) {
        activityBySession.remove(childSessionId);
    }

    @Scheduled(fixedDelayString = "${app.session.activity-flush-interval-seconds:12}000")
    @Transactional
    public void flushAll() {
        if (activityBySession.isEmpty()) {
            return;
        }
        int count = 0;
        for (Map.Entry<Long, LocalDateTime> entry : activityBySession.entrySet()) {
            try {
                childSessionRepository.updateLastActivityAt(entry.getKey(), entry.getValue());
                count++;
            } catch (Exception e) {
                LOGGER.warn("Failed to flush activity for childSessionId={}: {}", entry.getKey(), e.getMessage());
            }
        }
        LOGGER.debug("Flushed activity for {} sessions", count);
    }

    @Transactional
    public void flushAndRemove(Long childSessionId) {
        LocalDateTime activity = activityBySession.remove(childSessionId);
        if (activity != null) {
            try {
                childSessionRepository.updateLastActivityAt(childSessionId, activity);
            } catch (Exception e) {
                LOGGER.warn("Failed to flush activity on close for childSessionId={}: {}", childSessionId, e.getMessage());
            }
        }
    }

    public int trackedSessionCount() {
        return activityBySession.size();
    }
}
