package es.vargontoc.educational.framework.tracking.application;

import es.vargontoc.educational.framework.tracking.ports.in.RegisterActivityAttemptUseCase;
import es.vargontoc.educational.framework.tracking.ports.out.ActivityAttemptRepository;
import es.vargontoc.educational.framework.tracking.ports.out.ActivitySummaryRepository;
import es.vargontoc.educational.framework.tracking.ports.out.TopicSummaryRepository;
import es.vargontoc.educational.framework.tracking.service.ActivityAttemptService;
import es.vargontoc.educational.framework.tracking.service.SummaryUpdateService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class TrackingModuleConfiguration {

    @Bean
    SummaryUpdateService summaryUpdateService(
            ActivitySummaryRepository activitySummaryRepository,
            TopicSummaryRepository topicSummaryRepository) {
        return new SummaryUpdateService(activitySummaryRepository, topicSummaryRepository);
    }

    @Bean
    RegisterActivityAttemptUseCase registerActivityAttemptUseCase(
            ActivityAttemptRepository repository,
            SummaryUpdateService summaryUpdateService) {
        return new ActivityAttemptService(repository, summaryUpdateService);
    }
}
