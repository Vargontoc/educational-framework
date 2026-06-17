package es.vargontoc.educational.framework.tracking.infrastructure.adapter;

import es.vargontoc.educational.framework.content.ports.out.DifficultyLevelRepository;
import es.vargontoc.educational.framework.tracking.ports.out.DifficultyLevelConfigPort;
import org.springframework.stereotype.Component;

@Component
public class DifficultyLevelConfigPortImpl implements DifficultyLevelConfigPort {

    private final DifficultyLevelRepository difficultyLevelRepository;

    public DifficultyLevelConfigPortImpl(DifficultyLevelRepository difficultyLevelRepository) {
        this.difficultyLevelRepository = difficultyLevelRepository;
    }

    @Override
    public String getAdaptiveThresholdConfig(Long difficultyLevelId) {
        if (difficultyLevelId == null) {
            return null;
        }
        return difficultyLevelRepository.findById(difficultyLevelId)
                .map(config -> config.getAdaptiveThresholdConfig())
                .orElse(null);
    }
}
