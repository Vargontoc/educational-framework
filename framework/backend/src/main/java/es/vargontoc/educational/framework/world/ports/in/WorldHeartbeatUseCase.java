package es.vargontoc.educational.framework.world.ports.in;

import es.vargontoc.educational.framework.world.model.WorldHeartbeatResult;

public interface WorldHeartbeatUseCase {

    WorldHeartbeatResult recordHeartbeat(Long childSessionId);

    WorldHeartbeatResult recordHeartbeat(Long childSessionId, Double positionX, Double positionY, String biome);
}