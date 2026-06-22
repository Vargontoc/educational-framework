package es.vargontoc.educational.framework.session.service;

import es.vargontoc.educational.framework.game.ports.in.GameOrchestrator;
import es.vargontoc.educational.framework.session.model.ChildSessionHeartbeatResult;
import es.vargontoc.educational.framework.session.infrastructure.websocket.SessionEvent;
import es.vargontoc.educational.framework.session.infrastructure.websocket.SessionEventPublisher;
import es.vargontoc.educational.framework.session.infrastructure.websocket.SessionEventType;
import es.vargontoc.educational.framework.session.model.ChildSession;
import es.vargontoc.educational.framework.session.model.ChildSessionStatus;
import es.vargontoc.educational.framework.session.ports.in.ChildSessionUseCase;
import es.vargontoc.educational.framework.session.ports.out.ChildSessionRepository;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
import es.vargontoc.educational.framework.shared.exception.SessionException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ChildSessionService implements ChildSessionUseCase {

    private final ChildSessionRepository childSessionRepository;
    private final SessionEventPublisher sessionEventPublisher;
    private final GameOrchestrator gameOrchestrator;

    public ChildSessionService(
            ChildSessionRepository childSessionRepository,
            @Lazy SessionEventPublisher sessionEventPublisher,
            @Lazy GameOrchestrator gameOrchestrator) {
        this.childSessionRepository = childSessionRepository;
        this.sessionEventPublisher = sessionEventPublisher;
        this.gameOrchestrator = gameOrchestrator;
    }

    @Override
    public ChildSession openSession(Long childProfileId, Long familyId, int heartbeatInterval, String connectionMeta) {
        var now = LocalDateTime.now();

        childSessionRepository.findActiveByChildProfileId(childProfileId)
            .ifPresent(existing -> {
                closeExistingSession(existing, now, ChildSessionStatus.CLOSED);
                childSessionRepository.save(existing);
                sessionEventPublisher.notifyChildWithFarewellAndParent(
                    existing.getId(),
                    existing.getFamilyId(),
                    SessionEvent.of(SessionEventType.SESSION_EXPIRED, existing.getId(),
                        Map.of("reason", "new_session_opened"))
                );
            });

        var session = new ChildSession();
        session.setChildProfileId(childProfileId);
        session.setFamilyId(familyId);
        session.setCreatedAt(now);
        session.setStartedAt(now);
        session.setStatus(ChildSessionStatus.ACTIVE);
        session.setLastActivityAt(now);
        session.setHeartbeatIntervalSeconds(heartbeatInterval);
        session.setConnectionMeta(connectionMeta);

        return childSessionRepository.save(session);
    }

    @Override
    public ChildSession closeSession(Long id) {
        var session = findById(id);
        closeExistingSession(session, LocalDateTime.now(), ChildSessionStatus.CLOSED);
        var saved = childSessionRepository.save(session);
        return saved;
    }

    @Override
    public ChildSession expelChild(Long id) {
        var session = findById(id);
        Long childSessionId = session.getId();
        closeExistingSession(session, LocalDateTime.now(), ChildSessionStatus.EXPELLED);
        var saved = childSessionRepository.save(session);

        sessionEventPublisher.notifyChildWithFarewellAndParent(
            saved.getId(),
            saved.getFamilyId(),
            SessionEvent.of(SessionEventType.CHILD_EXPELLED, saved.getId())
        );

        try {
            gameOrchestrator.abandonGameForSession(childSessionId);
        } catch (Exception e) {
            // Log but don't fail the session close
        }

        return saved;
    }

    @Override
    public void recordHeartbeat(Long id) {
        var session = findById(id);

        if (!ChildSessionStatus.ACTIVE.equals(session.getStatus())) {
            throw new SessionException("Child session is not active");
        }

        session.setLastActivityAt(LocalDateTime.now());
        childSessionRepository.save(session);
    }

    @Override
    public ChildSessionHeartbeatResult recordGameHeartbeat(Long childSessionId) {
        var sessionOpt = childSessionRepository.findById(childSessionId);

        if (sessionOpt.isEmpty()) {
            return new ChildSessionHeartbeatResult(false, null, null);
        }

        var session = sessionOpt.get();
        if (!ChildSessionStatus.ACTIVE.equals(session.getStatus())) {
            return new ChildSessionHeartbeatResult(false, session.getStatus().name(), session.getLastActivityAt());
        }

        session.setLastActivityAt(LocalDateTime.now());
        childSessionRepository.save(session);

        return new ChildSessionHeartbeatResult(true, session.getStatus().name(), session.getLastActivityAt());
    }

    @Override
    @Transactional(readOnly = true)
    public ChildSession getSession(Long id) {
        return childSessionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Child session not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChildSession> getActiveSessions(Long familyId) {
        return childSessionRepository.findActiveByFamilyId(familyId);
    }

    @Override
    public int expireInactiveSessions(LocalDateTime cutoff) {
        var sessions = childSessionRepository.findExpirableSessions(cutoff);

        for (ChildSession session : sessions) {
            session.setStatus(ChildSessionStatus.EXPIRED);
        }

        childSessionRepository.saveAll(sessions);

        for (ChildSession session : sessions) {
            Long childSessionId = session.getId();
            sessionEventPublisher.notifyChildWithFarewellAndParent(
                session.getId(),
                session.getFamilyId(),
                SessionEvent.of(SessionEventType.SESSION_EXPIRED, session.getId(),
                    Map.of("reason", "inactivity"))
            );

            try {
                gameOrchestrator.abandonGameForSession(childSessionId);
            } catch (Exception e) {
                // Log but don't fail the batch
            }
        }

        return sessions.size();
    }

    private ChildSession findById(Long id) {
        return childSessionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Child session not found"));
    }

    private void closeExistingSession(ChildSession session, LocalDateTime endedAt, ChildSessionStatus status) {
        session.setEndedAt(endedAt);
        session.setDurationSeconds(calculateDurationSeconds(session.getStartedAt(), endedAt));
        session.setStatus(status);
    }

    private Integer calculateDurationSeconds(LocalDateTime startedAt, LocalDateTime endedAt) {
        if (startedAt == null) {
            return 0;
        }

        return Math.toIntExact(Math.max(0, Duration.between(startedAt, endedAt).getSeconds()));
    }
}
