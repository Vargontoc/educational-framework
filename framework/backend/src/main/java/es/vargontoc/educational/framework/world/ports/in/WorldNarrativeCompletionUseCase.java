package es.vargontoc.educational.framework.world.ports.in;

import es.vargontoc.educational.framework.world.model.WorldNarrativeCompletionResult;

public interface WorldNarrativeCompletionUseCase {

    WorldNarrativeCompletionResult markNarrativeComplete(Long childSessionId, Long learningPathId, Long learningPathStepId);
}