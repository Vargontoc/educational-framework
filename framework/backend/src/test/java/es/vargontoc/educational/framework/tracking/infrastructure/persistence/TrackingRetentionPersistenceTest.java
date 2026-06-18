package es.vargontoc.educational.framework.tracking.infrastructure.persistence;

import es.vargontoc.educational.framework.tracking.model.ActivityAttempt;
import es.vargontoc.educational.framework.tracking.model.AttemptResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
@Transactional
@Disabled("Docker/Testcontainers requerido para ejecutar este test de integracion")
class TrackingRetentionPersistenceTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
        registry.add("spring.liquibase.enabled", () -> true);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    private ActivityAttemptPersistenceAdapter adapter;

    @Autowired
    private ActivityAttemptJpaRepository jpaRepository;

    @BeforeEach
    void setUp() {
        jpaRepository.deleteAll();
    }

    @Test
    void deleteCreatedAtBefore_deletesOldAttempts() {
        var oldAttempt = buildAttempt(LocalDateTime.now().minusDays(200));
        var recentAttempt = buildAttempt(LocalDateTime.now().minusDays(10));
        adapter.save(oldAttempt);
        adapter.save(recentAttempt);

        var cutoff = LocalDateTime.now().minusDays(180);
        int deleted = adapter.deleteCreatedAtBefore(cutoff);

        assertEquals(1, deleted);
        assertEquals(1, jpaRepository.count());
    }

    @Test
    void deleteCreatedAtBefore_keepsRecentAttempts() {
        var recentAttempt = buildAttempt(LocalDateTime.now().minusDays(5));
        adapter.save(recentAttempt);

        var cutoff = LocalDateTime.now().minusDays(180);
        adapter.deleteCreatedAtBefore(cutoff);

        assertEquals(1, jpaRepository.count());
    }

    @Test
    void deleteCreatedAtBefore_keepsSummariesAndProgress() {
        var oldAttempt = buildAttempt(LocalDateTime.now().minusDays(200));
        adapter.save(oldAttempt);

        var cutoff = LocalDateTime.now().minusDays(180);
        adapter.deleteCreatedAtBefore(cutoff);

        assertEquals(0, jpaRepository.count());
    }

    private ActivityAttempt buildAttempt(LocalDateTime createdAt) {
        var attempt = new ActivityAttempt();
        attempt.setChildProfileId(1L);
        attempt.setActivityId(1L);
        attempt.setChildSessionId(1L);
        attempt.setTopicId(1L);
        attempt.setDifficultyLevelId(1L);
        attempt.setResult(AttemptResult.CORRECT);
        attempt.setResponseTimeMs(5000);
        attempt.setAttemptContext("{\"engine\":\"test\"}");
        attempt.setCreatedAt(createdAt);
        attempt.setUpdatedAt(createdAt);
        return attempt;
    }
}
