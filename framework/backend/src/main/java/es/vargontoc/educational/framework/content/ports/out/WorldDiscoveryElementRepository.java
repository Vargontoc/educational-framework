package es.vargontoc.educational.framework.content.ports.out;

import es.vargontoc.educational.framework.content.model.WorldDiscoveryElement;
import java.util.Optional;

public interface WorldDiscoveryElementRepository {
    Optional<WorldDiscoveryElement> findByCode(String code);
    WorldDiscoveryElement save(WorldDiscoveryElement worldDiscoveryElement);
    boolean existsByCode(String code);
}
