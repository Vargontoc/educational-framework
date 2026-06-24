package es.vargontoc.educational.framework.world.service;

import es.vargontoc.educational.framework.session.ports.in.ChildSessionUseCase;
import es.vargontoc.educational.framework.world.model.WorldHeartbeatResult;
import es.vargontoc.educational.framework.world.model.WorldInactivityStatus;
import es.vargontoc.educational.framework.world.model.WorldRuntimeStatus;
import es.vargontoc.educational.framework.world.model.WorldState;
import es.vargontoc.educational.framework.world.ports.out.WorldStateRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorldHeartbeatServiceTest {

    @Mock
    private WorldStateRegistry worldStateRegistry;

    @Mock
    private WorldProposalService worldProposalService;

    @Mock
    private ChildSessionUseCase childSessionUseCase;

    private WorldInactivityConfig inactivityConfig;

    private WorldHeartbeatService service;

    @BeforeEach
    void setUp() {
        inactivityConfig = new WorldInactivityConfig();
        inactivityConfig.setInactivityThresholdSeconds(180);
        service = new WorldHeartbeatService(worldStateRegistry, worldProposalService, childSessionUseCase, inactivityConfig);
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
    void recordHeartbeat_updatesWorldTimestamp() {
        Long childSessionId = 100L;
        WorldState worldState = createWorldState(childSessionId);

        when(worldStateRegistry.findByChildSessionId(childSessionId)).thenReturn(java.util.Optional.of(worldState));
        doNothing().when(childSessionUseCase).recordHeartbeat(childSessionId);

        WorldHeartbeatResult result = service.recordHeartbeat(childSessionId);

        assertNotNull(result);
        assertTrue(result.isWorldStateFound());
        assertTrue(result.isChildSessionActivityUpdated());
        assertNotNull(result.getLastWorldActivityAt());
        verify(worldStateRegistry).save(worldState);
        verify(childSessionUseCase).recordHeartbeat(childSessionId);
    }

    @Test
    void recordHeartbeat_callsSessionHeartbeat() {
        Long childSessionId = 100L;
        WorldState worldState = createWorldState(childSessionId);

        when(worldStateRegistry.findByChildSessionId(childSessionId)).thenReturn(java.util.Optional.of(worldState));
        doNothing().when(childSessionUseCase).recordHeartbeat(childSessionId);

        service.recordHeartbeat(childSessionId);

        verify(childSessionUseCase).recordHeartbeat(childSessionId);
    }

    @Test
    void recordHeartbeat_closesInactiveWorld() {
        Long childSessionId = 100L;
        WorldState worldState = createWorldState(childSessionId);
        worldState.setLastWorldActivityAt(LocalDateTime.now().minusSeconds(200));
        worldState.setPendingProposalId(5L);

        when(worldStateRegistry.findByChildSessionId(childSessionId)).thenReturn(java.util.Optional.of(worldState));
        doNothing().when(worldProposalService).resolveAndCloseWorld(childSessionId);

        WorldHeartbeatResult result = service.recordHeartbeat(childSessionId);

        assertNotNull(result);
        verify(worldProposalService).resolveAndCloseWorld(childSessionId);
    }

    @Test
    void recordHeartbeat_resolvesPendingProposalOnInactivity() {
        Long childSessionId = 100L;
        WorldState worldState = createWorldState(childSessionId);
        worldState.setLastWorldActivityAt(LocalDateTime.now().minusSeconds(200));
        worldState.setPendingProposalId(5L);

        when(worldStateRegistry.findByChildSessionId(childSessionId)).thenReturn(java.util.Optional.of(worldState));
        doNothing().when(worldProposalService).resolveAndCloseWorld(childSessionId);

        service.recordHeartbeat(childSessionId);

        verify(worldProposalService).resolveAndCloseWorld(childSessionId);
    }

    @Test
    void recordHeartbeat_noWorldState_noOps() {
        Long childSessionId = 100L;

        when(worldStateRegistry.findByChildSessionId(childSessionId)).thenReturn(java.util.Optional.empty());
        doNothing().when(childSessionUseCase).recordHeartbeat(childSessionId);

        WorldHeartbeatResult result = service.recordHeartbeat(childSessionId);

        assertNotNull(result);
        assertFalse(result.isWorldStateFound());
        assertTrue(result.isChildSessionActivityUpdated());
        assertEquals(WorldInactivityStatus.NO_WORLD_STATE, result.getStatus());
        verify(worldProposalService, never()).resolveAndCloseWorld(childSessionId);
        verify(childSessionUseCase).recordHeartbeat(childSessionId);
    }

    @Test
    void recordHeartbeat_activeWorld_doesNotClose() {
        Long childSessionId = 100L;
        WorldState worldState = createWorldState(childSessionId);
        worldState.setLastWorldActivityAt(LocalDateTime.now());

        when(worldStateRegistry.findByChildSessionId(childSessionId)).thenReturn(java.util.Optional.of(worldState));
        doNothing().when(childSessionUseCase).recordHeartbeat(childSessionId);

        WorldHeartbeatResult result = service.recordHeartbeat(childSessionId);

        assertNotNull(result);
        assertTrue(result.isWorldStateFound());
        verify(worldProposalService, never()).resolveAndCloseWorld(childSessionId);
    }
}