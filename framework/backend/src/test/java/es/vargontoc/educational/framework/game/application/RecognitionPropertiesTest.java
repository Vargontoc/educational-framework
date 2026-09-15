package es.vargontoc.educational.framework.game.application;

import es.vargontoc.educational.framework.content.model.DifficultyCode;
import es.vargontoc.educational.framework.family.model.ColorVisionMode;
import es.vargontoc.educational.framework.game.model.enums.RecognitionCategory;
import es.vargontoc.educational.framework.game.model.recognition.DistractorStrategy;
import es.vargontoc.educational.framework.game.model.recognition.RecognitionDifficultyConfig;
import es.vargontoc.educational.framework.game.model.recognition.RoundParameters;
import es.vargontoc.educational.framework.game.service.RecognitionDifficultyService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;

class RecognitionPropertiesTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withUserConfiguration(TestConfig.class);

    @EnableConfigurationProperties(RecognitionProperties.class)
    static class TestConfig {
    }

    @Test
    void defaults_appliedWhenNoYamlOverride() {
        contextRunner.run(context -> {
            RecognitionProperties properties = context.getBean(RecognitionProperties.class);

            assertEquals(92, properties.getTouchTargetSizePx());

            assertEquals(2, properties.getDifficulty().getEasy().getOptionCount());
            assertEquals(500, properties.getDifficulty().getEasy().getTouchEnableDelayMs());
            assertTrue(properties.getDifficulty().getEasy().isGuideChromEnabled());

            assertEquals(3, properties.getDifficulty().getMedium().getOptionCount());
            assertEquals(800, properties.getDifficulty().getMedium().getTouchEnableDelayMs());
            assertFalse(properties.getDifficulty().getMedium().isGuideChromEnabled());

            assertEquals(4, properties.getDifficulty().getHard().getOptionCount());
            assertEquals(0, properties.getDifficulty().getHard().getTouchEnableDelayMs());
            assertFalse(properties.getDifficulty().getHard().isGuideChromEnabled());
        });
    }

    @Test
    void customValues_bindFromProperties() {
        contextRunner
                .withPropertyValues(
                        "app.recognition.touch-target-size-px=96",
                        "app.recognition.difficulty.easy.option-count=3",
                        "app.recognition.difficulty.easy.touch-enable-delay-ms=250",
                        "app.recognition.difficulty.easy.guide-chrom-enabled=false",
                        "app.recognition.difficulty.hard.option-count=5")
                .run(context -> {
                    RecognitionProperties properties = context.getBean(RecognitionProperties.class);

                    assertEquals(96, properties.getTouchTargetSizePx());
                    assertEquals(3, properties.getDifficulty().getEasy().getOptionCount());
                    assertEquals(250, properties.getDifficulty().getEasy().getTouchEnableDelayMs());
                    assertFalse(properties.getDifficulty().getEasy().isGuideChromEnabled());
                    assertEquals(5, properties.getDifficulty().getHard().getOptionCount());
                });
    }

    @Test
    void invalidOptionCount_failsContextStartup() {
        contextRunner
                .withPropertyValues("app.recognition.difficulty.easy.option-count=0")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void invalidTouchTargetSizePx_failsContextStartup() {
        contextRunner
                .withPropertyValues("app.recognition.touch-target-size-px=0")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void negativeTouchEnableDelayMs_failsContextStartup() {
        contextRunner
                .withPropertyValues("app.recognition.difficulty.hard.touch-enable-delay-ms=-1")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void resolveRoundParameters_reflectsYamlOverriddenProperties() {
        contextRunner
                .withPropertyValues(
                        "app.recognition.difficulty.medium.option-count=6",
                        "app.recognition.difficulty.medium.touch-enable-delay-ms=999",
                        "app.recognition.difficulty.medium.guide-chrom-enabled=true")
                .run(context -> {
                    RecognitionProperties properties = context.getBean(RecognitionProperties.class);
                    RecognitionDifficultyService service =
                            new RecognitionDifficultyService(new RecognitionDifficultyConfig(properties));

                    RoundParameters result = service.resolveRoundParameters(
                            DifficultyCode.MEDIUM, RecognitionCategory.LETTER, ColorVisionMode.NONE);

                    assertEquals(6, result.optionCount());
                    assertEquals(999, result.touchEnableDelayMs());
                    assertTrue(result.guideChromEnabled());
                    assertEquals(DistractorStrategy.SAME_CATEGORY, result.distractorStrategy());
                });
    }
}
