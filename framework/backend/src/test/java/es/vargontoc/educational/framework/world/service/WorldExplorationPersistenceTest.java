package es.vargontoc.educational.framework.world.service;

import es.vargontoc.educational.framework.content.model.Biome;
import es.vargontoc.educational.framework.content.model.WorldHostProjection;
import es.vargontoc.educational.framework.content.ports.in.WorldCatalogUseCase;
import es.vargontoc.educational.framework.session.ports.in.ChildSessionUseCase;
import es.vargontoc.educational.framework.world.model.WorldExplorationState;
import es.vargontoc.educational.framework.world.model.WorldHeartbeatResult;
import es.vargontoc.educational.framework.world.model.WorldInactivityStatus;
import es.vargontoc.educational.framework.world.model.WorldRuntimeStatus;
import es.vargontoc.educational.framework.world.model.WorldState;
import es.vargontoc.educational.framework.world.ports.out.WorldExplorationStateRepository;
import es.vargontoc.educational.framework.world.ports.out.WorldStateRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorldExplorationPersistenceTest {

    @Mock
    private WorldStateRegistry worldStateRegistry;

    @Mock
    private WorldProposalService worldProposalService;

    @Mock
    private ChildSessionUseCase childSessionUseCase;

    @Mock
    private WorldExplorationStateRepository worldExplorationStateRepository;

    @Mock
    private WorldCatalogUseCase worldCatalogUseCase;

    private WorldInactivityConfig inactivityConfig;
    private WorldHeartbeatService heartbeatService;
    private WorldOrchestratorService orchestratorService;

    @BeforeEach
    void setUp() {
        inactivityConfig = new WorldInactivityConfig();
        inactivityConfig.setInactivityThresholdSeconds(180);
        heartbeatService = new WorldHeartbeatService(worldStateRegistry, worldProposalService, childSessionUseCase, inactivityConfig, worldExplorationStateRepository);

        es.vargontoc.educational.framework.tracking.ports.in.SelectTopicsForDifficultyUseCase selectTopics =
            org.mockito.Mockito.mock(es.vargontoc.educational.framework.tracking.ports.in.SelectTopicsForDifficultyUseCase.class);
        es.vargontoc.educational.framework.world.ports.in.EngagementThresholdConfigUseCase engagementConfig =
            org.mockito.Mockito.mock(es.vargontoc.educational.framework.world.ports.in.EngagementThresholdConfigUseCase.class);
        WorldEngagementEvaluator evaluator = new WorldEngagementEvaluator();
        WorldExplorationConfig explorationConfig = new WorldExplorationConfig();

        orchestratorService = new WorldOrchestratorService(selectTopics, worldCatalogUseCase, engagementConfig, evaluator, worldStateRegistry, explorationConfig);
    }

    private WorldState createActiveWorldState(Long childSessionId, Long childProfileId) {
        WorldState state = new WorldState();
        state.setChildSessionId(childSessionId);
        state.setChildProfileId(childProfileId);
        state.setStatus(WorldRuntimeStatus.ACTIVE);
        state.setLastWorldActivityAt(LocalDateTime.now());
        state.setCreatedAt(LocalDateTime.now());
        state.setUpdatedAt(LocalDateTime.now());
        return state;
    }

    @Test
    void heartbeat_withPositionAndBiome_persistsExplorationState() {
        Long childSessionId = 100L;
        Long childProfileId = 42L;
        WorldState worldState = createActiveWorldState(childSessionId, childProfileId);

        when(worldStateRegistry.findByChildSessionId(childSessionId)).thenReturn(Optional.of(worldState));
        when(worldExplorationStateRepository.findByChildProfileId(childProfileId)).thenReturn(Optional.empty());
        doNothing().when(childSessionUseCase).recordHeartbeat(childSessionId);

        WorldHeartbeatResult result = heartbeatService.recordHeartbeat(childSessionId, 0.75, 0.25, "BEACH");

        assertNotNull(result);
        assertEquals(WorldInactivityStatus.ACTIVE, result.getStatus());

        ArgumentCaptor<WorldExplorationState> captor = ArgumentCaptor.forClass(WorldExplorationState.class);
        verify(worldExplorationStateRepository).save(captor.capture());
        WorldExplorationState saved = captor.getValue();
        assertEquals(childProfileId, saved.getChildProfileId());
        assertEquals("BEACH", saved.getBiome());
        assertEquals(0.75, saved.getPositionX());
        assertEquals(0.25, saved.getPositionY());
    }

    @Test
    void heartbeat_withoutPositionOrBiome_doesNotPersistExplorationState() {
        Long childSessionId = 100L;
        Long childProfileId = 42L;
        WorldState worldState = createActiveWorldState(childSessionId, childProfileId);

        when(worldStateRegistry.findByChildSessionId(childSessionId)).thenReturn(Optional.of(worldState));
        doNothing().when(childSessionUseCase).recordHeartbeat(childSessionId);

        heartbeatService.recordHeartbeat(childSessionId, null, null, null);

        verify(worldExplorationStateRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void heartbeat_positionClampedToNormalizedRange() {
        Long childSessionId = 100L;
        Long childProfileId = 42L;
        WorldState worldState = createActiveWorldState(childSessionId, childProfileId);

        when(worldStateRegistry.findByChildSessionId(childSessionId)).thenReturn(Optional.of(worldState));
        when(worldExplorationStateRepository.findByChildProfileId(childProfileId)).thenReturn(Optional.empty());
        doNothing().when(childSessionUseCase).recordHeartbeat(childSessionId);

        heartbeatService.recordHeartbeat(childSessionId, 1.5, -0.3, "FARM");

        ArgumentCaptor<WorldExplorationState> captor = ArgumentCaptor.forClass(WorldExplorationState.class);
        verify(worldExplorationStateRepository).save(captor.capture());
        WorldExplorationState saved = captor.getValue();
        assertEquals(1.0, saved.getPositionX());
        assertEquals(0.0, saved.getPositionY());
    }

    @Test
    void isBiomeAvailable_withActiveHost_returnsTrue() {
        when(worldCatalogUseCase.listActiveHostsForAge(3))
            .thenReturn(List.of(new WorldHostProjection(1L, "H1", "Host 1", Biome.BEACH, "Desc", 3, 10, "asset", 1, null)));

        assertTrue(orchestratorService.isBiomeAvailable("BEACH", 3));
    }

    @Test
    void isBiomeAvailable_withoutMatchingHost_returnsFalse() {
        when(worldCatalogUseCase.listActiveHostsForAge(3))
            .thenReturn(List.of(new WorldHostProjection(1L, "H1", "Host 1", Biome.MEADOW, "Desc", 3, 10, "asset", 1, null)));

        assertEquals(false, orchestratorService.isBiomeAvailable("SPACE", 3));
    }

    @Test
    void isBiomeAvailable_invalidBiomeName_returnsFalse() {
        assertEquals(false, orchestratorService.isBiomeAvailable("ATLANTIS", 3));
    }

    @Test
    void buildDestinationForBiomeOrDefault_availableBiome_usesRequestedBiome() {
        when(worldCatalogUseCase.listActiveHostsForAge(3))
            .thenReturn(List.of(
                new WorldHostProjection(1L, "H1", "Host 1", Biome.MEADOW, "Desc", 3, 10, "asset", 1, null),
                new WorldHostProjection(2L, "H2", "Host 2", Biome.WOODS, "Desc", 3, 10, "asset", 2, null)));
        when(worldCatalogUseCase.listActiveSituationsForAge(3)).thenReturn(Collections.emptyList());
        when(worldCatalogUseCase.listActiveElementsByBiomeAndAge(eq(Biome.WOODS), eq(3))).thenReturn(Collections.emptyList());

        var destination = orchestratorService.buildDestinationForBiomeOrDefault(100L, "WOODS", 3);

        assertNotNull(destination);
        assertEquals("WOODS", destination.getBiome());
    }

    @Test
    void buildDestinationForBiomeOrDefault_unavailableBiome_fallsBackToMeadow() {
        when(worldCatalogUseCase.listActiveHostsForAge(3))
            .thenReturn(List.of(new WorldHostProjection(1L, "H1", "Host 1", Biome.MEADOW, "Desc", 3, 10, "asset", 1, null)));
        when(worldCatalogUseCase.listActiveSituationsForAge(3)).thenReturn(Collections.emptyList());
        when(worldCatalogUseCase.listActiveElementsByBiomeAndAge(eq(Biome.MEADOW), eq(3))).thenReturn(Collections.emptyList());

        var destination = orchestratorService.buildDestinationForBiomeOrDefault(100L, "SPACE", 3);

        assertNotNull(destination);
        assertEquals("MEADOW", destination.getBiome());
    }

    @Test
    void buildDestinationForBiomeOrDefault_nullBiome_fallsBackToMeadow() {
        when(worldCatalogUseCase.listActiveHostsForAge(3))
            .thenReturn(List.of(new WorldHostProjection(1L, "H1", "Host 1", Biome.MEADOW, "Desc", 3, 10, "asset", 1, null)));
        when(worldCatalogUseCase.listActiveSituationsForAge(3)).thenReturn(Collections.emptyList());
        when(worldCatalogUseCase.listActiveElementsByBiomeAndAge(eq(Biome.MEADOW), eq(3))).thenReturn(Collections.emptyList());

        var destination = orchestratorService.buildDestinationForBiomeOrDefault(100L, null, 3);

        assertNotNull(destination);
        assertEquals("MEADOW", destination.getBiome());
    }
}
