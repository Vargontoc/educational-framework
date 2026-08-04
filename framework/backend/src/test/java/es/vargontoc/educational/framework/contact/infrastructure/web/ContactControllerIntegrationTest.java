package es.vargontoc.educational.framework.contact.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.vargontoc.educational.framework.contact.infrastructure.persistence.ContactMessageJpaEntity;
import es.vargontoc.educational.framework.contact.infrastructure.persistence.ContactMessageJpaRepository;
import es.vargontoc.educational.framework.family.infrastructure.web.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ContactControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ContactMessageJpaRepository contactMessageJpaRepository;

    @Test
    void validMessage_returns202WithSentTrueAndTimestamp() throws Exception {
        var body = objectMapper.writeValueAsString(Map.of(
            "type", "COMMENT",
            "message", "Hola, esto es un comentario de prueba"
        ));

        mockMvc.perform(post("/api/v1/contact")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.sent", is(true)))
            .andExpect(jsonPath("$.data.timestamp", notNullValue()));
    }

    @Test
    void emptyMessage_returns400() throws Exception {
        var body = objectMapper.writeValueAsString(Map.of(
            "type", "COMMENT",
            "message", ""
        ));

        mockMvc.perform(post("/api/v1/contact")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest());
    }

    @Test
    void messageExceeding2000Characters_returns400() throws Exception {
        var longMessage = "a".repeat(2001);
        var body = objectMapper.writeValueAsString(Map.of(
            "type", "ERROR",
            "message", longMessage
        ));

        mockMvc.perform(post("/api/v1/contact")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest());
    }

    @Test
    void unknownFields_returns400() throws Exception {
        var body = """
            {"type":"COMMENT","message":"hello","extraField":"unexpected"}
            """;

        mockMvc.perform(post("/api/v1/contact")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest());
    }

    @Test
    void xssMessage_isSanitizedBeforeStorage() throws Exception {
        var body = objectMapper.writeValueAsString(Map.of(
            "type", "SUGGEST",
            "message", "<script>alert('xss')</script>hello"
        ));

        mockMvc.perform(post("/api/v1/contact")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isAccepted());

        var saved = contactMessageJpaRepository.findAll().stream()
            .filter(e -> "SUGGEST".equals(e.getType().name()))
            .findFirst()
            .orElseThrow();

        assertFalse(saved.getMessage().contains("<script>"));
        assertTrue(saved.getMessage().contains("hello"));
    }
}
