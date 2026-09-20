package es.vargontoc.educational.framework.game.application;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import es.vargontoc.educational.framework.audio.application.ports.in.AudioUseCase;
import es.vargontoc.educational.framework.family.ports.in.ColorAdaptativeUseCase;
import es.vargontoc.educational.framework.content.ports.in.DifficultyLevelUseCase;
import es.vargontoc.educational.framework.content.ports.in.GameCatalogUseCase;
import es.vargontoc.educational.framework.content.ports.in.TopicUseCase;
import es.vargontoc.educational.framework.content.ports.out.RecognitionElementRepository;
import es.vargontoc.educational.framework.family.ports.in.ChildProfileUseCase;
import es.vargontoc.educational.framework.game.infrastructure.persistence.RecognitionSimilarityPairJpaRepository;
import es.vargontoc.educational.framework.game.model.recognition.RecognitionDifficultyConfig;
import es.vargontoc.educational.framework.game.ports.in.GameOrchestrator;
import es.vargontoc.educational.framework.game.ports.out.GameStateRegistry;
import es.vargontoc.educational.framework.game.ports.out.SessionAntiRepetitionRegistry;
import es.vargontoc.educational.framework.game.service.ColorSimilarityValidator;
import es.vargontoc.educational.framework.game.service.GameOrchestratorService;
import es.vargontoc.educational.framework.game.service.RecognitionDifficultyService;
import es.vargontoc.educational.framework.game.service.RecognitionSimilarityService;
import es.vargontoc.educational.framework.game.service.RoundAudioService;
import es.vargontoc.educational.framework.tracking.ports.in.EvaluateGameCompletionAchievementsUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.FilterAllowedRecognitionCategoriesUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.RegisterActivityAttemptUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.RegisterGameSessionSummaryUseCase;
import es.vargontoc.educational.framework.tracking.ports.out.ElementProgressPort;

@Configuration
@EnableConfigurationProperties(RecognitionProperties.class)
class GameModuleConfiguration {

    @Bean
    public GameOrchestrator gameOrchestrator(
            GameCatalogUseCase gameCatalogUseCase,
            GameStateRegistry gameStateRegistry,
            SessionAntiRepetitionRegistry sessionAntiRepetitionRegistry,
            RegisterActivityAttemptUseCase registerActivityAttemptUseCase,
            EvaluateGameCompletionAchievementsUseCase evaluateGameCompletionAchievementsUseCase,
            RegisterGameSessionSummaryUseCase registerGameSessionSummaryUseCase,
            ApplicationEventPublisher eventPublisher,
            TopicUseCase topicUseCase,
            FilterAllowedRecognitionCategoriesUseCase filterAllowedRecognitionCategoriesUseCase,
            ElementProgressPort elementProgressPort,
            RecognitionElementRepository recognitionElementRepository,
            DifficultyLevelUseCase difficultyLevelUseCase,
            @Lazy ChildProfileUseCase childProfileUseCase,
            RecognitionDifficultyService recognitionDifficultyService,
            RecognitionSimilarityService recognitionSimilarityService,
            RoundAudioService roundAudioService,
            ColorSimilarityValidator colorSimilarityValidator) {
        return new GameOrchestratorService(
            gameCatalogUseCase,
            gameStateRegistry,
            sessionAntiRepetitionRegistry,
            registerActivityAttemptUseCase,
            evaluateGameCompletionAchievementsUseCase,
            registerGameSessionSummaryUseCase,
            eventPublisher,
            topicUseCase,
            filterAllowedRecognitionCategoriesUseCase,
            elementProgressPort,
            recognitionElementRepository,
            difficultyLevelUseCase,
            childProfileUseCase,
            recognitionDifficultyService,
            recognitionSimilarityService,
            roundAudioService,
            colorSimilarityValidator
        );
    }

    @Bean
    public ColorSimilarityValidator colorSimilarityValidator(ColorAdaptativeUseCase colorAdaptativeUseCase) {
        return new ColorSimilarityValidator(colorAdaptativeUseCase);
    }

    @Bean
    public RecognitionSimilarityService recognitionSimilarityService(RecognitionSimilarityPairJpaRepository recognitionSimilarityPairJpaRepository) {
        return new RecognitionSimilarityService(recognitionSimilarityPairJpaRepository);
    }

    @Bean
    public RoundAudioService roundAudioService(
            RecognitionElementRepository recognitionElementRepository,
            @Lazy ChildProfileUseCase childProfileUseCase,
            AudioUseCase audioUseCase) {
        return new RoundAudioService(recognitionElementRepository, childProfileUseCase, audioUseCase);
    }

    @Bean
    public RecognitionDifficultyConfig recognitionDifficultyConfig(RecognitionProperties recognitionProperties) {
        return new RecognitionDifficultyConfig(recognitionProperties);
    }

    @Bean
    public RecognitionDifficultyService recognitionDifficultyService(RecognitionDifficultyConfig recognitionDifficultyConfig) {
        return new RecognitionDifficultyService(recognitionDifficultyConfig);
    }
}
