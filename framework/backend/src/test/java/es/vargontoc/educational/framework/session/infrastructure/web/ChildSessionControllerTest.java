package es.vargontoc.educational.framework.session.infrastructure.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.vargontoc.educational.framework.family.infrastructure.web.AbstractIntegrationTest;
import es.vargontoc.educational.framework.family.ports.in.ChildProfileUseCase;
import es.vargontoc.educational.framework.family.ports.in.FamilyUseCase;
import es.vargontoc.educational.framework.session.ports.in.ChildSessionUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChildSessionControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FamilyUseCase familyUseCase;

    @Autowired
    private ChildProfileUseCase childProfileUseCase;

    @Autowired
    private ChildSessionUseCase childSessionUseCase;

    private Long childProfileId;
    private String token;

    @BeforeEach
    void setUp() throws Exception {
        var family = familyUseCase.familyExists()
            ? familyUseCase.getFamily()
            : familyUseCase.createFamily("Family One", "1234", true, true);
        childProfileId = childProfileUseCase.createChild(
            family.getId(),
            "Kid One",
            LocalDate.now().minusYears(8),
            "avatar-1",
            true,
            true,
            100,
            null
        ).getId();
        token = loginAndReturnToken();
    }

    @Test
    void openSessionReturns201() throws Exception {
        mockMvc.perform(post("/api/v1/sessions/children")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(openBody(childProfileId)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.childProfileId", is(childProfileId.intValue())))
            .andExpect(jsonPath("$.data.status", is("ACTIVE")));
    }

    @Test
    void secondOpenSessionClosesOldAndCreatesNew() throws Exception {
        var firstId = openSessionAndReturnId();
        var secondId = openSessionAndReturnId();

        mockMvc.perform(get("/api/v1/sessions/children").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()", is(1)))
            .andExpect(jsonPath("$.data[0].id", is(secondId.intValue())));

        mockMvc.perform(delete("/api/v1/sessions/children/{id}", firstId).header("Authorization", "Bearer " + token))
            .andExpect(status().isNoContent());
    }

    @Test
    void closeSessionReturns204() throws Exception {
        var sessionId = openSessionAndReturnId();

        mockMvc.perform(delete("/api/v1/sessions/children/{id}", sessionId).header("Authorization", "Bearer " + token))
            .andExpect(status().isNoContent());
    }

    @Test
    void expelChildReturns204() throws Exception {
        var sessionId = openSessionAndReturnId();

        mockMvc.perform(delete("/api/v1/sessions/children/{id}/expel", sessionId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isNoContent());
    }

    @Test
    void heartbeatReturns204() throws Exception {
        var sessionId = openSessionAndReturnId();

        mockMvc.perform(post("/api/v1/sessions/children/{id}/heartbeat", sessionId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isNoContent());
    }

    @Test
    void getActiveSessionsReturnsResults() throws Exception {
        openSessionAndReturnId();

        mockMvc.perform(get("/api/v1/sessions/children").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));
    }

    @Test
    void recordGameHeartbeat_activeSession_updatesLastActivityAtAndReturnsActive() throws Exception {
        var sessionId = openSessionAndReturnId();
        var session = childSessionUseCase.getSession(sessionId);
        var before = session.getLastActivityAt();

        Thread.sleep(10);

        var result = childSessionUseCase.recordGameHeartbeat(sessionId);

        assertTrue(result.active());
        assertEquals("ACTIVE", result.status());
        assertTrue(result.lastActivityAt().isAfter(before));
    }

    @Test
    void recordGameHeartbeat_unknownSession_returnsInactive() {
        var result = childSessionUseCase.recordGameHeartbeat(99999L);

        assertFalse(result.active());
    }

    private String openBody(Long childId) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
            "childProfileId", childId,
            "heartbeatIntervalSeconds", 30
        ));
    }

    private Long openSessionAndReturnId() throws Exception {
        var result = mockMvc.perform(post("/api/v1/sessions/children")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(openBody(childProfileId)))
            .andExpect(status().isCreated())
            .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("id").asLong();
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
}
