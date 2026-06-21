package es.vargontoc.educational.framework.content.service;

import es.vargontoc.educational.framework.content.model.DifficultyCode;
import es.vargontoc.educational.framework.content.model.DifficultyLevel;
import es.vargontoc.educational.framework.content.ports.in.DifficultyLevelUseCase;
import es.vargontoc.educational.framework.content.ports.out.ActivityRepository;
import es.vargontoc.educational.framework.content.ports.out.DifficultyLevelRepository;
import es.vargontoc.educational.framework.content.validation.DifficultyLevelValidator;
import es.vargontoc.educational.framework.shared.exception.ContentNotReadyException;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Transactional
public class DifficultyLevelService implements DifficultyLevelUseCase {

    private final DifficultyLevelRepository difficultyLevelRepository;
    private final ActivityRepository activityRepository;
    private final DifficultyLevelValidator difficultyLevelValidator;

    public DifficultyLevelService(DifficultyLevelRepository difficultyLevelRepository, ActivityRepository activityRepository) {
        this.difficultyLevelRepository = difficultyLevelRepository;
        this.activityRepository = activityRepository;
        this.difficultyLevelValidator = new DifficultyLevelValidator();
    }

    @Override
    public DifficultyLevel createDifficultyLevel(Long activityId, DifficultyCode difficultyCode, String engineParams, String adaptiveThresholdConfig) {
        difficultyLevelValidator.validateForCreate(activityId, difficultyCode);

        if (!activityRepository.findById(activityId).isPresent()) {
            throw new ResourceNotFoundException("Activity not found with id: " + activityId);
        }

        var difficultyLevel = new DifficultyLevel();
        difficultyLevel.setActivityId(activityId);
        difficultyLevel.setDifficultyCode(difficultyCode);
        difficultyLevel.setEngineParams(engineParams);
        difficultyLevel.setAdaptiveThresholdConfig(adaptiveThresholdConfig);
        difficultyLevel.setCreatedAt(LocalDateTime.now());

        return difficultyLevelRepository.save(difficultyLevel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DifficultyLevel> listByActivity(Long activityId) {
        return difficultyLevelRepository.findByActivityId(activityId);
    }

    @Override
    public DifficultyLevel updateDifficultyLevel(Long id, DifficultyCode difficultyCode, String engineParams, String adaptiveThresholdConfig) {
        var existing = difficultyLevelRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("DifficultyLevel not found with id: " + id));

        difficultyLevelValidator.validateForUpdate(existing.getActivityId(), difficultyCode);

        existing.setDifficultyCode(difficultyCode);
        existing.setEngineParams(engineParams);
        existing.setAdaptiveThresholdConfig(adaptiveThresholdConfig);
        existing.setUpdatedAt(LocalDateTime.now());

        return difficultyLevelRepository.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public DifficultyLevel getGameReadyDifficultyLevel(Long difficultyLevelId) {
        return difficultyLevelRepository.findByIdAndEngineParamsIsNotNull(difficultyLevelId)
            .orElseThrow(() -> new ContentNotReadyException("DifficultyLevel is not ready for game: " + difficultyLevelId));
    }

    @Override
    @Transactional(readOnly = true)
    public DifficultyLevel getEasiestDifficultyLevel(Long activityId) {
        return difficultyLevelRepository.findFirstByActivityIdOrderByDifficultyCodeAsc(activityId)
            .orElseThrow(() -> new ContentNotReadyException("No difficulty level found for activity: " + activityId));
    }
}
