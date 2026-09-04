package es.vargontoc.educational.framework.tracking;

import es.vargontoc.educational.framework.tracking.model.ActivityAttempt;
import es.vargontoc.educational.framework.tracking.model.ActivitySummary;
import es.vargontoc.educational.framework.tracking.model.AttemptResult;
import es.vargontoc.educational.framework.tracking.model.ChildAchievement;
import es.vargontoc.educational.framework.tracking.model.ChildLearningCompletedStep;
import es.vargontoc.educational.framework.tracking.model.ChildLearningProgress;
import es.vargontoc.educational.framework.tracking.model.CuriosityViewed;
import es.vargontoc.educational.framework.tracking.model.TopicPerformanceBand;
import es.vargontoc.educational.framework.tracking.model.TopicSummary;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
@Transactional
@ActiveProfiles("dev")
class TrackingSchemaApplicationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            org.testcontainers.utility.DockerImageName.parse("pgvector/pgvector:pg16")
                    .asCompatibleSubstituteFor("postgres"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
        registry.add("spring.liquibase.enabled", () -> true);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("seed.enabled", () -> "false");
    }

    @Test
    void contextLoads() {
    }

    @Test
    void activityAttemptModel_canBeInstantiated() {
        ActivityAttempt attempt = new ActivityAttempt();
        assertNotNull(attempt);
        attempt.setResult(AttemptResult.CORRECT);
        assertTrue(attempt.getResult() == AttemptResult.CORRECT);
    }

    @Test
    void activitySummaryModel_canBeInstantiated() {
        ActivitySummary summary = new ActivitySummary();
        assertNotNull(summary);
    }

    @Test
    void topicSummaryModel_canBeInstantiated() {
        TopicSummary summary = new TopicSummary();
        assertNotNull(summary);
        summary.setPerformanceBand(TopicPerformanceBand.STRONG);
        assertTrue(summary.getPerformanceBand() == TopicPerformanceBand.STRONG);
    }

    @Test
    void curiosityViewedModel_canBeInstantiated() {
        CuriosityViewed viewed = new CuriosityViewed();
        assertNotNull(viewed);
    }

    @Test
    void childAchievementModel_canBeInstantiated() {
        ChildAchievement achievement = new ChildAchievement();
        assertNotNull(achievement);
    }

    @Test
    void childLearningProgressModel_canBeInstantiated() {
        ChildLearningProgress progress = new ChildLearningProgress();
        assertNotNull(progress);
    }

    @Test
    void childLearningCompletedStepModel_canBeInstantiated() {
        ChildLearningCompletedStep step = new ChildLearningCompletedStep();
        assertNotNull(step);
    }
}
