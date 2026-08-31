package es.vargontoc.educational.framework.content.application;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.vargontoc.educational.framework.content.infrastructure.ActivityInformationPortImpl;
import es.vargontoc.educational.framework.content.infrastructure.persistence.ActivityJpaRepository;
import es.vargontoc.educational.framework.content.infrastructure.persistence.DevSeedStateJpaRepository;
import es.vargontoc.educational.framework.content.infrastructure.seed.SeedService;
import es.vargontoc.educational.framework.content.ports.in.ActivityUseCase;
import es.vargontoc.educational.framework.content.ports.in.DifficultyLevelUseCase;
import es.vargontoc.educational.framework.content.ports.in.WorldCatalogUseCase;
import es.vargontoc.educational.framework.content.ports.out.AccessibleColorPaletteRepository;
import es.vargontoc.educational.framework.content.ports.out.AccessibleColorRepository;
import es.vargontoc.educational.framework.content.ports.out.ActivityRepository;
import es.vargontoc.educational.framework.content.ports.out.ActivityResourceRepository;
import es.vargontoc.educational.framework.content.ports.out.AvatarEventCatalogRepository;
import es.vargontoc.educational.framework.content.ports.out.CategoryRepository;
import es.vargontoc.educational.framework.content.ports.out.ContentLocaleRepository;
import es.vargontoc.educational.framework.content.ports.out.CuriosityRepository;
import es.vargontoc.educational.framework.content.ports.out.DifficultyLevelRepository;
import es.vargontoc.educational.framework.content.ports.out.LearningPathRepository;
import es.vargontoc.educational.framework.content.ports.out.LearningPathStepRepository;
import es.vargontoc.educational.framework.content.ports.out.TopicRepository;
import es.vargontoc.educational.framework.content.ports.out.TracingPatternRepository;
import es.vargontoc.educational.framework.content.ports.out.WorldDiscoveryElementRepository;
import es.vargontoc.educational.framework.content.ports.out.WorldHostRepository;
import es.vargontoc.educational.framework.content.ports.out.WorldNarrativeSituationRepository;
import es.vargontoc.educational.framework.content.service.ActivityResourceService;
import es.vargontoc.educational.framework.content.service.ActivityService;
import es.vargontoc.educational.framework.content.service.AvatarEventCatalogService;
import es.vargontoc.educational.framework.content.service.CategoryService;
import es.vargontoc.educational.framework.content.service.ContentLocaleService;
import es.vargontoc.educational.framework.content.service.CuriosityService;
import es.vargontoc.educational.framework.content.service.DifficultyLevelService;
import es.vargontoc.educational.framework.content.service.GameCatalogService;
import es.vargontoc.educational.framework.content.service.LearningPathService;
import es.vargontoc.educational.framework.content.service.LearningPathStepService;
import es.vargontoc.educational.framework.content.service.TopicService;
import es.vargontoc.educational.framework.content.service.TracingPatternService;
import es.vargontoc.educational.framework.content.service.WorldCatalogService;
import es.vargontoc.educational.framework.tracking.ports.out.ActivityInformationPort;
import es.vargontoc.educational.framework.tracking.ports.out.ActivitySummaryRepository;

@Configuration
class ContentModuleConfiguration {

    @Bean
    CategoryService categoryService(CategoryRepository categoryRepository) {
        return new CategoryService(categoryRepository);
    }

    @Bean
    TopicService topicService(TopicRepository topicRepository, CategoryRepository categoryRepository) {
        return new TopicService(topicRepository, categoryRepository);
    }

    @Bean
    ActivityService activityService(ActivityRepository activityRepository, TopicRepository topicRepository) {
        return new ActivityService(activityRepository, topicRepository);
    }

    @Bean
    DifficultyLevelService difficultyLevelService(DifficultyLevelRepository difficultyLevelRepository, ActivityRepository activityRepository) {
        return new DifficultyLevelService(difficultyLevelRepository, activityRepository);
    }

    @Bean
    ActivityResourceService activityResourceService(ActivityResourceRepository activityResourceRepository, ActivityRepository activityRepository) {
        return new ActivityResourceService(activityResourceRepository, activityRepository);
    }

    @Bean
    ContentLocaleService contentLocaleService(ContentLocaleRepository contentLocaleRepository) {
        return new ContentLocaleService(contentLocaleRepository);
    }

    @Bean
    CuriosityService curiosityService(CuriosityRepository curiosityRepository, TopicRepository topicRepository) {
        return new CuriosityService(curiosityRepository, topicRepository);
    }

    @Bean
    AvatarEventCatalogService avatarEventCatalogService(AvatarEventCatalogRepository avatarEventCatalogRepository) {
        return new AvatarEventCatalogService(avatarEventCatalogRepository);
    }

    @Bean
    LearningPathService learningPathService(LearningPathRepository learningPathRepository) {
        return new LearningPathService(learningPathRepository);
    }

    @Bean
    LearningPathStepService learningPathStepService(LearningPathStepRepository learningPathStepRepository, LearningPathRepository learningPathRepository, ActivityRepository activityRepository) {
        return new LearningPathStepService(learningPathStepRepository, learningPathRepository, activityRepository);
    }

    @Bean
    TracingPatternService tracingPatternService(TracingPatternRepository tracingPatternRepository, TopicRepository topicRepository) {
        return new TracingPatternService(tracingPatternRepository, topicRepository);
    }


    @Bean
    GameCatalogService gameCatalogService(
            ActivityUseCase activityUseCase,
            DifficultyLevelUseCase difficultyLevelUseCase,
            ActivitySummaryRepository activitySummaryRepository) {
        return new GameCatalogService(activityUseCase, difficultyLevelUseCase, activitySummaryRepository);
    }

    @Bean
    WorldCatalogUseCase worldCatalogUseCase(
            WorldHostRepository worldHostRepository,
            WorldNarrativeSituationRepository worldNarrativeSituationRepository,
            WorldDiscoveryElementRepository worldDiscoveryElementRepository,
            ActivityRepository activityRepository,
            DifficultyLevelRepository difficultyLevelRepository) {
        return new WorldCatalogService(worldHostRepository, worldNarrativeSituationRepository,
            worldDiscoveryElementRepository, activityRepository, difficultyLevelRepository);
    }

    @Bean
    SeedService seedService(
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
            WorldDiscoveryElementRepository worldDiscoveryElementRepository,
            AccessibleColorRepository accessibleColorRepository,
            AccessibleColorPaletteRepository accessibleColorPaletteRepository,
            ObjectMapper objectMapper) {
        return new SeedService(seedStateRepository, categoryRepository, topicRepository, curiosityRepository,
            activityRepository, difficultyLevelRepository, avatarEventCatalogRepository, learningPathRepository,
            learningPathStepRepository, tracingPatternRepository,
            worldHostRepository, worldNarrativeSituationRepository, worldDiscoveryElementRepository,
            accessibleColorRepository, accessibleColorPaletteRepository, objectMapper);
    }

    @Bean
    ActivityInformationPort activityInformationPort(
            ActivityJpaRepository activityJpaRepository) {
        return new ActivityInformationPortImpl(activityJpaRepository);
    }
}
