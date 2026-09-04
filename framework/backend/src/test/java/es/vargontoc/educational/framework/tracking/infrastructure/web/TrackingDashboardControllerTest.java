package es.vargontoc.educational.framework.tracking.infrastructure.web;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import es.vargontoc.educational.framework.family.ports.in.ChildProfileUseCase;
import es.vargontoc.educational.framework.family.ports.in.FamilyUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Single-family happy-path coverage for the tracking dashboard endpoints. Cross-family
 * authorization (403) is covered separately by {@link TrackingDashboardControllerAuthorizationTest},
 * a mocked unit test — {@code family} is a schema-enforced singleton table (SPRINT-087), so a
 * second real family row cannot be persisted here.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
@Transactional
@ActiveProfiles("dev")
class TrackingDashboardControllerTest {

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(
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

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FamilyUseCase familyUseCase;

    @Autowired
    private ChildProfileUseCase childProfileUseCase;

    private String token;
    private Long childProfileId;

    @BeforeEach
    void setUp() throws Exception {
        if (!familyUseCase.familyExists()) {
            familyUseCase.createFamily("Test Family", "1234", true, true);
        }
        token = loginAndReturnToken();

        var family = familyUseCase.getFamily();
        var child = childProfileUseCase.createChild(
                family.getId(),
                "Test Child",
                LocalDate.now().minusYears(4),
                null,
                true,
                true,
                100,
                null
        );
        childProfileId = child.getId();
    }

    private String loginAndReturnToken() throws Exception {
        var result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("pin", "1234"))))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("token").asString();
    }

    @Test
    void getSummary_returnsCorrectData() throws Exception {
        mockMvc.perform(get("/api/v1/tracking/children/{childProfileId}/summary", childProfileId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.childProfileId", is(childProfileId.intValue())));
    }

    @Test
    void getActivities_returnsActivityPerformance() throws Exception {
        mockMvc.perform(get("/api/v1/tracking/children/{childProfileId}/activities", childProfileId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    void getTopics_returnsTopicPerformance() throws Exception {
        mockMvc.perform(get("/api/v1/tracking/children/{childProfileId}/topics", childProfileId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    void getDifficulty_returnsEvolutionHistory() throws Exception {
        mockMvc.perform(get("/api/v1/tracking/children/{childProfileId}/difficulty", childProfileId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.childProfileId", is(childProfileId.intValue())));
    }

    @Test
    void getDifficulty_withActivityId_returnsEvolutionHistory() throws Exception {
        mockMvc.perform(get("/api/v1/tracking/children/{childProfileId}/difficulty", childProfileId)
                        .param("activityId", "1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.childProfileId", is(childProfileId.intValue())));
    }

    @Test
    void getResponseTime_returnsMetrics() throws Exception {
        mockMvc.perform(get("/api/v1/tracking/children/{childProfileId}/response-time", childProfileId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.childProfileId", is(childProfileId.intValue())));
    }

    @Test
    void getAchievements_returnsChildAchievements() throws Exception {
        mockMvc.perform(get("/api/v1/tracking/children/{childProfileId}/achievements", childProfileId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    void getLearningProgress_returnsAllOrFiltered() throws Exception {
        mockMvc.perform(get("/api/v1/tracking/children/{childProfileId}/learning-progress", childProfileId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(0)));
    }
}
