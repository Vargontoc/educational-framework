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
import es.vargontoc.educational.framework.shared.infrastructure.GameCacheStorage;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public class WorldCatalogService implements WorldCatalogUseCase {

    private final WorldHostRepository worldHostRepository;
    private final WorldNarrativeSituationRepository worldNarrativeSituationRepository;
    private final WorldDiscoveryElementRepository worldDiscoveryElementRepository;
    private final ActivityRepository activityRepository;
    private final DifficultyLevelRepository difficultyLevelRepository;
    private final GameCacheStorage gameCacheStorage;

    public WorldCatalogService(
            WorldHostRepository worldHostRepository,
            WorldNarrativeSituationRepository worldNarrativeSituationRepository,
            WorldDiscoveryElementRepository worldDiscoveryElementRepository,
            ActivityRepository activityRepository,
            DifficultyLevelRepository difficultyLevelRepository,
            GameCacheStorage gameCacheStorage) {
        this.worldHostRepository = worldHostRepository;
        this.worldNarrativeSituationRepository = worldNarrativeSituationRepository;
        this.worldDiscoveryElementRepository = worldDiscoveryElementRepository;
        this.activityRepository = activityRepository;
        this.difficultyLevelRepository = difficultyLevelRepository;
        this.gameCacheStorage = gameCacheStorage;
    }

    @Override
    public List<WorldHostProjection> listActiveHostsForAge(Integer targetAge) {
        String cacheKey = "hosts:age=" + targetAge;
        Optional<List<WorldHostProjection>> cached = gameCacheStorage.getWorldCatalog(cacheKey);
        if (cached.isPresent()) {
            return cached.get();
        }
        List<WorldHost> hosts = worldHostRepository.findByStatusAndMinAgeLessThanEqualAndMaxAgeGreaterThanEqual(
            ContentStatus.ACTIVE, targetAge);
        List<WorldHostProjection> result = hosts.stream().map(this::toWorldHostProjection).toList();
        gameCacheStorage.putWorldCatalog(cacheKey, result);
        return result;
    }

    @Override
    public List<WorldNarrativeSituationProjection> listActiveSituationsForAge(Integer targetAge) {
        String cacheKey = "situations:age=" + targetAge;
        Optional<List<WorldNarrativeSituationProjection>> cached = gameCacheStorage.getWorldCatalog(cacheKey);
        if (cached.isPresent()) {
            return cached.get();
        }
        List<WorldNarrativeSituation> situations = worldNarrativeSituationRepository
            .findByStatusAndMinAgeLessThanEqualAndMaxAgeGreaterThanEqual(
                ContentStatus.ACTIVE, targetAge);
        List<WorldNarrativeSituationProjection> result = situations.stream().map(this::toWorldNarrativeSituationProjection).toList();
        gameCacheStorage.putWorldCatalog(cacheKey, result);
        return result;
    }

    @Override
    public List<WorldDiscoveryElementProjection> listActiveElementsForAge(Integer targetAge) {
        String cacheKey = "elements:age=" + targetAge;
        Optional<List<WorldDiscoveryElementProjection>> cached = gameCacheStorage.getWorldCatalog(cacheKey);
        if (cached.isPresent()) {
            return cached.get();
        }
        List<WorldDiscoveryElement> elements = worldDiscoveryElementRepository
            .findByStatusAndMinAgeLessThanEqualAndMaxAgeGreaterThanEqual(
                ContentStatus.ACTIVE, targetAge);
        List<WorldDiscoveryElementProjection> result = elements.stream().map(this::toWorldDiscoveryElementProjection).toList();
        gameCacheStorage.putWorldCatalog(cacheKey, result);
        return result;
    }

    @Override
    public List<WorldDiscoveryElementProjection> listActiveElementsByBiomeAndAge(Biome biome, Integer targetAge) {
        String cacheKey = "elements:biome=" + biome.name() + ":age=" + targetAge;
        Optional<List<WorldDiscoveryElementProjection>> cached = gameCacheStorage.getWorldCatalog(cacheKey);
        if (cached.isPresent()) {
            return cached.get();
        }
        List<WorldDiscoveryElement> elements = worldDiscoveryElementRepository
            .findByStatusAndBiomeAndMinAgeLessThanEqualAndMaxAgeGreaterThanEqual(
                ContentStatus.ACTIVE, biome, targetAge);
        List<WorldDiscoveryElementProjection> result = elements.stream().map(this::toWorldDiscoveryElementProjection).toList();
        gameCacheStorage.putWorldCatalog(cacheKey, result);
        return result;
    }

    @Override
    public List<CompatibleActivityProjection> listCompatibleActivitiesByTopic(Long topicId, Integer targetAge) {
        if (topicId == null) {
            return Collections.emptyList();
        }
        String cacheKey = "activities:topic=" + topicId + ":age=" + targetAge;
        Optional<List<CompatibleActivityProjection>> cached = gameCacheStorage.getWorldCatalog(cacheKey);
        if (cached.isPresent()) {
            return cached.get();
        }
        List<Activity> activities = activityRepository.findByStatusAndTopicId(topicId, ContentStatus.ACTIVE, targetAge);
        List<CompatibleActivityProjection> result = activities.stream().map(this::toCompatibleActivityProjection).toList();
        gameCacheStorage.putWorldCatalog(cacheKey, result);
        return result;
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
            source.getSortOrder(),
            source.getWorldWidth()
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
            source.getSortOrder(),
            source.getPositionX(),
            source.getPositionY()
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
