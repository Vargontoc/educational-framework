package es.vargontoc.educational.framework.family.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.vargontoc.educational.framework.family.infrastructure.persistence.FamilyJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FamilyControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FamilyJpaRepository familyJpaRepository;

    @Test
    void createFamily_thenConflictOnSecondCreate() throws Exception {
        var body = objectMapper.writeValueAsString(Map.of(
            "name", "Family One",
            "pin", "1234",
            "ttsEnabled", true,
            "agentEnabled", true
        ));

        mockMvc.perform(post("/api/v1/family").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.name", is("Family One")))
            .andExpect(jsonPath("$.data", not(hasKey("pin"))))
            .andExpect(jsonPath("$.data", not(hasKey("pinHash"))));

        mockMvc.perform(post("/api/v1/family").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isConflict());
    }

    @Test
    void getFamily_whenMissing_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/family"))
            .andExpect(status().isNotFound());
    }

    @Test
    void getFamily_whenExists_returns200() throws Exception {
        createFamily();

        mockMvc.perform(get("/api/v1/family"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name", is("Family One")));
    }

    @Test
    void patchFamily_updatesName() throws Exception {
        createFamily();

        var body = objectMapper.writeValueAsString(Map.of(
            "name", "Renamed",
            "ttsEnabled", true,
            "agentEnabled", true
        ));

        mockMvc.perform(patch("/api/v1/family").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name", is("Renamed")));
    }

    @Test
    void patchFamily_withNullPin_keepsPinHash() throws Exception {
        createFamily();
        var before = familyJpaRepository.findAll().getFirst().getPinHash();

        var body = objectMapper.writeValueAsString(Map.of(
            "name", "Family One Updated",
            "ttsEnabled", true,
            "agentEnabled", true
        ));

        mockMvc.perform(patch("/api/v1/family").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk());

        var after = familyJpaRepository.findAll().getFirst().getPinHash();
        assertEquals(before, after);
    }

    private void createFamily() throws Exception {
        var body = objectMapper.writeValueAsString(Map.of(
            "name", "Family One",
            "pin", "1234",
            "ttsEnabled", true,
            "agentEnabled", true
        ));
        mockMvc.perform(post("/api/v1/family").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated());
    }
}
