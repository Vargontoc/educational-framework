package es.vargontoc.educational.framework.tracking.application;

import es.vargontoc.educational.framework.tracking.ports.in.RegisterActivityAttemptUseCase;
import es.vargontoc.educational.framework.tracking.service.ActivityAttemptService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class TrackingModuleConfiguration {

    @Bean
    RegisterActivityAttemptUseCase registerActivityAttemptUseCase(
            es.vargontoc.educational.framework.tracking.ports.out.ActivityAttemptRepository repository) {
        return new ActivityAttemptService(repository);
    }
}
