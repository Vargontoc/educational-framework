package es.vargontoc.educational.framework.tracking.ports.out;

import java.util.List;

public interface DifficultyLevelNavigationPort {

    List<Long> findOrderedDifficultyLevelIds(Long activityId);
}
