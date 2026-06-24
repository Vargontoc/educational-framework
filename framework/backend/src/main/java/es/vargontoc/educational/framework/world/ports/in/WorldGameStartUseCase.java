package es.vargontoc.educational.framework.world.ports.in;

import es.vargontoc.educational.framework.world.model.WorldGameStartResult;

public interface WorldGameStartUseCase {

    WorldGameStartResult startGameFromProposal(Long childSessionId, Long activityId);
}