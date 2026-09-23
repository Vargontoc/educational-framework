package es.vargontoc.educational.framework.tracking.ports.out;

import es.vargontoc.educational.framework.tracking.model.GameSessionSummary;

import java.time.LocalDateTime;
import java.util.List;

public interface GameSessionSummaryRepository {

    GameSessionSummary save(GameSessionSummary summary);

    List<GameSessionSummary> findByChildProfileId(Long childProfileId);

    List<GameSessionSummary> findByChildProfileIdAndStartedAtBetween(Long childProfileId, LocalDateTime start, LocalDateTime end);

    List<GameSessionSummary> findRecentInitialAbandonmentsByChildAndActivity(Long childProfileId, Long activityId, int limit);
}