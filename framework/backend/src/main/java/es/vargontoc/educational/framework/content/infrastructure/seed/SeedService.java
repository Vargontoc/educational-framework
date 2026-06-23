package es.vargontoc.educational.framework.content.infrastructure.seed;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.vargontoc.educational.framework.content.infrastructure.persistence.DevSeedStateJpaEntity;
import es.vargontoc.educational.framework.content.infrastructure.persistence.DevSeedStateJpaRepository;
import es.vargontoc.educational.framework.content.model.Activity;
import es.vargontoc.educational.framework.content.model.AvatarEventType;
import es.vargontoc.educational.framework.content.model.AvatarTone;
import es.vargontoc.educational.framework.content.model.Category;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.Curiosity;
import es.vargontoc.educational.framework.content.model.DifficultyCode;
import es.vargontoc.educational.framework.content.model.DifficultyLevel;
import es.vargontoc.educational.framework.content.model.LearningPath;
import es.vargontoc.educational.framework.content.model.LearningPathStep;
import es.vargontoc.educational.framework.content.model.Story;
import es.vargontoc.educational.framework.content.model.StoryPage;
import es.vargontoc.educational.framework.content.model.Topic;
import es.vargontoc.educational.framework.content.model.TracingPattern;
import es.vargontoc.educational.framework.content.model.WorldDiscoveryElement;
import es.vargontoc.educational.framework.content.model.WorldHost;
import es.vargontoc.educational.framework.content.model.WorldNarrativeSituation;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Transactional
public class SeedService {

    private static final Logger log = LoggerFactory.getLogger(SeedService.class);

    private final DevSeedStateJpaRepository seedStateRepository;
    private final CategoryRepository categoryRepository;
    private final TopicRepository topicRepository;
    private final CuriosityRepository curiosityRepository;
    private final ActivityRepository activityRepository;
    private final DifficultyLevelRepository difficultyLevelRepository;
    private final AvatarEventCatalogRepository avatarEventCatalogRepository;
    private final LearningPathRepository learningPathRepository;
    private final LearningPathStepRepository learningPathStepRepository;
    private final TracingPatternRepository tracingPatternRepository;
    private final StoryRepository storyRepository;
    private final StoryPageRepository storyPageRepository;
    private final WorldHostRepository worldHostRepository;
    private final WorldNarrativeSituationRepository worldNarrativeSituationRepository;
    private final WorldDiscoveryElementRepository worldDiscoveryElementRepository;
    private final ObjectMapper objectMapper;

    private final Map<String, Long> categoryCache = new HashMap<>();
    private final Map<String, Long> topicCache = new HashMap<>();
    private final Map<String, Long> activityCache = new HashMap<>();
    private final Map<String, Long> learningPathCache = new HashMap<>();
    private final Map<String, Long> storyCache = new HashMap<>();

    public SeedService(
            DevSeedStateJpaRepository seedStateRepository,
            CategoryRepository categoryRepository,
            TopicRepository topicRepository,
            CuriosityRepository curiosityRepository,
            ActivityRepository activityRepository,
            DifficultyLevelRepository difficultyLevelRepository,
            AvatarEventCatalogRepository avatarEventCatalogRepository,
            LearningPathRepository learningPathRepository,
            LearningPathStepRepository learningPathStepRepository,
            TracingPatternRepository tracingPatternRepository,
            StoryRepository storyRepository,
            StoryPageRepository storyPageRepository,
            WorldHostRepository worldHostRepository,
            WorldNarrativeSituationRepository worldNarrativeSituationRepository,
            WorldDiscoveryElementRepository worldDiscoveryElementRepository,
            ObjectMapper objectMapper) {
        this.seedStateRepository = seedStateRepository;
        this.categoryRepository = categoryRepository;
        this.topicRepository = topicRepository;
        this.curiosityRepository = curiosityRepository;
        this.activityRepository = activityRepository;
        this.difficultyLevelRepository = difficultyLevelRepository;
        this.avatarEventCatalogRepository = avatarEventCatalogRepository;
        this.learningPathRepository = learningPathRepository;
        this.learningPathStepRepository = learningPathStepRepository;
        this.tracingPatternRepository = tracingPatternRepository;
        this.storyRepository = storyRepository;
        this.storyPageRepository = storyPageRepository;
        this.worldHostRepository = worldHostRepository;
        this.worldNarrativeSituationRepository = worldNarrativeSituationRepository;
        this.worldDiscoveryElementRepository = worldDiscoveryElementRepository;
        this.objectMapper = objectMapper;
    }

    public void loadAll() {
        log.info("Starting seed loading...");
        int loaded = 0;
        loaded += loadCategories();
        loaded += loadTopics();
        loaded += loadCuriosities();
        loaded += loadActivities();
        loaded += loadDifficultyLevels();
        loaded += loadAvatarEvents();
        loaded += loadLearningPaths();
        loaded += loadLearningPathSteps();
        loaded += loadTracingPatterns();
        loaded += loadStories();
        loaded += loadStoryPages();
        loaded += loadWorldHosts();
        loaded += loadWorldNarrativeSituations();
        loaded += loadWorldDiscoveryElements();
        log.info("Seed loading complete. {} records loaded.", loaded);
    }

    private boolean alreadyLoaded(String seedKey) {
        return seedStateRepository.existsBySeedKey(seedKey);
    }

    private void markLoaded(String seedKey, String seedFile) {
        seedStateRepository.save(new DevSeedStateJpaEntity(seedKey, seedFile));
    }

    private <T> List<T> readSeedFile(String fileName, TypeReference<List<T>> typeRef) {
        try {
            var resource = new ClassPathResource("seeds/" + fileName);
            return objectMapper.readValue(resource.getInputStream(), typeRef);
        } catch (IOException e) {
            log.error("Failed to read seed file: {}", fileName, e);
            return Collections.emptyList();
        }
    }

    private int loadCategories() {
        String file = "01-categories.json";
        var seeds = readSeedFile(file, new TypeReference<List<SeedData.CategorySeed>>() {});
        int count = 0;
        for (var seed : seeds) {
            String key = "category:" + seed.name().toLowerCase();
            if (alreadyLoaded(key)) {
                log.debug("Skipping already loaded seed: {}", key);
                continue;
            }
            var category = new Category();
            category.setName(seed.name());
            category.setDescription(seed.description());
            category.setStatus(ContentStatus.valueOf(seed.status()));
            category.setDisplayOrder(seed.displayOrder());
            category.setCreatedAt(LocalDateTime.now());
            var savedCategory = categoryRepository.save(category);
            markLoaded(key, file);
            categoryCache.put(seed.name(), savedCategory.getId());
            count++;
            log.info("Loaded seed: {}", key);
        }
        return count;
    }

    private int loadTopics() {
        String file = "02-topics.json";
        var seeds = readSeedFile(file, new TypeReference<List<SeedData.TopicSeed>>() {});
        int count = 0;
        for (var seed : seeds) {
            String key = "topic:" + seed.name().toLowerCase();
            if (alreadyLoaded(key)) {
                log.debug("Skipping already loaded seed: {}", key);
                continue;
            }
            Long categoryId = resolveCategoryId(seed.categoryName());
            if (categoryId == null) {
                log.warn("Category not found for topic seed: {}", seed.name());
                continue;
            }
            var topic = new Topic();
            topic.setName(seed.name());
            topic.setDescription(seed.description());
            topic.setCategoryId(categoryId);
            topic.setStatus(ContentStatus.valueOf(seed.status()));
            topic.setMinAge(seed.minAge());
            topic.setMaxAge(seed.maxAge());
            topic.setCreatedAt(LocalDateTime.now());
            var savedTopic = topicRepository.save(topic);
            markLoaded(key, file);
            topicCache.put(seed.name(), savedTopic.getId());
            count++;
            log.info("Loaded seed: {}", key);
        }
        return count;
    }

    private int loadCuriosities() {
        String file = "03-curiosities.json";
        var seeds = readSeedFile(file, new TypeReference<List<SeedData.CuriositySeed>>() {});
        int count = 0;
        for (var seed : seeds) {
            String key = "curiosity:" + seed.text().substring(0, Math.min(50, seed.text().length())).toLowerCase();
            if (alreadyLoaded(key)) {
                log.debug("Skipping already loaded seed: {}", key);
                continue;
            }
            Long topicId = resolveTopicId(seed.topicName());
            var curiosity = new Curiosity();
            curiosity.setText(seed.text());
            curiosity.setTopicId(topicId);
            curiosity.setMinAge(seed.minAge());
            curiosity.setMaxAge(seed.maxAge());
            curiosity.setTags(seed.tags() != null ? seed.tags() : Collections.emptyList());
            curiosity.setLocale(seed.locale());
            curiosity.setPhoneticHint(seed.phoneticHint());
            curiosity.setStatus(ContentStatus.ACTIVE);
            curiosity.setCreatedAt(LocalDateTime.now());
            curiosityRepository.save(curiosity);
            markLoaded(key, file);
            count++;
            log.info("Loaded seed: {}", key);
        }
        return count;
    }

    private int loadActivities() {
        String file = "04-activities.json";
        var seeds = readSeedFile(file, new TypeReference<List<SeedData.ActivitySeed>>() {});
        int count = 0;
        for (var seed : seeds) {
            String key = "activity:" + seed.name().toLowerCase();
            if (alreadyLoaded(key)) {
                log.debug("Skipping already loaded seed: {}", key);
                continue;
            }
            List<Long> topicIds = seed.topicNames() != null
                ? seed.topicNames().stream().map(this::resolveTopicId).filter(id -> id != null).toList()
                : Collections.emptyList();
            var activity = new Activity();
            activity.setName(seed.name());
            activity.setDescription(seed.description());
            activity.setGameEngineType(seed.gameEngineType());
            activity.setMinAge(seed.minAge());
            activity.setMaxAge(seed.maxAge());
            activity.setStatus(ContentStatus.valueOf(seed.status()));
            activity.setTopicIds(topicIds);
            activity.setCreatedAt(LocalDateTime.now());
            var savedActivity = activityRepository.save(activity);
            markLoaded(key, file);
            activityCache.put(seed.name(), savedActivity.getId());
            count++;
            log.info("Loaded seed: {}", key);
        }
        return count;
    }

    private int loadDifficultyLevels() {
        String file = "05-difficulty-levels.json";
        var seeds = readSeedFile(file, new TypeReference<List<SeedData.DifficultyLevelSeed>>() {});
        int count = 0;
        for (var seed : seeds) {
            String key = "difficulty:" + seed.activityName().toLowerCase() + ":" + seed.difficultyCode().toLowerCase();
            if (alreadyLoaded(key)) {
                log.debug("Skipping already loaded seed: {}", key);
                continue;
            }
            Long activityId = resolveActivityId(seed.activityName());
            if (activityId == null) {
                log.warn("Activity not found for difficulty seed: {}", seed.activityName());
                continue;
            }
            var difficulty = new DifficultyLevel();
            difficulty.setActivityId(activityId);
            difficulty.setDifficultyCode(DifficultyCode.valueOf(seed.difficultyCode()));
            difficulty.setEngineParams(seed.engineParams());
            difficulty.setCreatedAt(LocalDateTime.now());
            difficultyLevelRepository.save(difficulty);
            markLoaded(key, file);
            count++;
            log.info("Loaded seed: {}", key);
        }
        return count;
    }

    private int loadAvatarEvents() {
        String file = "06-avatar-events.json";
        var seeds = readSeedFile(file, new TypeReference<List<SeedData.AvatarEventSeed>>() {});
        int count = 0;
        for (var seed : seeds) {
            String key = "avatar-event:" + seed.eventType().toLowerCase() + ":" + seed.tone().toLowerCase();
            if (alreadyLoaded(key)) {
                log.debug("Skipping already loaded seed: {}", key);
                continue;
            }
            var event = new es.vargontoc.educational.framework.content.model.AvatarEventCatalog();
            event.setEventType(AvatarEventType.valueOf(seed.eventType()));
            event.setTone(AvatarTone.valueOf(seed.tone()));
            event.setLocale(seed.locale());
            event.setMessageText(seed.messageText());
            event.setStatus(ContentStatus.valueOf(seed.status()));
            event.setCreatedAt(LocalDateTime.now());
            avatarEventCatalogRepository.save(event);
            markLoaded(key, file);
            count++;
            log.info("Loaded seed: {}", key);
        }
        return count;
    }

    private int loadLearningPaths() {
        String file = "07-learning-paths.json";
        var seeds = readSeedFile(file, new TypeReference<List<SeedData.LearningPathSeed>>() {});
        int count = 0;
        for (var seed : seeds) {
            String key = "learning-path:" + seed.name().toLowerCase();
            if (alreadyLoaded(key)) {
                log.debug("Skipping already loaded seed: {}", key);
                continue;
            }
            var path = new LearningPath();
            path.setName(seed.name());
            path.setDescription(seed.description());
            path.setMinAge(seed.minAge());
            path.setMaxAge(seed.maxAge());
            path.setLocale(seed.locale());
            path.setStatus(ContentStatus.valueOf(seed.status()));
            path.setCreatedAt(LocalDateTime.now());
            var savedPath = learningPathRepository.save(path);
            markLoaded(key, file);
            learningPathCache.put(seed.name(), savedPath.getId());
            count++;
            log.info("Loaded seed: {}", key);
        }
        return count;
    }

    private int loadLearningPathSteps() {
        String file = "08-learning-path-steps.json";
        var seeds = readSeedFile(file, new TypeReference<List<SeedData.LearningPathStepSeed>>() {});
        int count = 0;
        for (var seed : seeds) {
            String key = "learning-path-step:" + seed.learningPathName().toLowerCase() + ":" + seed.stepOrder();
            if (alreadyLoaded(key)) {
                log.debug("Skipping already loaded seed: {}", key);
                continue;
            }
            Long pathId = resolveLearningPathId(seed.learningPathName());
            Long activityId = resolveActivityId(seed.activityName());
            if (pathId == null || activityId == null) {
                log.warn("Learning path or activity not found for step seed: {}", seed.learningPathName());
                continue;
            }
            var step = new LearningPathStep();
            step.setLearningPathId(pathId);
            step.setActivityId(activityId);
            step.setStepOrder(seed.stepOrder());
            step.setUnlockCondition(seed.unlockCondition());
            step.setStatus(ContentStatus.ACTIVE);
            step.setCreatedAt(LocalDateTime.now());
            learningPathStepRepository.save(step);
            markLoaded(key, file);
            count++;
            log.info("Loaded seed: {}", key);
        }
        return count;
    }

    private int loadTracingPatterns() {
        String file = "09-tracing-patterns.json";
        var seeds = readSeedFile(file, new TypeReference<List<SeedData.TracingPatternSeed>>() {});
        int count = 0;
        for (var seed : seeds) {
            String key = "tracing-pattern:" + seed.name().toLowerCase();
            if (alreadyLoaded(key)) {
                log.debug("Skipping already loaded seed: {}", key);
                continue;
            }
            Long topicId = resolveTopicId(seed.topicName());
            if (topicId == null) {
                log.warn("Topic not found for tracing pattern seed: {}", seed.name());
                continue;
            }
            var pattern = new TracingPattern();
            pattern.setTopicId(topicId);
            pattern.setName(seed.name());
            pattern.setDescription(seed.description());
            pattern.setPatternType(seed.patternType());
            pattern.setPoints(seed.points() != null ? seed.points() : Collections.emptyList());
            pattern.setMinAge(seed.minAge());
            pattern.setMaxAge(seed.maxAge());
            pattern.setStatus(ContentStatus.valueOf(seed.status()));
            pattern.setCreatedAt(LocalDateTime.now());
            tracingPatternRepository.save(pattern);
            markLoaded(key, file);
            count++;
            log.info("Loaded seed: {}", key);
        }
        return count;
    }

    private int loadStories() {
        String file = "10-stories.json";
        var seeds = readSeedFile(file, new TypeReference<List<SeedData.StorySeed>>() {});
        int count = 0;
        for (var seed : seeds) {
            String key = "story:" + seed.title().toLowerCase();
            if (alreadyLoaded(key)) {
                log.debug("Skipping already loaded seed: {}", key);
                continue;
            }
            List<Long> topicIds = seed.topicNames() != null
                ? seed.topicNames().stream().map(this::resolveTopicId).filter(id -> id != null).toList()
                : Collections.emptyList();
            var story = new Story();
            story.setTitle(seed.title());
            story.setDescription(seed.description());
            story.setMinAge(seed.minAge());
            story.setMaxAge(seed.maxAge());
            story.setEstimatedDurationMinutes(seed.estimatedDurationMinutes());
            story.setTopicIds(topicIds);
            story.setStatus(ContentStatus.valueOf(seed.status()));
            story.setCreatedAt(LocalDateTime.now());
            var savedStory = storyRepository.save(story);
            markLoaded(key, file);
            storyCache.put(seed.title(), savedStory.getId());
            count++;
            log.info("Loaded seed: {}", key);
        }
        return count;
    }

    private int loadStoryPages() {
        String file = "11-story-pages.json";
        var seeds = readSeedFile(file, new TypeReference<List<SeedData.StoryPageSeed>>() {});
        int count = 0;
        for (var seed : seeds) {
            String key = "story-page:" + seed.storyTitle().toLowerCase() + ":" + seed.pageOrder();
            if (alreadyLoaded(key)) {
                log.debug("Skipping already loaded seed: {}", key);
                continue;
            }
            Long storyId = resolveStoryId(seed.storyTitle());
            if (storyId == null) {
                log.warn("Story not found for page seed: {}", seed.storyTitle());
                continue;
            }
            var page = new StoryPage();
            page.setStoryId(storyId);
            page.setPageOrder(seed.pageOrder());
            page.setText(seed.text());
            page.setStatus(ContentStatus.valueOf(seed.status()));
            page.setCreatedAt(LocalDateTime.now());
            storyPageRepository.save(page);
            markLoaded(key, file);
            count++;
            log.info("Loaded seed: {}", key);
        }
        return count;
    }

    private int loadWorldHosts() {
        String file = "12-world-hosts.json";
        var seeds = readSeedFile(file, new TypeReference<List<SeedData.WorldHostSeed>>() {});
        int count = 0;
        for (var seed : seeds) {
            String key = "world-host:" + seed.code().toLowerCase();
            if (alreadyLoaded(key)) {
                log.debug("Skipping already loaded seed: {}", key);
                continue;
            }
            var worldHost = new WorldHost();
            worldHost.setCode(seed.code());
            worldHost.setDisplayName(seed.displayName());
            worldHost.setBiome(es.vargontoc.educational.framework.content.model.Biome.valueOf(seed.biome()));
            worldHost.setDescription(seed.description());
            worldHost.setMinAge(seed.minAge());
            worldHost.setMaxAge(seed.maxAge());
            worldHost.setStatus(ContentStatus.valueOf(seed.status()));
            worldHost.setSortOrder(seed.sortOrder());
            worldHost.setVisualAssetKey(seed.visualAssetKey());
            worldHost.setCreatedAt(LocalDateTime.now());
            worldHostRepository.save(worldHost);
            markLoaded(key, file);
            count++;
            log.info("Loaded seed: {}", key);
        }
        return count;
    }

    private int loadWorldNarrativeSituations() {
        String file = "13-world-narrative-situations.json";
        var seeds = readSeedFile(file, new TypeReference<List<SeedData.WorldNarrativeSituationSeed>>() {});
        int count = 0;
        for (var seed : seeds) {
            String key = "world-narrative-situation:" + seed.code().toLowerCase();
            if (alreadyLoaded(key)) {
                log.debug("Skipping already loaded seed: {}", key);
                continue;
            }
            var situation = new WorldNarrativeSituation();
            situation.setCode(seed.code());
            situation.setDisplayText(seed.displayText());
            situation.setSituationType(es.vargontoc.educational.framework.content.model.SituationType.valueOf(seed.situationType()));
            situation.setTone(seed.tone() != null ? es.vargontoc.educational.framework.content.model.Tone.valueOf(seed.tone()) : null);
            situation.setMinAge(seed.minAge());
            situation.setMaxAge(seed.maxAge());
            situation.setStatus(ContentStatus.valueOf(seed.status()));
            situation.setSortOrder(seed.sortOrder());
            situation.setCreatedAt(LocalDateTime.now());
            worldNarrativeSituationRepository.save(situation);
            markLoaded(key, file);
            count++;
            log.info("Loaded seed: {}", key);
        }
        return count;
    }

    private int loadWorldDiscoveryElements() {
        String file = "14-world-discovery-elements.json";
        var seeds = readSeedFile(file, new TypeReference<List<SeedData.WorldDiscoveryElementSeed>>() {});
        int count = 0;
        for (var seed : seeds) {
            String key = "world-discovery-element:" + seed.code().toLowerCase();
            if (alreadyLoaded(key)) {
                log.debug("Skipping already loaded seed: {}", key);
                continue;
            }
            var element = new WorldDiscoveryElement();
            element.setCode(seed.code());
            element.setDisplayName(seed.displayName());
            element.setElementType(es.vargontoc.educational.framework.content.model.ElementType.valueOf(seed.elementType()));
            element.setBiome(es.vargontoc.educational.framework.content.model.Biome.valueOf(seed.biome()));
            element.setMinAge(seed.minAge());
            element.setMaxAge(seed.maxAge());
            element.setStatus(ContentStatus.valueOf(seed.status()));
            element.setActivityId(seed.activityId());
            element.setTopicId(seed.topicId());
            element.setVisualAssetKey(seed.visualAssetKey());
            element.setInteractionCueType(seed.interactionCueType() != null
                    ? es.vargontoc.educational.framework.content.model.InteractionCueType.valueOf(seed.interactionCueType())
                    : null);
            element.setSortOrder(seed.sortOrder());
            element.setCreatedAt(LocalDateTime.now());
            worldDiscoveryElementRepository.save(element);
            markLoaded(key, file);
            count++;
            log.info("Loaded seed: {}", key);
        }
        return count;
    }

    private Long resolveCategoryId(String name) {
        if (categoryCache.containsKey(name)) {
            return categoryCache.get(name);
        }
        return categoryRepository.findAll().stream()
            .filter(c -> c.getName().equals(name))
            .map(Category::getId)
            .findFirst()
            .orElse(null);
    }

    private Long resolveTopicId(String name) {
        if (topicCache.containsKey(name)) {
            return topicCache.get(name);
        }
        return topicRepository.findAll().stream()
            .filter(t -> t.getName().equals(name))
            .map(Topic::getId)
            .findFirst()
            .orElse(null);
    }

    private Long resolveActivityId(String name) {
        if (activityCache.containsKey(name)) {
            return activityCache.get(name);
        }
        return activityRepository.findAll().stream()
            .filter(a -> a.getName().equals(name))
            .map(Activity::getId)
            .findFirst()
            .orElse(null);
    }

    private Long resolveLearningPathId(String name) {
        if (learningPathCache.containsKey(name)) {
            return learningPathCache.get(name);
        }
        return learningPathRepository.findAll().stream()
            .filter(lp -> lp.getName().equals(name))
            .map(LearningPath::getId)
            .findFirst()
            .orElse(null);
    }

    private Long resolveStoryId(String title) {
        if (storyCache.containsKey(title)) {
            return storyCache.get(title);
        }
        return storyRepository.findAll().stream()
            .filter(s -> s.getTitle().equals(title))
            .map(Story::getId)
            .findFirst()
            .orElse(null);
    }
}
