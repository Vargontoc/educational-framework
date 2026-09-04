package es.vargontoc.educational.framework.content.service;

import es.vargontoc.educational.framework.content.model.CompatibleActivityProjection;
import es.vargontoc.educational.framework.content.model.WorldDiscoveryElementProjection;
import es.vargontoc.educational.framework.content.model.WorldHostProjection;
import es.vargontoc.educational.framework.content.model.WorldNarrativeSituationProjection;
import es.vargontoc.educational.framework.content.model.Activity;
import es.vargontoc.educational.framework.content.model.Biome;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.WorldDiscoveryElement;
import es.vargontoc.educational.framework.content.model.WorldHost;
import es.vargontoc.educational.framework.content.model.WorldNarrativeSituation;
import es.vargontoc.educational.framework.content.ports.in.WorldCatalogUseCase;
import es.vargontoc.educational.framework.content.ports.out.ActivityRepository;
import es.vargontoc.educational.framework.content.ports.out.DifficultyLevelRepository;
import es.vargontoc.educational.framework.content.ports.out.WorldDiscoveryElementRepository;
import es.vargontoc.educational.framework.content.ports.out.WorldHostRepository;
import es.vargontoc.educational.framework.content.ports.out.WorldNarrativeSituationRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Transactional(readOnly = true)
public class WorldCatalogService implements WorldCatalogUseCase {

    private final WorldHostRepository worldHostRepository;
    private final WorldNarrativeSituationRepository worldNarrativeSituationRepository;
    private final WorldDiscoveryElementRepository worldDiscoveryElementRepository;
    private final ActivityRepository activityRepository;
    private final DifficultyLevelRepository difficultyLevelRepository;

    public WorldCatalogService(
            WorldHostRepository worldHostRepository,
            WorldNarrativeSituationRepository worldNarrativeSituationRepository,
            WorldDiscoveryElementRepository worldDiscoveryElementRepository,
            ActivityRepository activityRepository,
            DifficultyLevelRepository difficultyLevelRepository) {
        this.worldHostRepository = worldHostRepository;
        this.worldNarrativeSituationRepository = worldNarrativeSituationRepository;
        this.worldDiscoveryElementRepository = worldDiscoveryElementRepository;
        this.activityRepository = activityRepository;
        this.difficultyLevelRepository = difficultyLevelRepository;
    }

    @Override
    public List<WorldHostProjection> listActiveHostsForAge(Integer targetAge) {
        List<WorldHost> hosts = worldHostRepository.findByStatusAndMinAgeLessThanEqualAndMaxAgeGreaterThanEqual(
            ContentStatus.ACTIVE, targetAge);
        return hosts.stream().map(this::toWorldHostProjection).toList();
    }

    @Override
    public List<WorldNarrativeSituationProjection> listActiveSituationsForAge(Integer targetAge) {
        List<WorldNarrativeSituation> situations = worldNarrativeSituationRepository
            .findByStatusAndMinAgeLessThanEqualAndMaxAgeGreaterThanEqual(
                ContentStatus.ACTIVE, targetAge);
        return situations.stream().map(this::toWorldNarrativeSituationProjection).toList();
    }

    @Override
    public List<WorldDiscoveryElementProjection> listActiveElementsForAge(Integer targetAge) {
        List<WorldDiscoveryElement> elements = worldDiscoveryElementRepository
            .findByStatusAndMinAgeLessThanEqualAndMaxAgeGreaterThanEqual(
                ContentStatus.ACTIVE, targetAge);
        return elements.stream().map(this::toWorldDiscoveryElementProjection).toList();
    }

    @Override
    public List<WorldDiscoveryElementProjection> listActiveElementsByBiomeAndAge(Biome biome, Integer targetAge) {
        List<WorldDiscoveryElement> elements = worldDiscoveryElementRepository
            .findByStatusAndBiomeAndMinAgeLessThanEqualAndMaxAgeGreaterThanEqual(
                ContentStatus.ACTIVE, biome, targetAge);
        return elements.stream().map(this::toWorldDiscoveryElementProjection).toList();
    }

    @Override
    public List<CompatibleActivityProjection> listCompatibleActivitiesByTopic(Long topicId, Integer targetAge) {
        if (topicId == null) {
            return Collections.emptyList();
        }
        List<Activity> activities = activityRepository.findByStatusAndTopicId(topicId, ContentStatus.ACTIVE, targetAge);
        return activities.stream().map(this::toCompatibleActivityProjection).toList();
    }

    private WorldHostProjection toWorldHostProjection(WorldHost source) {
        return new WorldHostProjection(
            source.getId(),
            source.getCode(),
            source.getDisplayName(),
            source.getBiome(),
            source.getDescription(),
            source.getMinAge(),
            source.getMaxAge(),
            source.getVisualAssetKey(),
            source.getSortOrder()
        );
    }

    private WorldNarrativeSituationProjection toWorldNarrativeSituationProjection(WorldNarrativeSituation source) {
        return new WorldNarrativeSituationProjection(
            source.getId(),
            source.getCode(),
            source.getDisplayText(),
            source.getSituationType(),
            source.getTone(),
            source.getMinAge(),
            source.getMaxAge(),
            source.getSortOrder()
        );
    }

    private WorldDiscoveryElementProjection toWorldDiscoveryElementProjection(WorldDiscoveryElement source) {
        return new WorldDiscoveryElementProjection(
            source.getId(),
            source.getCode(),
            source.getDisplayName(),
            source.getElementType(),
            source.getBiome(),
            source.getMinAge(),
            source.getMaxAge(),
            source.getActivityId(),
            source.getTopicId(),
            source.getVisualAssetKey(),
            source.getInteractionCueType(),
            source.getSortOrder()
        );
    }

    private CompatibleActivityProjection toCompatibleActivityProjection(Activity source) {
        List<Long> difficultyLevelIds = difficultyLevelRepository.findByActivityId(source.getId())
            .stream()
            .map(dl -> dl.getId())
            .toList();

        return new CompatibleActivityProjection(
            source.getId(),
            source.getName(),
            source.getGameEngineType(),
            source.getTopicIds(),
            source.getMinAge(),
            source.getMaxAge(),
            difficultyLevelIds
        );
    }
}
