package es.vargontoc.educational.framework.content.infrastructure.seed;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.vargontoc.educational.framework.content.infrastructure.persistence.DevSeedStateJpaEntity;
import es.vargontoc.educational.framework.content.infrastructure.persistence.DevSeedStateJpaRepository;
import es.vargontoc.educational.framework.content.model.Category;
import es.vargontoc.educational.framework.content.model.Topic;
import es.vargontoc.educational.framework.content.ports.out.ActivityRepository;
import es.vargontoc.educational.framework.content.ports.out.AvatarEventCatalogRepository;
import es.vargontoc.educational.framework.content.ports.out.CategoryRepository;
import es.vargontoc.educational.framework.content.ports.out.CuriosityRepository;
import es.vargontoc.educational.framework.content.ports.out.DifficultyLevelRepository;
import es.vargontoc.educational.framework.content.ports.out.LearningPathRepository;
import es.vargontoc.educational.framework.content.ports.out.LearningPathStepRepository;
import es.vargontoc.educational.framework.content.ports.out.StoryPageRepository;
import es.vargontoc.educational.framework.content.ports.out.StoryRepository;
import es.vargontoc.educational.framework.content.ports.out.TopicRepository;
import es.vargontoc.educational.framework.content.ports.out.TracingPatternRepository;
import es.vargontoc.educational.framework.content.ports.out.WorldDiscoveryElementRepository;
import es.vargontoc.educational.framework.content.ports.out.WorldHostRepository;
import es.vargontoc.educational.framework.content.ports.out.WorldNarrativeSituationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SeedServiceTest {

    @Mock
    private DevSeedStateJpaRepository seedStateRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private CuriosityRepository curiosityRepository;

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private DifficultyLevelRepository difficultyLevelRepository;

    @Mock
    private AvatarEventCatalogRepository avatarEventCatalogRepository;

    @Mock
    private LearningPathRepository learningPathRepository;

    @Mock
    private LearningPathStepRepository learningPathStepRepository;

    @Mock
    private TracingPatternRepository tracingPatternRepository;

    @Mock
    private StoryRepository storyRepository;

    @Mock
    private StoryPageRepository storyPageRepository;

    @Mock
    private WorldHostRepository worldHostRepository;

    @Mock
    private WorldNarrativeSituationRepository worldNarrativeSituationRepository;

    @Mock
    private WorldDiscoveryElementRepository worldDiscoveryElementRepository;

    private SeedService seedService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        seedService = new SeedService(
            seedStateRepository, categoryRepository, topicRepository, curiosityRepository,
            activityRepository, difficultyLevelRepository, avatarEventCatalogRepository,
            learningPathRepository, learningPathStepRepository, tracingPatternRepository,
            storyRepository, storyPageRepository, worldHostRepository, worldNarrativeSituationRepository,
            worldDiscoveryElementRepository, objectMapper
        );
    }

    private Category buildCategory(Long id, String name) {
        var cat = new Category();
        cat.setId(id);
        cat.setName(name);
        return cat;
    }

    private Topic buildTopic(Long id, String name, Long categoryId) {
        var topic = new Topic();
        topic.setId(id);
        topic.setName(name);
        topic.setCategoryId(categoryId);
        return topic;
    }

    private void setupDefaultMocks() {
        when(seedStateRepository.save(any(DevSeedStateJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> {
            var cat = inv.getArgument(0, Category.class);
            cat.setId(1L);
            return cat;
        });
        when(categoryRepository.findAll()).thenReturn(List.of(buildCategory(1L, "Naturaleza"), buildCategory(2L, "Matemáticas")));
        when(topicRepository.save(any(Topic.class))).thenAnswer(inv -> {
            var topic = inv.getArgument(0, Topic.class);
            topic.setId(1L);
            return topic;
        });
        when(topicRepository.findAll()).thenReturn(List.of(
            buildTopic(1L, "Animales", 1L),
            buildTopic(2L, "Plantas", 1L),
            buildTopic(3L, "Números", 2L),
            buildTopic(4L, "Formas", 2L)
        ));
        when(activityRepository.save(any())).thenAnswer(inv -> {
            var obj = inv.getArgument(0);
            try { obj.getClass().getMethod("setId", Long.class).invoke(obj, 1L); } catch (Exception ignored) {}
            return obj;
        });
        when(curiosityRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(difficultyLevelRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(avatarEventCatalogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(learningPathRepository.save(any())).thenAnswer(inv -> {
            var obj = inv.getArgument(0);
            try { obj.getClass().getMethod("setId", Long.class).invoke(obj, 1L); } catch (Exception ignored) {}
            return obj;
        });
        when(learningPathStepRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(tracingPatternRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(storyRepository.save(any())).thenAnswer(inv -> {
            var obj = inv.getArgument(0);
            try { obj.getClass().getMethod("setId", Long.class).invoke(obj, 1L); } catch (Exception ignored) {}
            return obj;
        });
        when(storyPageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(worldHostRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(worldNarrativeSituationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(worldDiscoveryElementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(learningPathRepository.findAll()).thenReturn(List.of());
        when(activityRepository.findAll()).thenReturn(List.of());
        when(storyRepository.findAll()).thenReturn(List.of());
    }

    @Test
    void loadAll_whenNoSeedsLoaded_loadsAllCategories() {
        when(seedStateRepository.existsBySeedKey(anyString())).thenReturn(false);
        setupDefaultMocks();

        seedService.loadAll();

        verify(categoryRepository, times(2)).save(any(Category.class));
    }

    @Test
    void loadAll_whenSeedsAlreadyLoaded_skipsAll() {
        when(seedStateRepository.existsBySeedKey(anyString())).thenReturn(true);

        seedService.loadAll();

        verify(categoryRepository, never()).save(any());
        verify(topicRepository, never()).save(any());
        verify(curiosityRepository, never()).save(any());
        verify(activityRepository, never()).save(any());
    }

    @Test
    void loadAll_whenCategoryAlreadyLoaded_skipsOnlyThatCategory() {
        when(seedStateRepository.existsBySeedKey(anyString())).thenReturn(false);
        when(seedStateRepository.existsBySeedKey("category:naturaleza")).thenReturn(true);
        setupDefaultMocks();

        seedService.loadAll();

        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void loadAll_isIdempotent_doesNotThrowOnSecondRun() {
        when(seedStateRepository.existsBySeedKey(anyString())).thenReturn(false);
        setupDefaultMocks();

        assertDoesNotThrow(() -> {
            seedService.loadAll();
            seedService.loadAll();
        });
    }
}
