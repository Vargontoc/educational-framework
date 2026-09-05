package es.vargontoc.educational.framework.tracking.application;

import es.vargontoc.educational.framework.content.ports.out.LearningPathRepository;
import es.vargontoc.educational.framework.content.ports.out.LearningPathStepRepository;
import es.vargontoc.educational.framework.content.ports.out.RecognitionElementRepository;
import es.vargontoc.educational.framework.content.ports.out.TopicRepository;
import es.vargontoc.educational.framework.tracking.config.AdaptiveDifficultyProperties;
import es.vargontoc.educational.framework.tracking.config.ElementMasteryProperties;
import es.vargontoc.educational.framework.tracking.config.NumberUnlockProperties;
import es.vargontoc.educational.framework.tracking.ports.in.EvaluateGameCompletionAchievementsUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.GetActivityEngagementSummaryUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.RegisterActivityAttemptUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.RegisterChildAchievementUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.RegisterGameSessionSummaryUseCase;
import es.vargontoc.educational.framework.tracking.ports.out.ActivityAttemptRepository;
import es.vargontoc.educational.framework.tracking.ports.out.ActivityInformationPort;
import es.vargontoc.educational.framework.tracking.ports.out.ActivityProposalLogRepository;
import es.vargontoc.educational.framework.tracking.ports.out.ActivitySummaryRepository;
import es.vargontoc.educational.framework.tracking.ports.out.ChildAchievementRepository;
import es.vargontoc.educational.framework.tracking.ports.out.ChildLearningCompletedStepRepository;
import es.vargontoc.educational.framework.tracking.ports.out.ChildLearningProgressRepository;
import es.vargontoc.educational.framework.tracking.ports.out.CuriosityViewedRepository;
import es.vargontoc.educational.framework.tracking.ports.out.DifficultyEvolutionRepository;
import es.vargontoc.educational.framework.tracking.ports.out.DifficultyLevelConfigPort;
import es.vargontoc.educational.framework.tracking.ports.out.DifficultyLevelNavigationPort;
import es.vargontoc.educational.framework.tracking.ports.out.ElementProgressPort;
import es.vargontoc.educational.framework.tracking.ports.out.ElementSummaryRepository;
import es.vargontoc.educational.framework.tracking.ports.out.GameSessionSummaryRepository;
import es.vargontoc.educational.framework.tracking.ports.out.TopicSummaryRepository;
import es.vargontoc.educational.framework.tracking.service.ActivityAttemptService;
import es.vargontoc.educational.framework.tracking.service.ActivityEngagementSummaryService;
import es.vargontoc.educational.framework.tracking.service.ActivityProposalLogService;
import es.vargontoc.educational.framework.tracking.service.AdaptiveDifficultyService;
import es.vargontoc.educational.framework.tracking.service.AchievementEvaluationService;
import es.vargontoc.educational.framework.tracking.service.ChildAchievementService;
import es.vargontoc.educational.framework.tracking.service.ChildLearningProgressService;
import es.vargontoc.educational.framework.tracking.service.CuriosityViewedService;
import es.vargontoc.educational.framework.tracking.service.GameCompletionAchievementService;
import es.vargontoc.educational.framework.tracking.service.GameSessionSummaryService;
import es.vargontoc.educational.framework.tracking.service.NumberUnlockReadinessService;
import es.vargontoc.educational.framework.tracking.service.SummaryUpdateService;
import es.vargontoc.educational.framework.tracking.service.TopicSelectionService;
import es.vargontoc.educational.framework.tracking.service.TrackingDashboardService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({AdaptiveDifficultyProperties.class, NumberUnlockProperties.class, ElementMasteryProperties.class})
class TrackingModuleConfiguration {

    @Bean
    SummaryUpdateService summaryUpdateService(
            ActivitySummaryRepository activitySummaryRepository,
            TopicSummaryRepository topicSummaryRepository,
            ElementSummaryRepository elementSummaryRepository,
            ElementMasteryProperties elementMasteryProperties,
            RecognitionElementRepository recognitionElementRepository) {
        return new SummaryUpdateService(activitySummaryRepository, topicSummaryRepository, elementSummaryRepository, elementMasteryProperties, recognitionElementRepository);
    }

    @Bean
    AdaptiveDifficultyService adaptiveDifficultyService(
            ActivityAttemptRepository attemptRepository,
            ActivitySummaryRepository summaryRepository,
            DifficultyLevelConfigPort difficultyLevelConfigPort,
            DifficultyLevelNavigationPort difficultyNavigationPort,
            AdaptiveDifficultyProperties properties) {
        return new AdaptiveDifficultyService(
                attemptRepository,
                summaryRepository,
                difficultyLevelConfigPort,
                difficultyNavigationPort,
                properties);
    }

    @Bean
    RegisterActivityAttemptUseCase registerActivityAttemptUseCase(
            ActivityAttemptRepository repository,
            SummaryUpdateService summaryUpdateService,
            AdaptiveDifficultyService adaptiveDifficultyService,
            AchievementEvaluationService achievementEvaluationService) {
        return new ActivityAttemptService(repository, summaryUpdateService, adaptiveDifficultyService, achievementEvaluationService);
    }

    @Bean
    AchievementEvaluationService achievementEvaluationService(
            ActivityAttemptRepository attemptRepository,
            RegisterChildAchievementUseCase achievementService) {
        return new AchievementEvaluationService(attemptRepository, achievementService);
    }

    @Bean
    EvaluateGameCompletionAchievementsUseCase evaluateGameCompletionAchievementsUseCase(
            AchievementEvaluationService achievementEvaluationService) {
        return new GameCompletionAchievementService(achievementEvaluationService);
    }

    @Bean
    RegisterGameSessionSummaryUseCase registerGameSessionSummaryUseCase(
            GameSessionSummaryRepository repository) {
        return new GameSessionSummaryService(repository);
    }

    @Bean
    CuriosityViewedService curiosityViewedService(CuriosityViewedRepository curiosityViewedRepository) {
        return new CuriosityViewedService(curiosityViewedRepository);
    }

    @Bean
    ChildAchievementService childAchievementService(ChildAchievementRepository childAchievementRepository) {
        return new ChildAchievementService(childAchievementRepository);
    }

    @Bean
    ChildLearningProgressService childLearningProgressService(
            ChildLearningProgressRepository progressRepository,
            ChildLearningCompletedStepRepository completedStepRepository,
            LearningPathRepository learningPathRepository,
            LearningPathStepRepository learningPathStepRepository) {
        return new ChildLearningProgressService(progressRepository, completedStepRepository, learningPathRepository, learningPathStepRepository);
    }

    @Bean
    TopicSelectionService topicSelectionService(TopicSummaryRepository topicSummaryRepository) {
        return new TopicSelectionService(topicSummaryRepository);
    }

    @Bean
    NumberUnlockReadinessService numberUnlockReadinessService(
            TopicSummaryRepository topicSummaryRepository,
            TopicRepository topicRepository,
            NumberUnlockProperties numberUnlockProperties) {
        return new NumberUnlockReadinessService(topicSummaryRepository, topicRepository, numberUnlockProperties);
    }

    @Bean
    TrackingDashboardService trackingDashboardService(
            ActivitySummaryRepository activitySummaryRepository,
            TopicSummaryRepository topicSummaryRepository,
            ChildAchievementRepository childAchievementRepository,
            ChildLearningProgressRepository childLearningProgressRepository,
            ChildLearningCompletedStepRepository childLearningCompletedStepRepository,
            DifficultyEvolutionRepository difficultyEvolutionRepository) {
        return new TrackingDashboardService(
                activitySummaryRepository,
                topicSummaryRepository,
                childAchievementRepository,
                childLearningProgressRepository,
                childLearningCompletedStepRepository,
                difficultyEvolutionRepository);
    }

    @Bean
    ActivityProposalLogService activityProposalLogService(ActivityProposalLogRepository repository) {
        return new ActivityProposalLogService(repository);
    }

    @Bean
    GetActivityEngagementSummaryUseCase getActivityEngagementSummaryUseCase(
            ActivityProposalLogRepository proposalLogRepository,
            GameSessionSummaryRepository sessionSummaryRepository,
            ActivityInformationPort activityInformationPort) {
        return new ActivityEngagementSummaryService(proposalLogRepository, sessionSummaryRepository, activityInformationPort);
    }

    @Bean
    ElementProgressPort elementProgressPort(
            ElementSummaryRepository elementSummaryRepository,
            RecognitionElementRepository recognitionElementRepository) {
        return new es.vargontoc.educational.framework.tracking.infrastructure.persistence.ElementProgressPortAdapter(
                elementSummaryRepository, recognitionElementRepository);
    }
}
