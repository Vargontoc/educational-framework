package es.vargontoc.educational.framework.world.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldStateTest {

    @Test
    void worldState_canTrackLastWorldActivityAt() {
        WorldState state = new WorldState();
        LocalDateTime now = LocalDateTime.now();

        state.setLastWorldActivityAt(now);

        assertEquals(now, state.getLastWorldActivityAt());
    }

    @Test
    void worldState_pendingProposalCanBeAttachedAndCleared() {
        WorldState state = new WorldState();
        assertNull(state.getPendingProposalId());

        state.setPendingProposalId(42L);
        assertEquals(42L, state.getPendingProposalId());

        state.setPendingProposalId(null);
        assertNull(state.getPendingProposalId());
    }

    @Test
    void worldState_doesNotIncludeTrackingOwnedPersistentIds() {
        WorldState state = new WorldState();
        state.setChildSessionId(100L);
        state.setChildProfileId(10L);
        state.setStatus(WorldRuntimeStatus.ACTIVE);
        state.setLastWorldActivityAt(LocalDateTime.now());
        state.setCreatedAt(LocalDateTime.now());
        state.setUpdatedAt(LocalDateTime.now());

        assertNotNull(state.getChildSessionId());
        assertNotNull(state.getChildProfileId());
        assertNotNull(state.getStatus());
        assertNotNull(state.getLastWorldActivityAt());
        assertNotNull(state.getCreatedAt());
        assertNotNull(state.getUpdatedAt());

        assertEquals(WorldRuntimeStatus.ACTIVE, state.getStatus());
    }

    @Test
    void worldEngagementWindow_canRecordAbandonmentByEngineType() {
        WorldEngagementWindow window = new WorldEngagementWindow();
        window.setChildSessionId(100L);

        WorldEngineAbandonmentSignal signal = new WorldEngineAbandonmentSignal();
        signal.setEngineType("PUZZLE");
        signal.setActivityId(5L);
        signal.setFinalStatus("ABANDONED");
        signal.setOccurredAt(LocalDateTime.now());

        window.addSignal(signal);

        assertEquals(100L, window.getChildSessionId());
        assertEquals(1, window.getSignals().size());
        assertEquals("PUZZLE", window.getSignals().get(0).getEngineType());
        assertEquals("ABANDONED", window.getSignals().get(0).getFinalStatus());
    }

    @Test
    void worldState_initializesWithEmptyDiscoveryElements() {
        WorldState state = new WorldState();

        assertNotNull(state.getVisibleDiscoveryElements());
        assertTrue(state.getVisibleDiscoveryElements().isEmpty());
    }

    @Test
    void worldDestination_canHaveDiscoveryProposals() {
        WorldDestination destination = new WorldDestination();
        destination.setDestinationId("dest-1");
        destination.setHostId(1L);
        destination.setHostCode("FOREST_HOST");
        destination.setHostDisplayName("Forest Temple");
        destination.setNarrativeSituationId(10L);
        destination.setNarrativeSituationCode("FOREST_TEMPLE");
        destination.setBiome("FOREST");

        WorldDiscoveryProposal proposal = new WorldDiscoveryProposal();
        proposal.setProposalRuntimeId("proposal-1");
        proposal.setDiscoveryElementId(100L);
        proposal.setDiscoveryElementCode("MYSTERY_TREE");
        proposal.setDisplayName("Mysterious Tree");
        proposal.setElementType("DISCOVERY");

        destination.getDiscoveryProposals().add(proposal);

        assertEquals(1, destination.getDiscoveryProposals().size());
        assertEquals("Mysterious Tree", destination.getDiscoveryProposals().get(0).getDisplayName());
    }

    @Test
    void worldDiscoveryProposal_trackingProposalIdIsInternalOnly() {
        WorldDiscoveryProposal proposal = new WorldDiscoveryProposal();
        proposal.setProposalRuntimeId("runtime-1");
        proposal.setDiscoveryElementId(50L);
        proposal.setDiscoveryElementCode("CURIOUS_ROCK");
        proposal.setDisplayName("Curious Rock");
        proposal.setElementType("CURiosity");
        proposal.setTrackingProposalId(999L);

        assertEquals("runtime-1", proposal.getProposalRuntimeId());
        assertEquals(999L, proposal.getTrackingProposalId());
    }
}
