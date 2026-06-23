package es.vargontoc.educational.framework.content.ports.in;

import es.vargontoc.educational.framework.content.model.Biome;
import es.vargontoc.educational.framework.content.model.CompatibleActivityProjection;
import es.vargontoc.educational.framework.content.model.WorldDiscoveryElementProjection;
import es.vargontoc.educational.framework.content.model.WorldHostProjection;
import es.vargontoc.educational.framework.content.model.WorldNarrativeSituationProjection;

import java.util.List;

public interface WorldCatalogUseCase {

    List<WorldHostProjection> listActiveHostsForAge(Integer targetAge);

    List<WorldNarrativeSituationProjection> listActiveSituationsForAge(Integer targetAge);

    List<WorldDiscoveryElementProjection> listActiveElementsForAge(Integer targetAge);

    List<WorldDiscoveryElementProjection> listActiveElementsByBiomeAndAge(Biome biome, Integer targetAge);

    List<CompatibleActivityProjection> listCompatibleActivitiesByTopic(Long topicId, Integer targetAge);
}
