package es.vargontoc.educational.framework.tracking.application;

import es.vargontoc.educational.framework.tracking.config.AdaptiveDifficultyProperties;
import es.vargontoc.educational.framework.tracking.ports.in.RegisterActivityAttemptUseCase;
import es.vargontoc.educational.framework.tracking.ports.out.ActivityAttemptRepository;
import es.vargontoc.educational.framework.tracking.ports.out.ActivitySummaryRepository;
import es.vargontoc.educational.framework.tracking.ports.out.DifficultyLevelConfigPort;
import es.vargontoc.educational.framework.tracking.ports.out.DifficultyLevelNavigationPort;
import es.vargontoc.educational.framework.tracking.ports.out.TopicSummaryRepository;
import es.vargontoc.educational.framework.tracking.service.ActivityAttemptService;
import es.vargontoc.educational.framework.tracking.service.AdaptiveDifficultyService;
import es.vargontoc.educational.framework.tracking.service.SummaryUpdateService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AdaptiveDifficultyProperties.class)
class TrackingModuleConfiguration {

    @Bean
    SummaryUpdateService summaryUpdateService(
            ActivitySummaryRepository activitySummaryRepository,
            TopicSummaryRepository topicSummaryRepository) {
        return new SummaryUpdateService(activitySummaryRepository, topicSummaryRepository);
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
            AdaptiveDifficultyService adaptiveDifficultyService) {
        return new ActivityAttemptService(repository, summaryUpdateService, adaptiveDifficultyService);
    }
}
