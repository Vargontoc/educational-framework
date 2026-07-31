package es.vargontoc.educational.framework.tracking.infrastructure.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.vargontoc.educational.framework.family.infrastructure.persistence.FamilyJpaEntity;
import es.vargontoc.educational.framework.family.infrastructure.persistence.FamilyJpaRepository;
import es.vargontoc.educational.framework.family.ports.in.ChildProfileUseCase;
import es.vargontoc.educational.framework.family.ports.in.FamilyUseCase;
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

import java.time.LocalDate;
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
    private FamilyJpaRepository familyJpaRepository;

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
                LocalDate.of(2015, 6, 15),
                null,
                true,
                true,
                null
        );
        childProfileId = child.getId();

        var otherFamily = new FamilyJpaEntity();
        otherFamily.setName("Other Family");
        otherFamily.setPinHash("unused-hash");
        var savedOtherFamily = familyJpaRepository.save(otherFamily);

        var otherChild = childProfileUseCase.createChild(
                savedOtherFamily.getId(),
                "Other Child",
                LocalDate.of(2018, 1, 1),
                null,
                true,
                true,
                null
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

    @Test
    void getActivities_unauthorizedChildProfile_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/tracking/children/{childProfileId}/activities", otherFamilyChildId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void getTopics_unauthorizedChildProfile_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/tracking/children/{childProfileId}/topics", otherFamilyChildId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void getDifficulty_unauthorizedChildProfile_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/tracking/children/{childProfileId}/difficulty", otherFamilyChildId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void getResponseTime_unauthorizedChildProfile_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/tracking/children/{childProfileId}/response-time", otherFamilyChildId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAchievements_unauthorizedChildProfile_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/tracking/children/{childProfileId}/achievements", otherFamilyChildId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void getLearningProgress_unauthorizedChildProfile_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/tracking/children/{childProfileId}/learning-progress", otherFamilyChildId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}
