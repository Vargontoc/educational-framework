package es.vargontoc.educational.framework.content.ports.out;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.WorldHost;
import java.util.List;
import java.util.Optional;

public interface WorldHostRepository {
    Optional<WorldHost> findByCode(String code);
    WorldHost save(WorldHost worldHost);
    boolean existsByCode(String code);
    List<WorldHost> findByStatusAndMinAgeLessThanEqualAndMaxAgeGreaterThanEqual(
        ContentStatus status, Integer targetAge);
}
