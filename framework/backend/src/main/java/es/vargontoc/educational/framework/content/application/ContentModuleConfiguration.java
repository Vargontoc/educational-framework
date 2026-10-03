package es.vargontoc.educational.framework.content.application;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import es.vargontoc.educational.framework.audio.application.ports.in.AudioUseCase;
import es.vargontoc.educational.framework.content.infrastructure.ActivityInformationPortImpl;
import es.vargontoc.educational.framework.content.infrastructure.persistence.ActivityJpaRepository;
import es.vargontoc.educational.framework.content.infrastructure.persistence.ActivityTopicJpaRepository;
import es.vargontoc.educational.framework.content.infrastructure.persistence.DevSeedStateJpaRepository;
import es.vargontoc.educational.framework.content.infrastructure.persistence.TopicJpaRepository;
import es.vargontoc.educational.framework.content.infrastructure.seed.SeedService;
import es.vargontoc.educational.framework.content.ports.in.ActivityUseCase;
import es.vargontoc.educational.framework.content.ports.in.DifficultyLevelUseCase;
import es.vargontoc.educational.framework.content.ports.in.WorldCatalogUseCase;
import es.vargontoc.educational.framework.content.ports.out.ActivityRepository;
import es.vargontoc.educational.framework.content.ports.out.ActivityResourceRepository;
import es.vargontoc.educational.framework.content.ports.out.AvatarEventCatalogRepository;
import es.vargontoc.educational.framework.content.ports.out.CategoryRepository;
import es.vargontoc.educational.framework.content.ports.out.ContentLocaleRepository;
import es.vargontoc.educational.framework.content.ports.out.DifficultyLevelRepository;
import es.vargontoc.educational.framework.content.ports.out.LearningPathRepository;
import es.vargontoc.educational.framework.content.ports.out.LearningPathStepRepository;
import es.vargontoc.educational.framework.content.ports.out.RecognitionElementRepository;
import es.vargontoc.educational.framework.content.ports.out.TopicRepository;
import es.vargontoc.educational.framework.content.ports.out.TracingPatternRepository;
import es.vargontoc.educational.framework.content.ports.out.WorldDiscoveryElementRepository;
import es.vargontoc.educational.framework.content.ports.out.WorldHostRepository;
import es.vargontoc.educational.framework.content.ports.out.WorldNarrativeSituationRepository;
import es.vargontoc.educational.framework.content.service.ActivityResourceService;
import es.vargontoc.educational.framework.content.service.ActivityService;
import es.vargontoc.educational.framework.content.service.AvatarEventCatalogService;
import es.vargontoc.educational.framework.content.service.ContentLocaleService;
import es.vargontoc.educational.framework.content.service.DifficultyLevelService;
import es.vargontoc.educational.framework.content.service.GameCatalogService;
import es.vargontoc.educational.framework.content.service.LearningPathService;
import es.vargontoc.educational.framework.content.service.LearningPathStepService;
import es.vargontoc.educational.framework.content.service.RecognitionElementService;
import es.vargontoc.educational.framework.content.service.TopicService;
import es.vargontoc.educational.framework.content.service.TracingPatternService;
import es.vargontoc.educational.framework.content.service.WorldCatalogService;
import es.vargontoc.educational.framework.game.infrastructure.persistence.RecognitionSimilarityPairJpaRepository;
import es.vargontoc.educational.framework.tracking.ports.out.ActivityInformationPort;
import es.vargontoc.educational.framework.tracking.ports.out.ActivitySummaryRepository;
import tools.jackson.databind.ObjectMapper;

@Configuration
class ContentModuleConfiguration {

    @Bean
    public TopicService topicService(TopicRepository topicRepository, CategoryRepository categoryRepository) {
        return new TopicService(topicRepository, categoryRepository);
    }

    @Bean
    public RecognitionElementService recognitionElementService(RecognitionElementRepository recognitionElementRepository) {
        return new RecognitionElementService(recognitionElementRepository);
    }

    @Bean
    public ActivityService activityService(ActivityRepository activityRepository, TopicRepository topicRepository) {
        return new ActivityService(activityRepository, topicRepository);
    }

    @Bean
    public DifficultyLevelService difficultyLevelService(DifficultyLevelRepository difficultyLevelRepository, ActivityRepository activityRepository) {
        return new DifficultyLevelService(difficultyLevelRepository, activityRepository);
    }

    @Bean
    public ActivityResourceService activityResourceService(ActivityResourceRepository activityResourceRepository, ActivityRepository activityRepository) {
        return new ActivityResourceService(activityResourceRepository, activityRepository);
    }

    @Bean
    public ContentLocaleService contentLocaleService(ContentLocaleRepository contentLocaleRepository) {
        return new ContentLocaleService(contentLocaleRepository);
    }


    @Bean
    public AvatarEventCatalogService avatarEventCatalogService(AvatarEventCatalogRepository avatarEventCatalogRepository) {
        return new AvatarEventCatalogService(avatarEventCatalogRepository);
    }

    @Bean
    public LearningPathService learningPathService(LearningPathRepository learningPathRepository) {
        return new LearningPathService(learningPathRepository);
    }

    @Bean
    public LearningPathStepService learningPathStepService(LearningPathStepRepository learningPathStepRepository, LearningPathRepository learningPathRepository, ActivityRepository activityRepository) {
        return new LearningPathStepService(learningPathStepRepository, learningPathRepository, activityRepository);
    }

    @Bean
    public TracingPatternService tracingPatternService(TracingPatternRepository tracingPatternRepository, TopicRepository topicRepository) {
        return new TracingPatternService(tracingPatternRepository, topicRepository);
    }


    @Bean
    public GameCatalogService gameCatalogService(
            ActivityUseCase activityUseCase,
            DifficultyLevelUseCase difficultyLevelUseCase,
            ActivitySummaryRepository activitySummaryRepository) {
        return new GameCatalogService(activityUseCase, difficultyLevelUseCase, activitySummaryRepository);
    }

    @Bean
    public WorldCatalogUseCase worldCatalogUseCase(
            WorldHostRepository worldHostRepository,
            WorldNarrativeSituationRepository worldNarrativeSituationRepository,
            WorldDiscoveryElementRepository worldDiscoveryElementRepository,
            ActivityRepository activityRepository,
            DifficultyLevelRepository difficultyLevelRepository) {
        return new WorldCatalogService(worldHostRepository, worldNarrativeSituationRepository,
            worldDiscoveryElementRepository, activityRepository, difficultyLevelRepository);
    }

    @Bean
    public SeedService seedService(
            AudioUseCase audio,
            DevSeedStateJpaRepository seedStateRepository,
            CategoryRepository categoryRepository,
            TopicRepository topicRepository,
            ActivityRepository activityRepository,
            DifficultyLevelRepository difficultyLevelRepository,
            AvatarEventCatalogRepository avatarEventCatalogRepository,
            LearningPathRepository learningPathRepository,
            LearningPathStepRepository learningPathStepRepository,
            TracingPatternRepository tracingPatternRepository,
            WorldHostRepository worldHostRepository, WorldDiscoveryElementRepository worldElementsRepository,
            RecognitionElementRepository recognitionElementRepository,
            RecognitionSimilarityPairJpaRepository recognitionSimilarityPairRepository,
            ObjectMapper objectMapper) {
        return new SeedService(audio, seedStateRepository, categoryRepository, topicRepository,
            activityRepository, difficultyLevelRepository, avatarEventCatalogRepository, learningPathRepository,
            learningPathStepRepository, tracingPatternRepository,
            worldHostRepository, worldElementsRepository,  recognitionElementRepository,
            recognitionSimilarityPairRepository, objectMapper);
    }

    @Bean
    public ActivityInformationPort activityInformationPort(
            ActivityJpaRepository activityJpaRepository,
            ActivityTopicJpaRepository activityTopicJpaRepository,
            TopicJpaRepository topicJpaRepository,
            DifficultyLevelRepository difficultyLevelRepository) {
        return new ActivityInformationPortImpl(activityJpaRepository, activityTopicJpaRepository, topicJpaRepository, difficultyLevelRepository);
    }
}
