package es.vargontoc.educational.framework.session.service;

import es.vargontoc.educational.framework.session.model.ChildSession;
import es.vargontoc.educational.framework.session.model.ChildSessionStatus;
import es.vargontoc.educational.framework.session.ports.in.ChildSessionUseCase;
import es.vargontoc.educational.framework.session.ports.out.ChildSessionRepository;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
import es.vargontoc.educational.framework.shared.exception.SessionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ChildSessionService implements ChildSessionUseCase {

    private final ChildSessionRepository childSessionRepository;

    public ChildSessionService(ChildSessionRepository childSessionRepository) {
        this.childSessionRepository = childSessionRepository;
    }

    @Override
    public ChildSession openSession(Long childProfileId, Long familyId, int heartbeatInterval, String connectionMeta) {
        var now = LocalDateTime.now();

        childSessionRepository.findActiveByChildProfileId(childProfileId)
            .ifPresent(existing -> {
                closeExistingSession(existing, now, ChildSessionStatus.CLOSED);
                childSessionRepository.save(existing);
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
        return childSessionRepository.save(session);
    }

    @Override
    public ChildSession expelChild(Long id) {
        var session = findById(id);
        closeExistingSession(session, LocalDateTime.now(), ChildSessionStatus.EXPELLED);
        return childSessionRepository.save(session);
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
