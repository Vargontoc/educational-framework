package es.vargontoc.educational.framework.world.service;

import es.vargontoc.educational.framework.game.model.event.GameSessionCompletedEvent;
import es.vargontoc.educational.framework.tracking.model.GameSessionFinalStatus;
import es.vargontoc.educational.framework.world.model.WorldNarrativeCompletionStatus;
import es.vargontoc.educational.framework.world.model.WorldState;
import es.vargontoc.educational.framework.world.ports.out.WorldStateRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;

import java.util.Optional;

public class WorldGameCompletionListener {

    private static final Logger log = LoggerFactory.getLogger(WorldGameCompletionListener.class);

    private final WorldStateRegistry worldStateRegistry;

    public WorldGameCompletionListener(WorldStateRegistry worldStateRegistry) {
        this.worldStateRegistry = worldStateRegistry;
    }

    @EventListener
    public void onGameSessionCompleted(GameSessionCompletedEvent event) {
        log.debug("Received GameSessionCompletedEvent: gameId={}, status={}", event.gameId(), event.finalStatus());

        Optional<WorldState> worldStateOpt = worldStateRegistry.findByChildSessionId(event.childSessionId());

        if (worldStateOpt.isEmpty()) {
            log.debug("No WorldState found for childSessionId={}, ignoring event", event.childSessionId());
            return;
        }

        WorldState worldState = worldStateOpt.get();

        if (event.finalStatus() == GameSessionFinalStatus.COMPLETED) {
            worldState.setNarrativeCompletionStatus(WorldNarrativeCompletionStatus.AWAITING_NARRATIVE);
            worldStateRegistry.save(worldState);
            log.info("Game completed for childSessionId={}, awaiting narrative completion", event.childSessionId());
        } else if (event.finalStatus() == GameSessionFinalStatus.ABANDONED) {
            worldState.setNarrativeCompletionStatus(WorldNarrativeCompletionStatus.NO_PENDING);
            worldStateRegistry.save(worldState);
            log.info("Game abandoned for childSessionId={}, no narrative pending", event.childSessionId());
        }
    }
}