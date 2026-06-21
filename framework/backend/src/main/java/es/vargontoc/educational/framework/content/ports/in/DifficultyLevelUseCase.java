package es.vargontoc.educational.framework.content.ports.in;

import es.vargontoc.educational.framework.content.model.DifficultyCode;
import es.vargontoc.educational.framework.content.model.DifficultyLevel;

import java.util.List;

public interface DifficultyLevelUseCase {

    DifficultyLevel createDifficultyLevel(Long activityId, DifficultyCode difficultyCode, String engineParams, String adaptiveThresholdConfig);

    List<DifficultyLevel> listByActivity(Long activityId);

    DifficultyLevel updateDifficultyLevel(Long id, DifficultyCode difficultyCode, String engineParams, String adaptiveThresholdConfig);

    DifficultyLevel getGameReadyDifficultyLevel(Long difficultyLevelId);

    DifficultyLevel getEasiestDifficultyLevel(Long activityId);
}
