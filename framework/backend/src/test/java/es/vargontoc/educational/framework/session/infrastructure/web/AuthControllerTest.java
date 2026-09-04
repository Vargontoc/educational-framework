package es.vargontoc.educational.framework.session.infrastructure.web;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import es.vargontoc.educational.framework.family.infrastructure.web.AbstractIntegrationTest;
import es.vargontoc.educational.framework.family.ports.in.FamilyUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FamilyUseCase familyUseCase;

    @BeforeEach
    void setUpFamily() {
        if (!familyUseCase.familyExists()) {
            familyUseCase.createFamily("Family One", "1234", true, true);
        }
    }

    @Test
    void login_withCorrectPinReturnsToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody("1234")))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.token").isString())
            .andExpect(jsonPath("$.data.sessionId").isNumber());
    }

    @Test
    void login_withWrongPinReturns401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody("9999")))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_withValidTokenReturns204() throws Exception {
        var token = loginAndReturnToken();

        mockMvc.perform(post("/api/v1/auth/logout").header("Authorization", "Bearer " + token))
            .andExpect(status().isNoContent());
    }

    @Test
    void logout_withInvalidTokenReturns401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout").header("Authorization", "Bearer invalid-token"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void secondLoginReturnsDifferentToken() throws Exception {
        var firstToken = loginAndReturnToken();
        var secondToken = loginAndReturnToken();

        assertNotEquals(firstToken, secondToken);
    }

    @Test
    void getFamilyDoesNotExposeSessionToken() throws Exception {
        mockMvc.perform(get("/api/v1/family"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", not(hasKey("token"))))
            .andExpect(jsonPath("$.data", not(hasKey("tokenHash"))));
    }

    private String loginBody(String pin) throws Exception {
        return objectMapper.writeValueAsString(Map.of("pin", pin));
    }

    private String loginAndReturnToken() throws Exception {
        var result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody("1234")))
            .andExpect(status().isCreated())
            .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("token").asString();
    }
}
