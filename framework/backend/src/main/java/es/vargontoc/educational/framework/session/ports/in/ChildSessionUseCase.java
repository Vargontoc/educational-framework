package es.vargontoc.educational.framework.session.ports.in;

import es.vargontoc.educational.framework.session.model.ChildSession;

import java.time.LocalDateTime;
import java.util.List;

public interface ChildSessionUseCase {

    ChildSession openSession(Long childProfileId, Long familyId, int heartbeatInterval, String connectionMeta);

    ChildSession closeSession(Long id);

    ChildSession expelChild(Long id);

    void recordHeartbeat(Long id);

    List<ChildSession> getActiveSessions(Long familyId);

    int expireInactiveSessions(LocalDateTime cutoff);
}
