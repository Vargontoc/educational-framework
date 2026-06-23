package es.vargontoc.educational.framework.world.infrastructure;

import es.vargontoc.educational.framework.world.model.WorldRuntimeStatus;
import es.vargontoc.educational.framework.world.model.WorldState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryWorldStateRegistryTest {

    private InMemoryWorldStateRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new InMemoryWorldStateRegistry();
    }

    private WorldState createWorldState(Long childSessionId, WorldRuntimeStatus status) {
        WorldState state = new WorldState();
        state.setChildSessionId(childSessionId);
        state.setStatus(status);
        state.setLastWorldActivityAt(LocalDateTime.now());
        state.setCreatedAt(LocalDateTime.now());
        state.setUpdatedAt(LocalDateTime.now());
        return state;
    }

    @Test
    void save_and_findByChildSessionId_returnsStoredState() {
        WorldState state = createWorldState(100L, WorldRuntimeStatus.ACTIVE);
        registry.save(state);

        Optional<WorldState> result = registry.findByChildSessionId(100L);

        assertTrue(result.isPresent());
        assertEquals(100L, result.get().getChildSessionId());
        assertEquals(WorldRuntimeStatus.ACTIVE, result.get().getStatus());
    }

    @Test
    void findByChildSessionId_returnsEmptyForUnknownSession() {
        Optional<WorldState> result = registry.findByChildSessionId(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void existsByChildSessionId_returnsTrueWhenActive() {
        WorldState state = createWorldState(100L, WorldRuntimeStatus.ACTIVE);
        registry.save(state);

        assertTrue(registry.existsByChildSessionId(100L));
    }

    @Test
    void existsByChildSessionId_returnsFalseWhenNoState() {
        assertFalse(registry.existsByChildSessionId(100L));
    }

    @Test
    void existsByChildSessionId_returnsFalseWhenClosed() {
        WorldState state = createWorldState(100L, WorldRuntimeStatus.CLOSED);
        registry.save(state);

        assertFalse(registry.existsByChildSessionId(100L));
    }

    @Test
    void removeByChildSessionId_deletesStateAndReturnsIt() {
        WorldState state = createWorldState(100L, WorldRuntimeStatus.ACTIVE);
        registry.save(state);

        Optional<WorldState> result = registry.removeByChildSessionId(100L);

        assertTrue(result.isPresent());
        assertEquals(100L, result.get().getChildSessionId());
        assertFalse(registry.existsByChildSessionId(100L));
    }

    @Test
    void removeByChildSessionId_returnsEmptyWhenNotFound() {
        Optional<WorldState> result = registry.removeByChildSessionId(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void clearClosed_removesOnlyClosedStates() {
        WorldState activeState = createWorldState(100L, WorldRuntimeStatus.ACTIVE);
        WorldState closedState = createWorldState(200L, WorldRuntimeStatus.CLOSED);
        registry.save(activeState);
        registry.save(closedState);

        registry.clearClosed();

        assertTrue(registry.existsByChildSessionId(100L));
        assertFalse(registry.existsByChildSessionId(200L));
    }

    @Test
    void save_replacesExistingStateForSameChildSession() {
        WorldState state1 = createWorldState(100L, WorldRuntimeStatus.ACTIVE);
        registry.save(state1);

        WorldState state2 = createWorldState(100L, WorldRuntimeStatus.ACTIVE);
        registry.save(state2);

        Optional<WorldState> result = registry.findByChildSessionId(100L);
        assertTrue(result.isPresent());
        assertNotNull(result.get().getCreatedAt());
    }

    @Test
    void findByChildSessionId_returnsEmptyWhenClosed() {
        WorldState state = createWorldState(100L, WorldRuntimeStatus.CLOSED);
        registry.save(state);

        Optional<WorldState> result = registry.findByChildSessionId(100L);

        assertFalse(result.isPresent());
    }
}