package es.vargontoc.educational.framework.tracking.ports.out;

import es.vargontoc.educational.framework.tracking.model.GameSessionSummary;

import java.util.List;

public interface GameSessionSummaryRepository {

    GameSessionSummary save(GameSessionSummary summary);

    List<GameSessionSummary> findByChildProfileId(Long childProfileId);
}