package es.vargontoc.educational.framework.world.service;

import es.vargontoc.educational.framework.game.model.event.GameSessionCompletedEvent;
import es.vargontoc.educational.framework.tracking.model.GameSessionFinalStatus;
import es.vargontoc.educational.framework.world.model.WorldNarrativeCompletionStatus;
import es.vargontoc.educational.framework.world.model.WorldRuntimeStatus;
import es.vargontoc.educational.framework.world.model.WorldState;
import es.vargontoc.educational.framework.world.ports.out.WorldStateRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorldGameCompletionListenerTest {

    @Mock
    private WorldStateRegistry worldStateRegistry;

    private WorldGameCompletionListener listener;

    @BeforeEach
    void setUp() {
        listener = new WorldGameCompletionListener(worldStateRegistry);
    }

    private WorldState createWorldState(Long childSessionId) {
        WorldState state = new WorldState();
        state.setChildSessionId(childSessionId);
        state.setChildProfileId(1L);
        state.setStatus(WorldRuntimeStatus.ACTIVE);
        state.setLastWorldActivityAt(LocalDateTime.now());
        state.setCreatedAt(LocalDateTime.now());
        state.setUpdatedAt(LocalDateTime.now());
        return state;
    }

    @Test
    void listener_completedSetsAwaitingStatus() {
        Long childSessionId = 100L;
        Long gameId = 1L;
        Long activityId = 10L;
        WorldState worldState = createWorldState(childSessionId);

        when(worldStateRegistry.findByChildSessionId(childSessionId)).thenReturn(Optional.of(worldState));

        GameSessionCompletedEvent event = new GameSessionCompletedEvent(
            gameId, childSessionId, activityId, GameSessionFinalStatus.COMPLETED, LocalDateTime.now()
        );

        listener.onGameSessionCompleted(event);

        assertEquals(WorldNarrativeCompletionStatus.AWAITING_NARRATIVE, worldState.getNarrativeCompletionStatus());
        verify(worldStateRegistry).save(worldState);
    }

    @Test
    void listener_abandonedClearsStatus() {
        Long childSessionId = 100L;
        Long gameId = 1L;
        Long activityId = 10L;
        WorldState worldState = createWorldState(childSessionId);
        worldState.setNarrativeCompletionStatus(WorldNarrativeCompletionStatus.AWAITING_NARRATIVE);

        when(worldStateRegistry.findByChildSessionId(childSessionId)).thenReturn(Optional.of(worldState));

        GameSessionCompletedEvent event = new GameSessionCompletedEvent(
            gameId, childSessionId, activityId, GameSessionFinalStatus.ABANDONED, LocalDateTime.now()
        );

        listener.onGameSessionCompleted(event);

        assertEquals(WorldNarrativeCompletionStatus.NO_PENDING, worldState.getNarrativeCompletionStatus());
        verify(worldStateRegistry).save(worldState);
    }

    @Test
    void listener_ignoresUnrelatedSession() {
        
        Long differentSessionId = 200L;

        when(worldStateRegistry.findByChildSessionId(differentSessionId)).thenReturn(Optional.empty());

        GameSessionCompletedEvent event = new GameSessionCompletedEvent(
            1L, differentSessionId, 10L, GameSessionFinalStatus.COMPLETED, LocalDateTime.now()
        );

        listener.onGameSessionCompleted(event);
    }
}