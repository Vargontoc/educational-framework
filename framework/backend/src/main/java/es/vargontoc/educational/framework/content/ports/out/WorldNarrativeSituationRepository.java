package es.vargontoc.educational.framework.content.ports.out;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.WorldNarrativeSituation;
import java.util.List;
import java.util.Optional;

public interface WorldNarrativeSituationRepository {
    Optional<WorldNarrativeSituation> findByCode(String code);
    WorldNarrativeSituation save(WorldNarrativeSituation worldNarrativeSituation);
    boolean existsByCode(String code);
    List<WorldNarrativeSituation> findByStatusAndMinAgeLessThanEqualAndMaxAgeGreaterThanEqual(
        ContentStatus status, Integer targetAge);
}
