package es.vargontoc.educational.framework.content.ports.in;

import es.vargontoc.educational.framework.content.infrastructure.dto.CompatibleActivityProjection;
import es.vargontoc.educational.framework.content.infrastructure.dto.WorldDiscoveryElementProjection;
import es.vargontoc.educational.framework.content.infrastructure.dto.WorldHostProjection;
import es.vargontoc.educational.framework.content.infrastructure.dto.WorldNarrativeSituationProjection;
import es.vargontoc.educational.framework.content.model.Biome;

import java.util.List;

public interface WorldCatalogUseCase {

    List<WorldHostProjection> listActiveHostsForAge(Integer targetAge);

    List<WorldNarrativeSituationProjection> listActiveSituationsForAge(Integer targetAge);

    List<WorldDiscoveryElementProjection> listActiveElementsForAge(Integer targetAge);

    List<WorldDiscoveryElementProjection> listActiveElementsByBiomeAndAge(Biome biome, Integer targetAge);

    List<CompatibleActivityProjection> listCompatibleActivitiesByTopic(Long topicId);
}
