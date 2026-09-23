package es.vargontoc.educational.framework.content.infrastructure.seed;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import es.vargontoc.educational.framework.audio.application.ports.in.AudioUseCase;
import es.vargontoc.educational.framework.audio.domain.AudioRequest;
import es.vargontoc.educational.framework.audio.domain.enums.TonePreset;
import es.vargontoc.educational.framework.avatar.domain.enums.AvatarEventType;
import es.vargontoc.educational.framework.content.infrastructure.persistence.DevSeedStateJpaEntity;
import es.vargontoc.educational.framework.content.infrastructure.persistence.DevSeedStateJpaRepository;
import es.vargontoc.educational.framework.content.infrastructure.seed.SeedData.RecognitionAlphaNumericElementSeed;
import es.vargontoc.educational.framework.content.infrastructure.seed.SeedData.RecognitionAnimalElementSeed;
import es.vargontoc.educational.framework.content.infrastructure.seed.SeedData.RecognitionComparisonElementSeed;
import es.vargontoc.educational.framework.content.infrastructure.seed.SeedData.RecognitionColorElementSeed;
import es.vargontoc.educational.framework.content.infrastructure.seed.SeedData.RecognitionMemoryElementSeed;
import es.vargontoc.educational.framework.content.infrastructure.seed.SeedData.RecognitionShapeElementSeed;
import es.vargontoc.educational.framework.content.infrastructure.seed.SeedData.LetterSimilarityPairSeed;
import es.vargontoc.educational.framework.content.model.Activity;
import es.vargontoc.educational.framework.content.model.Category;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.Curiosity;
import es.vargontoc.educational.framework.content.model.DifficultyCode;
import es.vargontoc.educational.framework.content.model.DifficultyLevel;
import es.vargontoc.educational.framework.content.model.LearningPath;
import es.vargontoc.educational.framework.content.model.LearningPathStep;
import es.vargontoc.educational.framework.content.model.RecognitionElement;
import es.vargontoc.educational.framework.content.model.Topic;
import es.vargontoc.educational.framework.content.model.TracingPattern;

import es.vargontoc.educational.framework.content.model.WorldHost;
import es.vargontoc.educational.framework.content.model.WorldNarrativeSituation;
import es.vargontoc.educational.framework.content.ports.out.ActivityRepository;
import es.vargontoc.educational.framework.content.ports.out.AvatarEventCatalogRepository;
import es.vargontoc.educational.framework.content.ports.out.CategoryRepository;
import es.vargontoc.educational.framework.content.ports.out.CuriosityRepository;
import es.vargontoc.educational.framework.content.ports.out.DifficultyLevelRepository;
import es.vargontoc.educational.framework.content.ports.out.LearningPathRepository;
import es.vargontoc.educational.framework.content.ports.out.LearningPathStepRepository;
import es.vargontoc.educational.framework.content.ports.out.RecognitionElementRepository;
import es.vargontoc.educational.framework.content.ports.out.TopicRepository;
import es.vargontoc.educational.framework.content.ports.out.TracingPatternRepository;
import es.vargontoc.educational.framework.content.ports.out.WorldHostRepository;
import es.vargontoc.educational.framework.content.ports.out.WorldNarrativeSituationRepository;
import es.vargontoc.educational.framework.game.infrastructure.persistence.RecognitionSimilarityPairJpaEntity;
import es.vargontoc.educational.framework.game.infrastructure.persistence.RecognitionSimilarityPairJpaRepository;
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
    private final WorldHostRepository worldHostRepository;
    private final WorldNarrativeSituationRepository worldNarrativeSituationRepository;
    private final RecognitionElementRepository recognitionElementRepository;
    private final RecognitionSimilarityPairJpaRepository recognitionSimilarityPairRepository;
    private final ObjectMapper objectMapper;
    private final AudioUseCase audio;


    private final Map<String, Long> categoryCache = new HashMap<>();
    private final Map<String, Long> topicCache = new HashMap<>();
    private final Map<String, Long> activityCache = new HashMap<>();
    private final Map<String, Long> learningPathCache = new HashMap<>();

    public SeedService(
            AudioUseCase audio,
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
            WorldHostRepository worldHostRepository,
            WorldNarrativeSituationRepository worldNarrativeSituationRepository,
            RecognitionElementRepository recognitionElementRepository,
            RecognitionSimilarityPairJpaRepository recognitionSimilarityPairRepository,
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
        this.worldHostRepository = worldHostRepository;
        this.worldNarrativeSituationRepository = worldNarrativeSituationRepository;
        this.recognitionElementRepository = recognitionElementRepository;
        this.recognitionSimilarityPairRepository = recognitionSimilarityPairRepository;
        this.objectMapper = objectMapper;
        this.audio = audio;
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
        loaded += loadWorldHosts();
        loaded += loadWorldNarrativeSituations();
        loaded += loadRecognitionAlphaNumeric(false);
        loaded += loadRecognitionAlphaNumeric(true);
        loaded += loadRecognitionColors();
        loaded += loadRecognitionAnimals();
        loaded += loadRecognitionComparison();
        loaded += loadRecognitionMemory();
        loaded += loadRecognitionShapes();
        loaded += loadLetterSimilarityPairs();
        loaded += loadNumberSimilarityPairs();
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
        } catch (IOException | JacksonException e) {
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
            if (seed.recognitionType() != null && !seed.recognitionType().isBlank()) {
                topic.setRecognitionType(es.vargontoc.educational.framework.content.model.RecognitionType.valueOf(seed.recognitionType()));
            }
            if (seed.habitatTag() != null && !seed.habitatTag().isBlank()) {
                topic.setHabitatTag(es.vargontoc.educational.framework.content.model.Biome.valueOf(seed.habitatTag()));
            }
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
            event.setTone(TonePreset.valueOf(seed.tone()));
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
            worldHost.setWorldWidth(seed.worldWidth());
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

    private Long resolveCategoryId(String name) {
        if (categoryCache.containsKey(name)) {
            return categoryCache.get(name);
        }
        return categoryRepository.findAll().stream()
            .filter(c -> c.getName().equals(name))
            .map(c -> c.getId())
            .findFirst()
            .orElse(null);
    }

    private Long resolveTopicId(String name) {
        if (topicCache.containsKey(name)) {
            return topicCache.get(name);
        }
        return topicRepository.findAll().stream()
            .filter(t -> t.getName().equals(name))
            .map(t -> t.getId())
            .findFirst()
            .orElse(null);
    }

    private Long resolveActivityId(String name) {
        if (activityCache.containsKey(name)) {
            return activityCache.get(name);
        }
        return activityRepository.findAll().stream()
            .filter(a -> a.getName().equals(name))
            .map(a -> a.getId())
            .findFirst()
            .orElse(null);
    }

    private Long resolveLearningPathId(String name) {
        if (learningPathCache.containsKey(name)) {
            return learningPathCache.get(name);
        }
        return learningPathRepository.findAll().stream()
            .filter(lp -> lp.getName().equals(name))
            .map(lp -> lp.getId())
            .findFirst()
            .orElse(null);
    }

    private int loadRecognitionShapes() {
        String file = "23-recognition-elements-shapes.json";
        var seeds = readSeedFile(file, new TypeReference<List<RecognitionShapeElementSeed>>() {});

        int count = 0;

        Long topicId = resolveTopicId("Formas");
        if(topicId == null) {
            log.warn("Topic not found for recognition seed");
            return count;
        }

        for(var seed : seeds) {
            String key = "recognition-element-shape:" + seed.code();
            if(alreadyLoaded(key)){
                log.debug("Skipping already loaded seed: {}", key);
                continue;
            }

            audio.getAudio(AudioRequest.withPreset(shapeNubiAudio(seed), TonePreset.CALM));

            var element = new RecognitionElement();
            element.setTopicId(topicId);
            element.setCode(seed.code());
            element.setSortOrder(0);
            // Keep the seed's own resourceRefs (it already carries "image", which the frontend needs to resolve
            // the texture). The group metadata used for distractor selection is read straight from the seed
            // file by ShapeGroupService, not from here, so it does not need to be duplicated into resourceRefs.
            element.setResourceRefs(seed.resourceRefs());
            element.setStatus(ContentStatus.ACTIVE);
            element.setCreatedAt(LocalDateTime.now());
            recognitionElementRepository.save(element);

            markLoaded(key, file);
            count++;
            log.info("Loaded seed: {}", key);
        }

        return count;
    }
    
    private int loadRecognitionAnimals() {
        String file = "20-recognition-elements-animals.json";
        var seeds = readSeedFile(file, new TypeReference<List<RecognitionAnimalElementSeed>>() {});

        int count = 0;

        Long topicId = resolveTopicId("Animales");
        if(topicId == null){
            log.warn("Topic not found for recognition element seed.");
            return 0;
        }

        for(var seed : seeds) {
            String key =  "recognition-element-animal:" + seed.code();
            if(alreadyLoaded(key)){
                log.debug("Skipping already loaded seed: {}", key);
                continue;
            }

            audio.getAudio(AudioRequest.withPreset(animalNubiAudio(seed), TonePreset.CALM));

            var element = new RecognitionElement();
            element.setTopicId(topicId);
            element.setCode(seed.code());
            element.setSortOrder(0);
            element.setResourceRefs(objectMapper.writeValueAsString(Map.of(
                "nubi-audio", animalNubiAudio(seed),
                "biome", seed.biome(),
                "group", seed.group()
            )));
            element.setStatus(ContentStatus.ACTIVE);
            element.setCreatedAt(LocalDateTime.now());
            recognitionElementRepository.save(element);

            markLoaded(key, file);
            count++;
            log.info("Loaded seed: {}", key);
        }

        return count;
    }


    /**
     * Narration text of an animal: {@code resourceRefs["nubi-audio"]} from the seed, falling back to the raw
     * {@code nubi} text. It is also the text warmed into the audio cache, which is keyed by the exact text
     * {@code RoundAudioService} asks for at runtime.
     */
    private String animalNubiAudio(RecognitionAnimalElementSeed seed) {
        return nubiAudioText(seed.resourceRefs(), seed.nubi());
    }

    private String shapeNubiAudio(RecognitionShapeElementSeed  seed) {
        return nubiAudioText(seed.resourceRefs(), seed.nubi());
    }

    private String nubiAudioText(String resourceRefs, String fallback) {
        if (resourceRefs != null && !resourceRefs.isBlank()) {
            var text = objectMapper.readTree(resourceRefs).get("nubi-audio");
            if (text != null && !text.isNull() && !text.asString().isBlank()) {
                return text.asString();
            }
        }
        return fallback;
    }

    private int loadRecognitionComparison() {
        String file = "21-comparison-elements.json";
        var seeds = readSeedFile(file, new TypeReference<List<RecognitionComparisonElementSeed>>() {});

        int count = 0;

        Long topicId = resolveTopicId("Comparación");
        if(topicId == null){
            log.warn("Topic not found for recognition element seed.");
            return 0;
        }

        for(var seed : seeds) {
            String key =  "recognition-element-comparison:" + seed.code();
            if(alreadyLoaded(key)){
                log.debug("Skipping already loaded seed: {}", key);
                continue;
            }

            String nubiAudio = nubiAudioText(seed.resourceRefs(), null);
            if (nubiAudio != null) {
                audio.getAudio(AudioRequest.withPreset(nubiAudio, TonePreset.CALM));
            }

            var element = new RecognitionElement();
            element.setTopicId(topicId);
            element.setCode(seed.code());
            element.setDisplayValue(seed.displayValue());
            element.setSortOrder(0);
            element.setResourceRefs(seed.resourceRefs());
            element.setSimilarityGroup(seed.similarityGroup());
            element.setStatus(ContentStatus.ACTIVE);
            element.setCreatedAt(LocalDateTime.now());
            recognitionElementRepository.save(element);

            markLoaded(key, file);
            count++;
            log.info("Loaded seed: {}", key);
        }

        return count;
    }

    private int loadRecognitionMemory() {
        String file = "22-memory-elements.json";
        var seeds = readSeedFile(file, new TypeReference<List<RecognitionMemoryElementSeed>>() {});

        int count = 0;

        Long topicId = resolveTopicId("Memoria");
        if(topicId == null){
            log.warn("Topic not found for memory element seed.");
            return 0;
        }

        for(var seed : seeds) {
            String key =  "recognition-element-memory:" + seed.code();
            if(alreadyLoaded(key)){
                log.debug("Skipping already loaded seed: {}", key);
                continue;
            }

            // Same prompt for every element: warm the audio cache with the text asked for at runtime.
            String nubiAudio = nubiAudioText(seed.resourceRefs(), null);
            if (nubiAudio != null) {
                audio.getAudio(AudioRequest.withPreset(nubiAudio, TonePreset.CALM));
            }

            var element = new RecognitionElement();
            element.setTopicId(topicId);
            element.setCode(seed.code());
            element.setDisplayValue(seed.displayValue());
            element.setSortOrder(0);
            element.setResourceRefs(seed.resourceRefs());
            element.setSimilarityGroup(seed.similarityGroup());
            element.setStatus(ContentStatus.ACTIVE);
            element.setCreatedAt(LocalDateTime.now());
            recognitionElementRepository.save(element);

            markLoaded(key, file);
            count++;
            log.info("Loaded seed: {}", key);
        }

        return count;
    }

    private int loadRecognitionColors() {
        String file = "19-recognition-elements-colors.json";
        var seeds = readSeedFile(file, new TypeReference<List<RecognitionColorElementSeed>>() {});

        int count = 0;

        Long topicId = resolveTopicId("Colores");
        if(topicId == null){
            log.warn("Topic not found for recognition element seed.");
            return 0;
        }

        for(var seed : seeds) {
            String key =  "recognition-element-color:" + seed.code();
            if(alreadyLoaded(key)){
                log.debug("Skipping already loaded seed: {}", key);
                continue;
            }

            audio.getAudio(AudioRequest.withPreset(nubiAudioText(seed.resourceRefs(), seed.nubi()), TonePreset.CALM));

            var element = new RecognitionElement();
            element.setTopicId(topicId);
            element.setCode(seed.code());
            element.setSortOrder(0);
            if (seed.resourceRefs() != null && !seed.resourceRefs().isBlank()) {
                element.setResourceRefs(seed.resourceRefs());
            } else {
                element.setResourceRefs(objectMapper.writeValueAsString(Map.of(
                    "nubi-audio", seed.nubi(),
                    "color", seed.color()
                )));
            }
            element.setStatus(ContentStatus.ACTIVE);
            element.setCreatedAt(LocalDateTime.now());
            recognitionElementRepository.save(element);

            markLoaded(key, file);
            count++;
            log.info("Loaded seed: {}", key);
        }

        return count;
    }


    private int loadRecognitionAlphaNumeric(boolean isNumbers) {
        String file = isNumbers ?
            "18-recognition-elements-numbers.json" : "17-recognition-elements-letters.json";
        var seeds = readSeedFile(file, new TypeReference<List<RecognitionAlphaNumericElementSeed>>() {});

        int count = 0;

        Long topicId = resolveTopicId(isNumbers ? "Números" : "Letras");
        if(topicId == null){
            log.warn("Topic not found for recognition element seed.");
            return 0;
        }

        for(var seed : seeds) {
            String key =  "recognition-element-" +
                (isNumbers ? "number:" : "letter:")
            + seed.code();
            if(alreadyLoaded(key)){
                log.debug("Skipping already loaded seed: {}", key);
                continue;
            }

            audio.getAudio(AudioRequest.withPreset(nubiAudioText(seed.resourceRefs(), seed.nubi()), TonePreset.CALM));

            var element = new RecognitionElement();
            element.setTopicId(topicId);
            element.setCode(seed.code());
            element.setSortOrder(0);
            if (seed.resourceRefs() != null && !seed.resourceRefs().isBlank()) {
                element.setResourceRefs(seed.resourceRefs());
            } else {
                element.setResourceRefs(objectMapper.writeValueAsString(Map.of(
                    "nubi-audio", seed.nubi()
                )));
            }
            element.setStatus(ContentStatus.ACTIVE);
            element.setCreatedAt(LocalDateTime.now());
            recognitionElementRepository.save(element);

            markLoaded(key, file);
            count++;
            log.info("Loaded seed: {}", key);
        }

        return count;
    }

    private int loadLetterSimilarityPairs() {
        String file = "18-letter-similarity-pairs.json";
        var seeds = readSeedFile(file, new TypeReference<List<LetterSimilarityPairSeed>>() {});
        int count = 0;
        for (var seed : seeds) {
            if (seed.pair() == null || seed.pair().length != 2) {
                log.warn("Invalid letter similarity pair seed, skipping");
                continue;
            }
            String codeA = seed.pair()[0];
            String codeB = seed.pair()[1];
            String key = "letter-similarity-pair:" + codeA + ":" + codeB;
            if (alreadyLoaded(key)) {
                log.debug("Skipping already loaded seed: {}", key);
                continue;
            }
            var entity = new RecognitionSimilarityPairJpaEntity("LETTER", codeA, codeB, seed.strength().toUpperCase());
            entity.setCreatedAt(LocalDateTime.now());
            recognitionSimilarityPairRepository.save(entity);
            markLoaded(key, file);
            count++;
            log.info("Loaded seed: {}", key);
        }
        return count;
    }

    private int loadNumberSimilarityPairs() {
        String file = "19-number-similarity-pairs.json";
        var seeds = readSeedFile(file, new TypeReference<List<LetterSimilarityPairSeed>>() {});
        int count = 0;
        for (var seed : seeds) {
            if (seed.pair() == null || seed.pair().length != 2) {
                log.warn("Invalid number similarity pair seed, skipping");
                continue;
            }
            String codeA = seed.pair()[0];
            String codeB = seed.pair()[1];
            String key = "number-similarity-pair:" + codeA + ":" + codeB;
            if (alreadyLoaded(key)) {
                log.debug("Skipping already loaded seed: {}", key);
                continue;
            }
            var entity = new RecognitionSimilarityPairJpaEntity("NUMBER", codeA, codeB, seed.strength().toUpperCase());
            entity.setCreatedAt(LocalDateTime.now());
            recognitionSimilarityPairRepository.save(entity);
            markLoaded(key, file);
            count++;
            log.info("Loaded seed: {}", key);
        }
        return count;
    }
}
