package es.vargontoc.educational.framework.content.infrastructure.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

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
class ProductiveStoryControllerTest {

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
        return root.path("data").path("token").asText();
    }

    @Test
    void listActiveStories_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/content/stories"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void getStoryDetail_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/content/stories/1"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void listActiveStories_withAuth_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/content/stories")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    void listActiveStories_onlyReturnsActiveStatus() throws Exception {
        createStory("Active Story", "ACTIVE");
        createStory("Draft Story", "DRAFT");
        createStory("Inactive Story", "INACTIVE");

        mockMvc.perform(get("/api/v1/content/stories")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data", hasSize(1)))
            .andExpect(jsonPath("$.data[0].title", is("Active Story")));
    }

    @Test
    void getStoryDetail_activeStory_returns200() throws Exception {
        var storyId = createStory("Test Story", "ACTIVE");

        mockMvc.perform(get("/api/v1/content/stories/" + storyId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.title", is("Test Story")));
    }

    @Test
    void getStoryDetail_nonActiveStory_returns404() throws Exception {
        var storyId = createStory("Draft Story", "DRAFT");

        mockMvc.perform(get("/api/v1/content/stories/" + storyId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isNotFound());
    }

    private Long createStory(String title, String status) throws Exception {
        var body = objectMapper.writeValueAsString(Map.of(
            "title", title,
            "description", "Test description",
            "minAge", 3,
            "maxAge", 6,
            "estimatedDurationMinutes", 5,
            "status", status
        ));

        var result = mockMvc.perform(post("/api/v1/dev/content/stories")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
            .path("data").path("id").asLong();
    }
}
