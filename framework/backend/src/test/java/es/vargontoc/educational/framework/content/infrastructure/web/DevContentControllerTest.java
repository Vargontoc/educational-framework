package es.vargontoc.educational.framework.content.infrastructure.web;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

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
class DevContentControllerTest {

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

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        if (!familyUseCase.familyExists()) {
            familyUseCase.createFamily("Test Family", "1234", true, true);
        }
        token = loginAndReturnToken();
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

    private MockHttpServletRequestBuilder authGet(String url) {
        return get(url).header("Authorization", "Bearer " + token);
    }

    private MockHttpServletRequestBuilder authPost(String url) {
        return post(url).header("Authorization", "Bearer " + token);
    }

    @Test
    void categories_list_returns200() throws Exception {
        mockMvc.perform(authGet("/api/v1/dev/content/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    void categories_create_thenList() throws Exception {
        var body = objectMapper.writeValueAsString(Map.of(
            "name", "Mathematics",
            "description", "Math concepts",
            "status", "ACTIVE",
            "displayOrder", 1
        ));

        mockMvc.perform(authPost("/api/v1/dev/content/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.name", is("Mathematics")))
            .andExpect(jsonPath("$.data.status", is("ACTIVE")));

        mockMvc.perform(authGet("/api/v1/dev/content/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    void topics_list_returns200() throws Exception {
        mockMvc.perform(authGet("/api/v1/dev/content/topics"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    void activities_list_returns200() throws Exception {
        mockMvc.perform(authGet("/api/v1/dev/content/activities"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    void difficultyLevels_list_returns200() throws Exception {
        mockMvc.perform(authGet("/api/v1/dev/content/difficulty-levels?activityId=1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    void activityResources_list_returns200() throws Exception {
        mockMvc.perform(authGet("/api/v1/dev/content/activity-resources?activityId=1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    void locales_list_returns200() throws Exception {
        mockMvc.perform(authGet("/api/v1/dev/content/locales?entityType=CATEGORY&entityId=1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)));
    }

    // --- Negative / validation tests ---

    @Test
    void categories_create_blankName_returns400() throws Exception {
        var body = objectMapper.writeValueAsString(Map.of(
            "name", "  ",
            "status", "ACTIVE"
        ));

        mockMvc.perform(authPost("/api/v1/dev/content/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    void categories_create_duplicateName_returns409() throws Exception {
        var body = objectMapper.writeValueAsString(Map.of(
            "name", "Animals",
            "status", "ACTIVE",
            "displayOrder", 1
        ));

        mockMvc.perform(authPost("/api/v1/dev/content/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated());

        mockMvc.perform(authPost("/api/v1/dev/content/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    void topics_create_categoryNotFound_returns404() throws Exception {
        var body = objectMapper.writeValueAsString(Map.of(
            "name", "Dog",
            "categoryId", 9999,
            "status", "ACTIVE"
        ));

        mockMvc.perform(authPost("/api/v1/dev/content/topics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    void activities_create_blankName_returns400() throws Exception {
        var body = objectMapper.writeValueAsString(Map.of(
            "name", "",
            "status", "ACTIVE"
        ));

        mockMvc.perform(authPost("/api/v1/dev/content/activities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success", is(false)));
    }

    // --- Curiosity tests ---

    @Test
    void curiosities_list_returns200() throws Exception {
        mockMvc.perform(authGet("/api/v1/dev/content/curiosities"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    void curiosities_create_thenList() throws Exception {
        var body = objectMapper.writeValueAsString(Map.of(
            "text", "Las mariposas prueban con sus patas",
            "minAge", 3,
            "maxAge", 6,
            "tags", List.of("animales", "insectos"),
            "locale", "es-ES",
            "phoneticHint", "mariposas (mah-ree-POH-sas)",
            "status", "ACTIVE"
        ));

        mockMvc.perform(authPost("/api/v1/dev/content/curiosities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.text", is("Las mariposas prueban con sus patas")))
            .andExpect(jsonPath("$.data.status", is("ACTIVE")));

        mockMvc.perform(authGet("/api/v1/dev/content/curiosities"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    void curiosities_create_blankText_returns400() throws Exception {
        var body = objectMapper.writeValueAsString(Map.of(
            "text", "  ",
            "locale", "es-ES",
            "status", "ACTIVE"
        ));

        mockMvc.perform(authPost("/api/v1/dev/content/curiosities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success", is(false)));
    }

    // --- Avatar Event Catalog tests ---

    @Test
    void avatarEvents_list_returns200() throws Exception {
        mockMvc.perform(authGet("/api/v1/dev/content/avatar-events"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    void avatarEvents_create_thenList() throws Exception {
        var body = objectMapper.writeValueAsString(Map.of(
            "eventType", "ACTIVITY_COMPLETED",
            "tone", "JOYFUL",
            "locale", "es-ES",
            "messageText", "Has completado la actividad!",
            "status", "ACTIVE"
        ));

        mockMvc.perform(authPost("/api/v1/dev/content/avatar-events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.eventType", is("ACTIVITY_COMPLETED")))
            .andExpect(jsonPath("$.data.tone", is("JOYFUL")))
            .andExpect(jsonPath("$.data.status", is("ACTIVE")));

        mockMvc.perform(authGet("/api/v1/dev/content/avatar-events"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    void avatarEvents_create_blankMessage_returns400() throws Exception {
        var body = objectMapper.writeValueAsString(Map.of(
            "eventType", "ACTIVITY_COMPLETED",
            "tone", "JOYFUL",
            "locale", "es-ES",
            "messageText", "  ",
            "status", "ACTIVE"
        ));

        mockMvc.perform(authPost("/api/v1/dev/content/avatar-events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success", is(false)));
    }

    // --- Learning Path tests ---

    @Test
    void learningPaths_list_returns200() throws Exception {
        mockMvc.perform(authGet("/api/v1/dev/content/learning-paths"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    void learningPaths_create_thenList() throws Exception {
        var body = objectMapper.writeValueAsString(Map.of(
            "name", "Math Basics",
            "description", "Learn basic math",
            "minAge", 3,
            "maxAge", 6,
            "locale", "es-ES",
            "status", "ACTIVE"
        ));

        mockMvc.perform(authPost("/api/v1/dev/content/learning-paths")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.name", is("Math Basics")))
            .andExpect(jsonPath("$.data.status", is("ACTIVE")));

        mockMvc.perform(authGet("/api/v1/dev/content/learning-paths"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    void learningPaths_create_blankName_returns400() throws Exception {
        var body = objectMapper.writeValueAsString(Map.of(
            "name", "  ",
            "locale", "es-ES",
            "status", "ACTIVE"
        ));

        mockMvc.perform(authPost("/api/v1/dev/content/learning-paths")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success", is(false)));
    }

    // --- Tracing Pattern tests ---

    @Test
    void tracingPatterns_list_returns200() throws Exception {
        mockMvc.perform(authGet("/api/v1/dev/content/tracing-patterns"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    void tracingPatterns_create_thenList() throws Exception {
        var categoryBody = objectMapper.writeValueAsString(Map.of(
            "name", "Animals",
            "status", "ACTIVE",
            "displayOrder", 1
        ));
        var categoryCreateResult = mockMvc.perform(authPost("/api/v1/dev/content/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(categoryBody))
            .andExpect(status().isCreated())
            .andReturn();

        var categoryId = objectMapper.readTree(categoryCreateResult.getResponse().getContentAsString())
            .path("data").path("id").asLong();

        var topicCreateResult = mockMvc.perform(authPost("/api/v1/dev/content/topics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "name", "Dog",
                    "categoryId", categoryId,
                    "status", "ACTIVE"
                ))))
            .andExpect(status().isCreated())
            .andReturn();

        var topicId = objectMapper.readTree(topicCreateResult.getResponse().getContentAsString())
            .path("data").path("id").asLong();

        var body = objectMapper.writeValueAsString(Map.of(
            "topicId", topicId,
            "name", "Circulo basico",
            "description", "Patron circular simple",
            "patternType", "CURVE",
            "points", List.of(List.of(0.0, 0.5), List.of(0.5, 1.0), List.of(1.0, 0.5)),
            "minAge", 3,
            "maxAge", 6,
            "status", "ACTIVE"
        ));

        mockMvc.perform(authPost("/api/v1/dev/content/tracing-patterns")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.name", is("Circulo basico")))
            .andExpect(jsonPath("$.data.patternType", is("CURVE")))
            .andExpect(jsonPath("$.data.minAge", is(3)))
            .andExpect(jsonPath("$.data.maxAge", is(6)))
            .andExpect(jsonPath("$.data.status", is("ACTIVE")));

        mockMvc.perform(authGet("/api/v1/dev/content/tracing-patterns"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    void tracingPatterns_create_blankName_returns400() throws Exception {
        var body = objectMapper.writeValueAsString(Map.of(
            "topicId", 1,
            "name", "  ",
            "points", List.of(List.of(0.0, 0.0), List.of(1.0, 1.0)),
            "status", "ACTIVE"
        ));

        mockMvc.perform(authPost("/api/v1/dev/content/tracing-patterns")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success", is(false)));
    }

    // --- Learning Path Step tests ---

    @Test
    void learningPathSteps_create_thenList() throws Exception {
        var categoryBody = objectMapper.writeValueAsString(Map.of(
            "name", "Math",
            "status", "ACTIVE",
            "displayOrder", 1
        ));
        mockMvc.perform(authPost("/api/v1/dev/content/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(categoryBody))
            .andExpect(status().isCreated());

        var activityCreateResult = mockMvc.perform(authPost("/api/v1/dev/content/activities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "name", "Counting",
                    "status", "ACTIVE"
                ))))
            .andExpect(status().isCreated())
            .andReturn();

        var activityId = objectMapper.readTree(activityCreateResult.getResponse().getContentAsString())
            .path("data").path("id").asLong();

        var pathCreateResult = mockMvc.perform(authPost("/api/v1/dev/content/learning-paths")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "name", "Numbers Path",
                    "locale", "es-ES",
                    "status", "ACTIVE"
                ))))
            .andExpect(status().isCreated())
            .andReturn();

        var pathId = objectMapper.readTree(pathCreateResult.getResponse().getContentAsString())
            .path("data").path("id").asLong();

        var stepBody = objectMapper.writeValueAsString(Map.of(
            "activityId", activityId,
            "stepOrder", 1
        ));

        mockMvc.perform(authPost("/api/v1/dev/content/learning-paths/" + pathId + "/steps")
                .contentType(MediaType.APPLICATION_JSON)
                .content(stepBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.stepOrder", is(1)));

        mockMvc.perform(authGet("/api/v1/dev/content/learning-paths/" + pathId + "/steps"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    void learningPathSteps_create_duplicateOrder_returns409() throws Exception {
        var activityCreateResult = mockMvc.perform(authPost("/api/v1/dev/content/activities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "name", "Drawing",
                    "status", "ACTIVE"
                ))))
            .andExpect(status().isCreated())
            .andReturn();

        var activityId = objectMapper.readTree(activityCreateResult.getResponse().getContentAsString())
            .path("data").path("id").asLong();

        var pathCreateResult = mockMvc.perform(authPost("/api/v1/dev/content/learning-paths")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "name", "Art Path",
                    "locale", "es-ES",
                    "status", "ACTIVE"
                ))))
            .andExpect(status().isCreated())
            .andReturn();

        var pathId = objectMapper.readTree(pathCreateResult.getResponse().getContentAsString())
            .path("data").path("id").asLong();

        var stepBody = objectMapper.writeValueAsString(Map.of(
            "activityId", activityId,
            "stepOrder", 1
        ));

        mockMvc.perform(authPost("/api/v1/dev/content/learning-paths/" + pathId + "/steps")
                .contentType(MediaType.APPLICATION_JSON)
                .content(stepBody))
            .andExpect(status().isCreated());

        mockMvc.perform(authPost("/api/v1/dev/content/learning-paths/" + pathId + "/steps")
                .contentType(MediaType.APPLICATION_JSON)
                .content(stepBody))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    void learningPathSteps_create_learningPathNotFound_returns404() throws Exception {
        var body = objectMapper.writeValueAsString(Map.of(
            "activityId", 1,
            "stepOrder", 1
        ));

        mockMvc.perform(authPost("/api/v1/dev/content/learning-paths/9999/steps")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success", is(false)));
    }

}
