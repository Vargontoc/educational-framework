package es.vargontoc.educational.framework.world.ports.in;

import es.vargontoc.educational.framework.game.model.LaunchContext;
import es.vargontoc.educational.framework.world.model.WorldGameStartResult;

public interface WorldGameStartUseCase {

    WorldGameStartResult startGameFromProposal(Long childSessionId, Long activityId);

    /**
     * Launch context (current biome, host, discovery element...) of the world proposal that offers the activity.
     * Lets a game started outside {@link #startGameFromProposal} (the client's {@code game_start}) keep the
     * player's biome.
     *
     * @return the context, or {@code null} when there is no world state or no matching animal proposal
     */
    LaunchContext resolveLaunchContext(Long childSessionId, Long activityId);
}