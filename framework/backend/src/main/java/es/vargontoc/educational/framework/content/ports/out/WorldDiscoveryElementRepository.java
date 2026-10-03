package es.vargontoc.educational.framework.content.ports.out;

import java.util.List;

import es.vargontoc.educational.framework.content.model.Biome;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.WorldDiscoveryElement;

public interface WorldDiscoveryElementRepository {
    List<WorldDiscoveryElement> findByStatusAndMinAgeLessThanEqualAndMaxAgeGreaterThanEqual(
        ContentStatus status, Integer targetAge);
    List<WorldDiscoveryElement> findByStatusAndBiomeAndMinAgeLessThanEqualAndMaxAgeGreaterThanEqual(
        ContentStatus status, Biome biome, Integer targetAge);

        void loadOnMemory();
}
