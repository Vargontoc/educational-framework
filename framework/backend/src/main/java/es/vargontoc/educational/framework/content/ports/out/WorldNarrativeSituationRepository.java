package es.vargontoc.educational.framework.content.ports.out;

import es.vargontoc.educational.framework.content.model.WorldNarrativeSituation;
import java.util.Optional;

public interface WorldNarrativeSituationRepository {
    Optional<WorldNarrativeSituation> findByCode(String code);
    WorldNarrativeSituation save(WorldNarrativeSituation worldNarrativeSituation);
    boolean existsByCode(String code);
}
