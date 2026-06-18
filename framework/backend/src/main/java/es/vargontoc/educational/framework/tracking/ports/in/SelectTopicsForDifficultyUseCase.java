package es.vargontoc.educational.framework.tracking.ports.in;

import es.vargontoc.educational.framework.tracking.model.TopicSelectionResult;
import es.vargontoc.educational.framework.tracking.model.DifficultyLevel;

public interface SelectTopicsForDifficultyUseCase {

    TopicSelectionResult selectTopicsForDifficulty(Long childProfileId, DifficultyLevel difficulty, Integer count);
}
