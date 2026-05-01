package es.vargontoc.educational.framework.family.infrastructure.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.vargontoc.educational.framework.family.ports.in.FamilyUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdultProfileControllerTest extends AbstractIntegrationTest {

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
    void fullCrudFlow() throws Exception {
        Long id = createAdultAndReturnId();

        mockMvc.perform(get("/api/v1/family/adults"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));

        mockMvc.perform(get("/api/v1/family/adults/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id", is(id.intValue())));

        mockMvc.perform(get("/api/v1/family/adults/{id}", 99999))
            .andExpect(status().isNotFound());

        var patchBody = objectMapper.writeValueAsString(Map.of(
            "name", "Parent Updated",
            "birthday", LocalDate.now().minusYears(36),
            "avatar", "adult-2"
        ));
        mockMvc.perform(patch("/api/v1/family/adults/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(patchBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name", is("Parent Updated")));

        mockMvc.perform(delete("/api/v1/family/adults/{id}", id))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/family/adults/{id}", id))
            .andExpect(status().isNotFound());
    }

    private Long createAdultAndReturnId() throws Exception {
        var body = objectMapper.writeValueAsString(Map.of(
            "name", "Parent One",
            "birthday", LocalDate.now().minusYears(35),
            "avatar", "adult-1"
        ));

        MvcResult result = mockMvc.perform(post("/api/v1/family/adults")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("id").asLong();
    }
}
