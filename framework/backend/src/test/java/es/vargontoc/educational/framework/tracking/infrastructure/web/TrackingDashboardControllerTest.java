package es.vargontoc.educational.framework.tracking.infrastructure.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.vargontoc.educational.framework.family.model.ChildProfile;
import es.vargontoc.educational.framework.family.model.Family;
import es.vargontoc.educational.framework.family.ports.in.ChildProfileUseCase;
import es.vargontoc.educational.framework.family.ports.in.FamilyUseCase;
import es.vargontoc.educational.framework.tracking.infrastructure.dto.ActivityPerformanceResponse;
import es.vargontoc.educational.framework.tracking.infrastructure.dto.ChildTrackingSummaryResponse;
import es.vargontoc.educational.framework.tracking.infrastructure.dto.DifficultyEvolutionResponse;
import es.vargontoc.educational.framework.tracking.infrastructure.dto.ResponseTimeMetricsResponse;
import es.vargontoc.educational.framework.tracking.model.ChildAchievement;
import es.vargontoc.educational.framework.tracking.model.ChildLearningProgress;
import es.vargontoc.educational.framework.tracking.model.TopicPerformanceBand;
import es.vargontoc.educational.framework.tracking.service.TrackingDashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
@Transactional
@ActiveProfiles("dev")
class TrackingDashboardControllerTest {

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

    @Autowired
    private TrackingDashboardService dashboardService;

    private String token;
    private Long childProfileId;
    private Long otherFamilyChildId;

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
                null,
                null,
                true,
                true
        );
        childProfileId = child.getId();

        var otherChild = childProfileUseCase.createChild(
                9999L,
                "Other Child",
                null,
                null,
                true,
                true
        );
        otherFamilyChildId = otherChild.getId();
    }

    private String loginAndReturnToken() throws Exception {
        var result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("pin", "1234"))))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("token").asText();
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

    @Test
    void getDashboard_unauthorizedChildProfile_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/tracking/children/{childProfileId}/summary", otherFamilyChildId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}
