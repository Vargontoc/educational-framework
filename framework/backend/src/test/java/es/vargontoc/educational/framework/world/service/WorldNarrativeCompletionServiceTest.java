package es.vargontoc.educational.framework.world.service;

import es.vargontoc.educational.framework.tracking.model.ChildLearningProgressResponse;
import es.vargontoc.educational.framework.tracking.ports.in.RegisterLearningPathStepProgressUseCase;
import es.vargontoc.educational.framework.world.model.WorldNarrativeCompletionResult;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorldNarrativeCompletionServiceTest {

    @Mock
    private WorldStateRegistry worldStateRegistry;

    @Mock
    private RegisterLearningPathStepProgressUseCase registerLearningPathStepProgressUseCase;

    private WorldNarrativeCompletionService service;

    @BeforeEach
    void setUp() {
        service = new WorldNarrativeCompletionService(worldStateRegistry, registerLearningPathStepProgressUseCase);
    }

    private WorldState createWorldState(Long childSessionId, Long childProfileId) {
        WorldState state = new WorldState();
        state.setChildSessionId(childSessionId);
        state.setChildProfileId(childProfileId);
        state.setStatus(WorldRuntimeStatus.ACTIVE);
        state.setNarrativeCompletionStatus(WorldNarrativeCompletionStatus.AWAITING_NARRATIVE);
        state.setLastWorldActivityAt(LocalDateTime.now());
        state.setCreatedAt(LocalDateTime.now());
        state.setUpdatedAt(LocalDateTime.now());
        return state;
    }

    @Test
    void markNarrativeComplete_withAwaitingAdvances() {
        Long childSessionId = 100L;
        Long childProfileId = 1L;
        Long learningPathId = 10L;
        Long learningPathStepId = 5L;

        WorldState worldState = createWorldState(childSessionId, childProfileId);

        when(worldStateRegistry.findByChildSessionId(childSessionId)).thenReturn(Optional.of(worldState));
        when(registerLearningPathStepProgressUseCase.registerLearningPathStepProgress(eq(childProfileId), eq(learningPathId), eq(learningPathStepId)))
            .thenReturn(new ChildLearningProgressResponse());

        WorldNarrativeCompletionResult result = service.markNarrativeComplete(childSessionId, learningPathId, learningPathStepId);

        assertNotNull(result);
        assertTrue(result.isProgressAdvanced());
        assertEquals(WorldNarrativeCompletionStatus.NO_PENDING, result.getStatus());
        verify(registerLearningPathStepProgressUseCase).registerLearningPathStepProgress(childProfileId, learningPathId, learningPathStepId);
    }

    @Test
    void markNarrativeComplete_withoutAwaitingNoOps() {
        Long childSessionId = 100L;
        Long learningPathId = 10L;
        Long learningPathStepId = 5L;

        WorldState worldState = createWorldState(childSessionId, 1L);
        worldState.setNarrativeCompletionStatus(WorldNarrativeCompletionStatus.NO_PENDING);

        when(worldStateRegistry.findByChildSessionId(childSessionId)).thenReturn(Optional.of(worldState));

        WorldNarrativeCompletionResult result = service.markNarrativeComplete(childSessionId, learningPathId, learningPathStepId);

        assertNotNull(result);
        assertFalse(result.isProgressAdvanced());
        assertEquals(WorldNarrativeCompletionStatus.NO_PENDING, result.getStatus());
    }

    @Test
    void markNarrativeComplete_clearsStatusAfterAdvance() {
        Long childSessionId = 100L;
        Long childProfileId = 1L;
        Long learningPathId = 10L;
        Long learningPathStepId = 5L;

        WorldState worldState = createWorldState(childSessionId, childProfileId);

        when(worldStateRegistry.findByChildSessionId(childSessionId)).thenReturn(Optional.of(worldState));
        when(registerLearningPathStepProgressUseCase.registerLearningPathStepProgress(any(), any(), any()))
            .thenReturn(new ChildLearningProgressResponse());

        service.markNarrativeComplete(childSessionId, learningPathId, learningPathStepId);

        assertEquals(WorldNarrativeCompletionStatus.NO_PENDING, worldState.getNarrativeCompletionStatus());
    }

    @Test
    void markNarrativeComplete_usesChildProfileFromState() {
        Long childSessionId = 100L;
        Long childProfileId = 99L;
        Long learningPathId = 10L;
        Long learningPathStepId = 5L;

        WorldState worldState = createWorldState(childSessionId, childProfileId);

        when(worldStateRegistry.findByChildSessionId(childSessionId)).thenReturn(Optional.of(worldState));
        when(registerLearningPathStepProgressUseCase.registerLearningPathStepProgress(eq(childProfileId), eq(learningPathId), eq(learningPathStepId)))
            .thenReturn(new ChildLearningProgressResponse());

        service.markNarrativeComplete(childSessionId, learningPathId, learningPathStepId);

        verify(registerLearningPathStepProgressUseCase).registerLearningPathStepProgress(childProfileId, learningPathId, learningPathStepId);
    }

    @Test
    void markNarrativeComplete_worldStateNotFound() {
        Long childSessionId = 100L;
        Long learningPathId = 10L;
        Long learningPathStepId = 5L;

        when(worldStateRegistry.findByChildSessionId(childSessionId)).thenReturn(Optional.empty());

        WorldNarrativeCompletionResult result = service.markNarrativeComplete(childSessionId, learningPathId, learningPathStepId);

        assertNotNull(result);
        assertFalse(result.isProgressAdvanced());
        assertEquals(WorldNarrativeCompletionStatus.NO_PENDING, result.getStatus());
    }
}