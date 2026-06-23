package es.vargontoc.educational.framework.content.ports.out;

import es.vargontoc.educational.framework.content.model.Biome;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.WorldDiscoveryElement;
import java.util.List;
import java.util.Optional;

public interface WorldDiscoveryElementRepository {
    Optional<WorldDiscoveryElement> findByCode(String code);
    WorldDiscoveryElement save(WorldDiscoveryElement worldDiscoveryElement);
    boolean existsByCode(String code);
    List<WorldDiscoveryElement> findByStatusAndMinAgeLessThanEqualAndMaxAgeGreaterThanEqual(
        ContentStatus status, Integer targetAge);
    List<WorldDiscoveryElement> findByStatusAndBiomeAndMinAgeLessThanEqualAndMaxAgeGreaterThanEqual(
        ContentStatus status, Biome biome, Integer targetAge);
}
