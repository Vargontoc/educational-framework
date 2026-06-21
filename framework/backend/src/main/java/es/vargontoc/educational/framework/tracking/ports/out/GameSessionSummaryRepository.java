package es.vargontoc.educational.framework.tracking.ports.out;

import es.vargontoc.educational.framework.tracking.model.GameSessionSummary;

public interface GameSessionSummaryRepository {

    GameSessionSummary save(GameSessionSummary summary);
}