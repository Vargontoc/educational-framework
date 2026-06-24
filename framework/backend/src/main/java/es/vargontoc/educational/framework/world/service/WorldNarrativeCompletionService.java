package es.vargontoc.educational.framework.world.service;

import es.vargontoc.educational.framework.tracking.model.ChildLearningProgressResponse;
import es.vargontoc.educational.framework.tracking.ports.in.RegisterLearningPathStepProgressUseCase;
import es.vargontoc.educational.framework.world.model.WorldNarrativeCompletionResult;
import es.vargontoc.educational.framework.world.model.WorldNarrativeCompletionStatus;
import es.vargontoc.educational.framework.world.model.WorldState;
import es.vargontoc.educational.framework.world.ports.in.WorldNarrativeCompletionUseCase;
import es.vargontoc.educational.framework.world.ports.out.WorldStateRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class WorldNarrativeCompletionService implements WorldNarrativeCompletionUseCase {

    private static final Logger log = LoggerFactory.getLogger(WorldNarrativeCompletionService.class);

    private final WorldStateRegistry worldStateRegistry;
    private final RegisterLearningPathStepProgressUseCase registerLearningPathStepProgressUseCase;

    public WorldNarrativeCompletionService(WorldStateRegistry worldStateRegistry,
                                        RegisterLearningPathStepProgressUseCase registerLearningPathStepProgressUseCase) {
        this.worldStateRegistry = worldStateRegistry;
        this.registerLearningPathStepProgressUseCase = registerLearningPathStepProgressUseCase;
    }

    @Override
    public WorldNarrativeCompletionResult markNarrativeComplete(Long childSessionId, Long learningPathId, Long learningPathStepId) {
        Optional<WorldState> worldStateOpt = worldStateRegistry.findByChildSessionId(childSessionId);

        if (worldStateOpt.isEmpty()) {
            log.warn("WorldState not found for childSessionId={}", childSessionId);
            return new WorldNarrativeCompletionResult(childSessionId, learningPathId, learningPathStepId, false, WorldNarrativeCompletionStatus.NO_PENDING);
        }

        WorldState worldState = worldStateOpt.get();

        if (worldState.getNarrativeCompletionStatus() != WorldNarrativeCompletionStatus.AWAITING_NARRATIVE) {
            log.debug("No narrative pending for childSessionId={}, status={}", childSessionId, worldState.getNarrativeCompletionStatus());
            return new WorldNarrativeCompletionResult(childSessionId, learningPathId, learningPathStepId, false, worldState.getNarrativeCompletionStatus());
        }

        Long childProfileId = worldState.getChildProfileId();

        ChildLearningProgressResponse progressResponse = registerLearningPathStepProgressUseCase.registerLearningPathStepProgress(
            childProfileId, learningPathId, learningPathStepId);

        worldState.setNarrativeCompletionStatus(WorldNarrativeCompletionStatus.NO_PENDING);
        worldStateRegistry.save(worldState);

        log.info("Narrative completed for childSessionId={}, LearningPath progress advanced", childSessionId);

        return new WorldNarrativeCompletionResult(childSessionId, learningPathId, learningPathStepId, true, WorldNarrativeCompletionStatus.NO_PENDING);
    }
}