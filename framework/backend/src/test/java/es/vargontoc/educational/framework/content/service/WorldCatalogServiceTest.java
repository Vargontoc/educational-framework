package es.vargontoc.educational.framework.content.service;

import es.vargontoc.educational.framework.content.infrastructure.dto.CompatibleActivityProjection;
import es.vargontoc.educational.framework.content.infrastructure.dto.WorldDiscoveryElementProjection;
import es.vargontoc.educational.framework.content.infrastructure.dto.WorldHostProjection;
import es.vargontoc.educational.framework.content.infrastructure.dto.WorldNarrativeSituationProjection;
import es.vargontoc.educational.framework.content.model.Activity;
import es.vargontoc.educational.framework.content.model.Biome;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.DifficultyLevel;
import es.vargontoc.educational.framework.content.model.ElementType;
import es.vargontoc.educational.framework.content.model.WorldDiscoveryElement;
import es.vargontoc.educational.framework.content.model.WorldHost;
import es.vargontoc.educational.framework.content.model.WorldNarrativeSituation;
import es.vargontoc.educational.framework.content.ports.out.ActivityRepository;
import es.vargontoc.educational.framework.content.ports.out.DifficultyLevelRepository;
import es.vargontoc.educational.framework.content.ports.out.WorldDiscoveryElementRepository;
import es.vargontoc.educational.framework.content.ports.out.WorldHostRepository;
import es.vargontoc.educational.framework.content.ports.out.WorldNarrativeSituationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorldCatalogServiceTest {

    @Mock
    private WorldHostRepository worldHostRepository;

    @Mock
    private WorldNarrativeSituationRepository worldNarrativeSituationRepository;

    @Mock
    private WorldDiscoveryElementRepository worldDiscoveryElementRepository;

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private DifficultyLevelRepository difficultyLevelRepository;

    private WorldCatalogService service;

    @BeforeEach
    void setUp() {
        service = new WorldCatalogService(
            worldHostRepository,
            worldNarrativeSituationRepository,
            worldDiscoveryElementRepository,
            activityRepository,
            difficultyLevelRepository
        );
    }

    @Test
    void listActiveHostsForAge_returnsOnlyMatchingAge() {
        WorldHost host = createWorldHost(1L, "MEADOW_DOG", Biome.MEADOW, 3, 4);
        when(worldHostRepository.findByStatusAndMinAgeLessThanEqualAndMaxAgeGreaterThanEqual(
            ContentStatus.ACTIVE, 3)).thenReturn(List.of(host));

        List<WorldHostProjection> result = service.listActiveHostsForAge(3);

        assertEquals(1, result.size());
        assertEquals("MEADOW_DOG", result.get(0).code());
        assertEquals(Biome.MEADOW, result.get(0).biome());
    }

    @Test
    void listActiveHostsForAge_excludesInactive() {
        when(worldHostRepository.findByStatusAndMinAgeLessThanEqualAndMaxAgeGreaterThanEqual(
            ContentStatus.ACTIVE, 3)).thenReturn(Collections.emptyList());

        List<WorldHostProjection> result = service.listActiveHostsForAge(3);

        assertTrue(result.isEmpty());
    }

    @Test
    void listActiveSituationsForAge_returnsOnlyMatchingAge() {
        WorldNarrativeSituation situation = createWorldNarrativeSituation(1L, "HOST_FOUND_SOMETHING", 3, 4);
        when(worldNarrativeSituationRepository.findByStatusAndMinAgeLessThanEqualAndMaxAgeGreaterThanEqual(
            ContentStatus.ACTIVE, 3)).thenReturn(List.of(situation));

        List<WorldNarrativeSituationProjection> result = service.listActiveSituationsForAge(3);

        assertEquals(1, result.size());
        assertEquals("HOST_FOUND_SOMETHING", result.get(0).code());
    }

    @Test
    void listActiveElementsByBiomeAndAge_combinesFilters() {
        WorldDiscoveryElement element = createWorldDiscoveryElement(1L, "MEADOW_SHINY_FLOWER", Biome.MEADOW, ElementType.DISCOVERY, 3, 4);
        when(worldDiscoveryElementRepository.findByStatusAndBiomeAndMinAgeLessThanEqualAndMaxAgeGreaterThanEqual(
            ContentStatus.ACTIVE, Biome.MEADOW, 3)).thenReturn(List.of(element));

        List<WorldDiscoveryElementProjection> result = service.listActiveElementsByBiomeAndAge(Biome.MEADOW, 3);

        assertEquals(1, result.size());
        assertEquals("MEADOW_SHINY_FLOWER", result.get(0).code());
        assertEquals(Biome.MEADOW, result.get(0).biome());
        assertEquals(ElementType.DISCOVERY, result.get(0).elementType());
    }

    @Test
    void listCompatibleActivitiesByTopic_returnsEmptyList_whenNoMatch() {
        when(activityRepository.findByStatusAndTopicId(1L, ContentStatus.ACTIVE))
            .thenReturn(Collections.emptyList());

        List<CompatibleActivityProjection> result = service.listCompatibleActivitiesByTopic(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void listCompatibleActivitiesByTopic_returnsMatchingActivities() {
        Activity activity = createActivity(1L, "Counting Game", "SIMPLE", 3, 4);
        DifficultyLevel difficultyLevel = createDifficultyLevel(1L, 1L);
        when(activityRepository.findByStatusAndTopicId(1L, ContentStatus.ACTIVE))
            .thenReturn(List.of(activity));
        when(difficultyLevelRepository.findByActivityId(1L))
            .thenReturn(List.of(difficultyLevel));

        List<CompatibleActivityProjection> result = service.listCompatibleActivitiesByTopic(1L);

        assertEquals(1, result.size());
        assertEquals("Counting Game", result.get(0).displayName());
        assertEquals("SIMPLE", result.get(0).gameEngineType());
        assertEquals(1, result.get(0).difficultyLevelIds().size());
    }

    @Test
    void listCompatibleActivitiesByTopic_returnsEmptyList_whenTopicIdIsNull() {
        List<CompatibleActivityProjection> result = service.listCompatibleActivitiesByTopic(null);

        assertTrue(result.isEmpty());
    }

    private WorldHost createWorldHost(Long id, String code, Biome biome, Integer minAge, Integer maxAge) {
        WorldHost host = new WorldHost();
        host.setId(id);
        host.setCode(code);
        host.setDisplayName("Dog");
        host.setBiome(biome);
        host.setMinAge(minAge);
        host.setMaxAge(maxAge);
        host.setStatus(ContentStatus.ACTIVE);
        return host;
    }

    private WorldNarrativeSituation createWorldNarrativeSituation(Long id, String code, Integer minAge, Integer maxAge) {
        WorldNarrativeSituation situation = new WorldNarrativeSituation();
        situation.setId(id);
        situation.setCode(code);
        situation.setDisplayText("Found something!");
        situation.setSituationType(es.vargontoc.educational.framework.content.model.SituationType.FOUND_OBJECT);
        situation.setMinAge(minAge);
        situation.setMaxAge(maxAge);
        situation.setStatus(ContentStatus.ACTIVE);
        return situation;
    }

    private WorldDiscoveryElement createWorldDiscoveryElement(Long id, String code, Biome biome, ElementType elementType, Integer minAge, Integer maxAge) {
        WorldDiscoveryElement element = new WorldDiscoveryElement();
        element.setId(id);
        element.setCode(code);
        element.setDisplayName("Shiny Flower");
        element.setBiome(biome);
        element.setElementType(elementType);
        element.setMinAge(minAge);
        element.setMaxAge(maxAge);
        element.setStatus(ContentStatus.ACTIVE);
        return element;
    }

    private Activity createActivity(Long id, String name, String engineType, Integer minAge, Integer maxAge) {
        Activity activity = new Activity();
        activity.setId(id);
        activity.setName(name);
        activity.setGameEngineType(engineType);
        activity.setMinAge(minAge);
        activity.setMaxAge(maxAge);
        activity.setStatus(ContentStatus.ACTIVE);
        activity.setTopicIds(List.of(1L));
        return activity;
    }

    private DifficultyLevel createDifficultyLevel(Long id, Long activityId) {
        DifficultyLevel level = new DifficultyLevel();
        level.setId(id);
        level.setActivityId(activityId);
        level.setDifficultyCode(es.vargontoc.educational.framework.content.model.DifficultyCode.EASY);
        return level;
    }
}
