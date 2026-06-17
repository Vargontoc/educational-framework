package es.vargontoc.educational.framework.tracking.infrastructure.adapter;

import es.vargontoc.educational.framework.content.model.DifficultyCode;
import es.vargontoc.educational.framework.content.ports.out.DifficultyLevelRepository;
import es.vargontoc.educational.framework.tracking.ports.out.DifficultyLevelNavigationPort;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class DifficultyLevelNavigationPortImpl implements DifficultyLevelNavigationPort {

    private final DifficultyLevelRepository difficultyLevelRepository;

    public DifficultyLevelNavigationPortImpl(DifficultyLevelRepository difficultyLevelRepository) {
        this.difficultyLevelRepository = difficultyLevelRepository;
    }

    @Override
    public List<Long> findOrderedDifficultyLevelIds(Long activityId) {
        return difficultyLevelRepository.findByActivityId(activityId)
                .stream()
                .sorted(Comparator.comparingInt(level -> codeOrder(level.getDifficultyCode())))
                .map(level -> level.getId())
                .toList();
    }

    private int codeOrder(DifficultyCode code) {
        return switch (code) {
            case EASY -> 1;
            case MEDIUM -> 2;
            case HARD -> 3;
        };
    }
}
