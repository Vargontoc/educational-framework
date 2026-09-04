package es.vargontoc.educational.framework.world.service;

import es.vargontoc.educational.framework.tracking.model.ActivityProposalLogResult;
import es.vargontoc.educational.framework.tracking.model.ActivityProposalOutcome;
import es.vargontoc.educational.framework.tracking.ports.in.RegisterActivityProposalUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.ResolveActivityProposalUseCase;
import es.vargontoc.educational.framework.world.model.WorldActivityProposalResult;
import es.vargontoc.educational.framework.world.model.WorldProposalResolutionResult;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorldProposalServiceTest {

    @Mock
    private RegisterActivityProposalUseCase registerActivityProposalUseCase;

    @Mock
    private ResolveActivityProposalUseCase resolveActivityProposalUseCase;

    @Mock
    private WorldStateRegistry worldStateRegistry;

    private WorldProposalService worldProposalService;

    @BeforeEach
    void setUp() {
        worldProposalService = new WorldProposalService(
            registerActivityProposalUseCase,
            resolveActivityProposalUseCase,
            worldStateRegistry
        );
    }

    private WorldState createActiveWorldState(Long childSessionId) {
        WorldState state = new WorldState();
        state.setChildSessionId(childSessionId);
        state.setStatus(WorldRuntimeStatus.ACTIVE);
        state.setLastWorldActivityAt(LocalDateTime.now());
        state.setCreatedAt(LocalDateTime.now());
        state.setUpdatedAt(LocalDateTime.now());
        return state;
    }

    @Test
    void proposeActivity_registersProposalAndStoresId() {
        Long childSessionId = 100L;
        Long childProfileId = 1L;
        Long activityId = 10L;
        Long topicId = 5L;
        Long trackingProposalId = 999L;

        when(worldStateRegistry.findByChildSessionId(childSessionId)).thenReturn(Optional.of(createActiveWorldState(childSessionId)));
        when(worldStateRegistry.getPendingProposalId(childSessionId)).thenReturn(Optional.empty());
        when(registerActivityProposalUseCase.registerActivityProposal(childProfileId, childSessionId, activityId, topicId))
            .thenReturn(new ActivityProposalLogResult(trackingProposalId, LocalDateTime.now()));

        WorldActivityProposalResult result = worldProposalService.proposeActivity(childSessionId, childProfileId, activityId, topicId);

        assertNotNull(result);
        assertEquals(childSessionId, result.getChildSessionId());
        assertEquals(trackingProposalId, result.getTrackingProposalId());
        assertEquals(activityId, result.getActivityId());
        assertEquals(topicId, result.getTopicId());
        assertEquals(WorldActivityProposalResult.Status.PENDING, result.getStatus());
        assertNotNull(result.getProposalRuntimeId());

        verify(registerActivityProposalUseCase).registerActivityProposal(childProfileId, childSessionId, activityId, topicId);
    }

    @Test
    void proposeActivity_resolvesPreviousPendingFirst() {
        Long childSessionId = 100L;
        Long childProfileId = 1L;
        Long activityId = 10L;
        Long topicId = 5L;
        Long previousProposalId = 500L;

        WorldState existingState = createActiveWorldState(childSessionId);
        existingState.setPendingProposalId(previousProposalId);

        when(worldStateRegistry.findByChildSessionId(childSessionId)).thenReturn(Optional.of(existingState));
        when(worldStateRegistry.getPendingProposalId(childSessionId)).thenReturn(Optional.of(previousProposalId));
        when(registerActivityProposalUseCase.registerActivityProposal(childProfileId, childSessionId, activityId, topicId))
            .thenReturn(new ActivityProposalLogResult(999L, LocalDateTime.now()));

        worldProposalService.proposeActivity(childSessionId, childProfileId, activityId, topicId);

        verify(resolveActivityProposalUseCase).resolveActivityProposal(previousProposalId, ActivityProposalOutcome.IGNORED);
    }

    @Test
    void resolveProposal_asStarted() {
        Long childSessionId = 100L;
        Long proposalId = 999L;

        WorldState state = createActiveWorldState(childSessionId);
        state.setPendingProposalId(proposalId);

        when(worldStateRegistry.getPendingProposalId(childSessionId)).thenReturn(Optional.of(proposalId));
        when(worldStateRegistry.findByChildSessionId(childSessionId)).thenReturn(Optional.of(state));
        when(resolveActivityProposalUseCase.resolveActivityProposal(proposalId, ActivityProposalOutcome.STARTED))
            .thenReturn(new ActivityProposalLogResult(proposalId, LocalDateTime.now()));

        WorldProposalResolutionResult result = worldProposalService.resolveProposal(childSessionId, ActivityProposalOutcome.STARTED);

        assertNotNull(result);
        assertEquals(proposalId, result.getTrackingProposalId());
        assertEquals("STARTED", result.getOutcome());

        verify(resolveActivityProposalUseCase).resolveActivityProposal(proposalId, ActivityProposalOutcome.STARTED);
    }

    @Test
    void resolveProposal_asIgnored() {
        Long childSessionId = 100L;
        Long proposalId = 999L;

        WorldState state = createActiveWorldState(childSessionId);
        state.setPendingProposalId(proposalId);

        when(worldStateRegistry.getPendingProposalId(childSessionId)).thenReturn(Optional.of(proposalId));
        when(worldStateRegistry.findByChildSessionId(childSessionId)).thenReturn(Optional.of(state));
        when(resolveActivityProposalUseCase.resolveActivityProposal(proposalId, ActivityProposalOutcome.IGNORED))
            .thenReturn(new ActivityProposalLogResult(proposalId, LocalDateTime.now()));

        WorldProposalResolutionResult result = worldProposalService.resolveProposal(childSessionId, ActivityProposalOutcome.IGNORED);

        assertNotNull(result);
        assertEquals(proposalId, result.getTrackingProposalId());
        assertEquals("IGNORED", result.getOutcome());

        verify(resolveActivityProposalUseCase).resolveActivityProposal(proposalId, ActivityProposalOutcome.IGNORED);
    }

    @Test
    void resolveProposal_whenNoPendingProposal_returnsNullId() {
        Long childSessionId = 100L;

        when(worldStateRegistry.getPendingProposalId(childSessionId)).thenReturn(Optional.empty());

        WorldProposalResolutionResult result = worldProposalService.resolveProposal(childSessionId, ActivityProposalOutcome.IGNORED);

        assertNotNull(result);
        assertNull(result.getTrackingProposalId());
        assertEquals("IGNORED", result.getOutcome());

        verify(resolveActivityProposalUseCase, never()).resolveActivityProposal(any(), any());
    }

    @Test
    void resolveAndCloseWorld_clearsOnClose() {
        Long childSessionId = 100L;
        Long proposalId = 999L;

        WorldState state = createActiveWorldState(childSessionId);
        state.setPendingProposalId(proposalId);

        when(worldStateRegistry.getPendingProposalId(childSessionId)).thenReturn(Optional.of(proposalId));
        when(worldStateRegistry.removeByChildSessionId(childSessionId)).thenReturn(Optional.of(state));

        worldProposalService.resolveAndCloseWorld(childSessionId);

        verify(resolveActivityProposalUseCase).resolveActivityProposal(proposalId, ActivityProposalOutcome.IGNORED);
        verify(worldStateRegistry).removeByChildSessionId(childSessionId);
    }

    @Test
    void resolveAndCloseWorld_whenNoPendingProposal_stillRemovesState() {
        Long childSessionId = 100L;

        WorldState state = createActiveWorldState(childSessionId);

        when(worldStateRegistry.getPendingProposalId(childSessionId)).thenReturn(Optional.empty());
        when(worldStateRegistry.removeByChildSessionId(childSessionId)).thenReturn(Optional.of(state));

        worldProposalService.resolveAndCloseWorld(childSessionId);

        verify(resolveActivityProposalUseCase, never()).resolveActivityProposal(any(), any());
        verify(worldStateRegistry).removeByChildSessionId(childSessionId);
    }
}