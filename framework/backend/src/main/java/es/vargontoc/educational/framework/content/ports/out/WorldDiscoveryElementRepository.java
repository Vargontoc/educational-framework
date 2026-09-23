package es.vargontoc.educational.framework.content.ports.out;

import es.vargontoc.educational.framework.content.model.Biome;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.WorldDiscoveryElement;
import java.util.List;

public interface WorldDiscoveryElementRepository {
    List<WorldDiscoveryElement> findByStatusAndMinAgeLessThanEqualAndMaxAgeGreaterThanEqual(
        ContentStatus status, Integer targetAge);
    List<WorldDiscoveryElement> findByStatusAndBiomeAndMinAgeLessThanEqualAndMaxAgeGreaterThanEqual(
        ContentStatus status, Biome biome, Integer targetAge);
}
