package es.vargontoc.educational.framework.world.ports.in;

import es.vargontoc.educational.framework.world.model.WorldHeartbeatResult;

public interface WorldHeartbeatUseCase {

    WorldHeartbeatResult recordHeartbeat(Long childSessionId);
}