package es.vargontoc.educational.framework.content.infrastructure.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class DevContentControllerDisabledTest {

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

    @Test
    void categories_endpoint_notAvailable_withoutDevProfile() throws Exception {
        mockMvc.perform(get("/api/v1/dev/content/categories"))
            .andExpect(status().isNotFound());
    }

    @Test
    void topics_endpoint_notAvailable_withoutDevProfile() throws Exception {
        mockMvc.perform(get("/api/v1/dev/content/topics"))
            .andExpect(status().isNotFound());
    }

    @Test
    void activities_endpoint_notAvailable_withoutDevProfile() throws Exception {
        mockMvc.perform(get("/api/v1/dev/content/activities"))
            .andExpect(status().isNotFound());
    }

    @Test
    void curiosities_endpoint_notAvailable_withoutDevProfile() throws Exception {
        mockMvc.perform(get("/api/v1/dev/content/curiosities"))
            .andExpect(status().isNotFound());
    }

    @Test
    void avatarEvents_endpoint_notAvailable_withoutDevProfile() throws Exception {
        mockMvc.perform(get("/api/v1/dev/content/avatar-events"))
            .andExpect(status().isNotFound());
    }

    @Test
    void learningPaths_endpoint_notAvailable_withoutDevProfile() throws Exception {
        mockMvc.perform(get("/api/v1/dev/content/learning-paths"))
            .andExpect(status().isNotFound());
    }

    @Test
    void tracingPatterns_endpoint_notAvailable_withoutDevProfile() throws Exception {
        mockMvc.perform(get("/api/v1/dev/content/tracing-patterns"))
            .andExpect(status().isNotFound());
    }

    @Test
    void stories_endpoint_notAvailable_withoutDevProfile() throws Exception {
        mockMvc.perform(get("/api/v1/dev/content/stories"))
            .andExpect(status().isNotFound());
    }
}
