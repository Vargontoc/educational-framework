package es.vargontoc.educational.framework.content.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
@Transactional
@ActiveProfiles("dev")
class DevContentControllerTest {

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
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void categories_list_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/dev/content/categories"))
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

        mockMvc.perform(post("/api/v1/dev/content/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.name", is("Mathematics")))
            .andExpect(jsonPath("$.data.status", is("ACTIVE")));

        mockMvc.perform(get("/api/v1/dev/content/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    void topics_list_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/dev/content/topics"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    void activities_list_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/dev/content/activities"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    void difficultyLevels_list_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/dev/content/difficulty-levels?activityId=1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    void activityResources_list_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/dev/content/activity-resources?activityId=1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    void locales_list_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/dev/content/locales?entityType=CATEGORY&entityId=1"))
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

        mockMvc.perform(post("/api/v1/dev/content/categories")
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

        mockMvc.perform(post("/api/v1/dev/content/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/dev/content/categories")
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

        mockMvc.perform(post("/api/v1/dev/content/topics")
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

        mockMvc.perform(post("/api/v1/dev/content/activities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success", is(false)));
    }

    // --- Curiosity tests ---

    @Test
    void curiosities_list_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/dev/content/curiosities"))
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

        mockMvc.perform(post("/api/v1/dev/content/curiosities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.text", is("Las mariposas prueban con sus patas")))
            .andExpect(jsonPath("$.data.status", is("ACTIVE")));

        mockMvc.perform(get("/api/v1/dev/content/curiosities"))
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

        mockMvc.perform(post("/api/v1/dev/content/curiosities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success", is(false)));
    }

    // --- Avatar Event Catalog tests ---

    @Test
    void avatarEvents_list_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/dev/content/avatar-events"))
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

        mockMvc.perform(post("/api/v1/dev/content/avatar-events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.eventType", is("ACTIVITY_COMPLETED")))
            .andExpect(jsonPath("$.data.tone", is("JOYFUL")))
            .andExpect(jsonPath("$.data.status", is("ACTIVE")));

        mockMvc.perform(get("/api/v1/dev/content/avatar-events"))
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

        mockMvc.perform(post("/api/v1/dev/content/avatar-events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success", is(false)));
    }
}
