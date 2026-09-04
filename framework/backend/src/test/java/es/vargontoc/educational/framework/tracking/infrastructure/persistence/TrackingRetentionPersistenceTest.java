package es.vargontoc.educational.framework.tracking.infrastructure.persistence;

import es.vargontoc.educational.framework.content.model.Activity;
import es.vargontoc.educational.framework.content.model.Category;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.DifficultyCode;
import es.vargontoc.educational.framework.content.model.DifficultyLevel;
import es.vargontoc.educational.framework.content.model.Topic;
import es.vargontoc.educational.framework.content.ports.out.ActivityRepository;
import es.vargontoc.educational.framework.content.ports.out.CategoryRepository;
import es.vargontoc.educational.framework.content.ports.out.DifficultyLevelRepository;
import es.vargontoc.educational.framework.content.ports.out.TopicRepository;
import es.vargontoc.educational.framework.family.ports.in.ChildProfileUseCase;
import es.vargontoc.educational.framework.family.ports.in.FamilyUseCase;
import es.vargontoc.educational.framework.session.ports.in.ChildSessionUseCase;
import es.vargontoc.educational.framework.tracking.model.ActivityAttempt;
import es.vargontoc.educational.framework.tracking.model.AttemptResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class TrackingRetentionPersistenceTest {

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
    }

    @Autowired
    private ActivityAttemptPersistenceAdapter adapter;

    @Autowired
    private ActivityAttemptJpaRepository jpaRepository;

    @Autowired
    private FamilyUseCase familyUseCase;

    @Autowired
    private ChildProfileUseCase childProfileUseCase;

    @Autowired
    private ChildSessionUseCase childSessionUseCase;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private DifficultyLevelRepository difficultyLevelRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long childProfileId;
    private Long childSessionId;
    private Long topicId;
    private Long activityId;
    private Long difficultyLevelId;

    @BeforeEach
    void setUp() {
        jpaRepository.deleteAll();

        if (!familyUseCase.familyExists()) {
            familyUseCase.createFamily("Retention Test Family", "1234", true, true);
        }
        var family = familyUseCase.getFamily();
        childProfileId = childProfileUseCase.createChild(
                family.getId(), "Retention Kid", LocalDate.now().minusYears(3), null, true, true, 100, null
        ).getId();
        childSessionId = childSessionUseCase.openSession(
                childProfileId, family.getId(), 30, "{\"ip\":\"test\",\"userAgent\":\"test\"}"
        ).getId();

        var category = new Category();
        category.setName("Retention Test Category");
        category.setStatus(ContentStatus.ACTIVE);
        category.setDisplayOrder(1);
        var savedCategory = categoryRepository.save(category);

        var topic = new Topic();
        topic.setCategoryId(savedCategory.getId());
        topic.setName("Retention Test Topic");
        topic.setStatus(ContentStatus.ACTIVE);
        topicId = topicRepository.save(topic).getId();

        var activity = new Activity();
        activity.setName("Retention Test Activity");
        activity.setStatus(ContentStatus.ACTIVE);
        activityId = activityRepository.save(activity).getId();

        var difficultyLevel = new DifficultyLevel();
        difficultyLevel.setActivityId(activityId);
        difficultyLevel.setDifficultyCode(DifficultyCode.EASY);
        difficultyLevelId = difficultyLevelRepository.save(difficultyLevel).getId();
    }

    @Test
    void deleteCreatedAtBefore_deletesOldAttempts() {
        saveAttemptWithCreatedAt(LocalDateTime.now().minusDays(200));
        saveAttemptWithCreatedAt(LocalDateTime.now().minusDays(10));

        var cutoff = LocalDateTime.now().minusDays(180);
        int deleted = adapter.deleteCreatedAtBefore(cutoff);

        assertEquals(1, deleted);
        assertEquals(1, jpaRepository.count());
    }

    @Test
    void deleteCreatedAtBefore_keepsRecentAttempts() {
        saveAttemptWithCreatedAt(LocalDateTime.now().minusDays(5));

        var cutoff = LocalDateTime.now().minusDays(180);
        adapter.deleteCreatedAtBefore(cutoff);

        assertEquals(1, jpaRepository.count());
    }

    @Test
    void deleteCreatedAtBefore_keepsSummariesAndProgress() {
        saveAttemptWithCreatedAt(LocalDateTime.now().minusDays(200));

        var cutoff = LocalDateTime.now().minusDays(180);
        adapter.deleteCreatedAtBefore(cutoff);

        assertEquals(0, jpaRepository.count());
    }

    /**
     * {@code created_at} is populated by Spring Data JPA auditing ({@code @CreatedDate}) on
     * insert, overriding whatever value the domain object carries — so backdating it for a
     * retention test requires a direct SQL update after the row exists.
     */
    private void saveAttemptWithCreatedAt(LocalDateTime createdAt) {
        var attempt = new ActivityAttempt();
        attempt.setChildProfileId(childProfileId);
        attempt.setActivityId(activityId);
        attempt.setChildSessionId(childSessionId);
        attempt.setTopicId(topicId);
        attempt.setDifficultyLevelId(difficultyLevelId);
        attempt.setResult(AttemptResult.CORRECT);
        attempt.setResponseTimeMs(5000);
        attempt.setAttemptContext("{\"engine\":\"test\"}");

        var saved = adapter.save(attempt);
        jpaRepository.flush();
        jdbcTemplate.update("UPDATE activity_attempt SET created_at = ? WHERE id = ?", createdAt, saved.getId());
    }
}
