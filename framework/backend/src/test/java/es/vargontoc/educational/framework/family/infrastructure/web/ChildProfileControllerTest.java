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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChildProfileControllerTest extends AbstractIntegrationTest {

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
    void createChild_returns201() throws Exception {
        var body = objectMapper.writeValueAsString(Map.of(
            "name", "Kid One",
            "birthday", LocalDate.now().minusYears(8),
            "avatar", "avatar-1",
            "npcVoiceEnabled", true,
            "npcEnabled", true,
            "npcVoiceVolume", 80,
            "colorVisionMode", "NONE"
        ));

        mockMvc.perform(post("/api/v1/family/children").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.name", is("Kid One")))
            .andExpect(jsonPath("$.data.colorVisionMode", is("NONE")))
            .andExpect(jsonPath("$.data.npcVoiceVolume", is(80)));
    }

    @Test
    void createChild_appliesNpcVoiceCeiling() throws Exception {
        familyUseCase.updateFamily(new es.vargontoc.educational.framework.family.infrastructure.dto.UpdateFamilyRequest("Family One", null, false, true, null, null, null, null, null, null, null));
        var body = objectMapper.writeValueAsString(Map.of(
            "name", "Kid One",
            "birthday", LocalDate.now().minusYears(8),
            "avatar", "avatar-1",
            "npcVoiceEnabled", true,
            "npcEnabled", true,
            "npcVoiceVolume", 80,
            "colorVisionMode", "NONE"
        ));

        mockMvc.perform(post("/api/v1/family/children").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.npcVoiceEnabled", is(false)));
    }

    @Test
    void createChild_withNonDefaultColorVisionMode() throws Exception {
        var body = objectMapper.writeValueAsString(Map.of(
            "name", "Kid Color Vision",
            "birthday", LocalDate.now().minusYears(6),
            "avatar", "avatar-cv",
            "npcVoiceEnabled", true,
            "npcEnabled", true,
            "npcVoiceVolume", 100,
            "colorVisionMode", "DEUTERANOMALY"
        ));

        mockMvc.perform(post("/api/v1/family/children").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.name", is("Kid Color Vision")))
            .andExpect(jsonPath("$.data.colorVisionMode", is("DEUTERANOMALY")));
    }

    @Test
    void getAll_and_getById_and_patch_and_deactivate() throws Exception {
        Long id = createChildAndReturnId();

        mockMvc.perform(get("/api/v1/family/children"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));

        mockMvc.perform(get("/api/v1/family/children/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id", is(id.intValue())));

        mockMvc.perform(get("/api/v1/family/children/{id}", 99999))
            .andExpect(status().isNotFound());

        var patchBody = objectMapper.writeValueAsString(Map.of(
            "name", "Kid Updated",
            "birthday", LocalDate.now().minusYears(9),
            "avatar", "avatar-2",
            "npcVoiceEnabled", true,
            "npcEnabled", true,
            "npcVoiceVolume", 90
        ));
        mockMvc.perform(put("/api/v1/family/children/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(patchBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name", is("Kid Updated")));

        mockMvc.perform(delete("/api/v1/family/children/{id}", id))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/family/children/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.active", is(false)));
    }

    @Test
    void updateChild_colorVisionMode() throws Exception {
        Long id = createChildWithColorVisionAndReturnId("NONE");

        mockMvc.perform(get("/api/v1/family/children/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.colorVisionMode", is("NONE")));

        var patchBody = objectMapper.writeValueAsString(Map.of(
            "name", "Kid CV",
            "birthday", LocalDate.now().minusYears(7),
            "colorVisionMode", "TRITANOPIA"
        ));
        mockMvc.perform(put("/api/v1/family/children/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(patchBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.colorVisionMode", is("TRITANOPIA")));
    }

    private Long createChildWithColorVisionAndReturnId(String colorVisionMode) throws Exception {
        var body = objectMapper.writeValueAsString(Map.of(
            "name", "Kid CV",
            "birthday", LocalDate.now().minusYears(7),
            "avatar", "avatar-cv",
            "npcVoiceEnabled", true,
            "npcEnabled", true,
            "npcVoiceVolume", 100,
            "colorVisionMode", colorVisionMode
        ));

        MvcResult result = mockMvc.perform(post("/api/v1/family/children")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("id").asLong();
    }

    private Long createChildAndReturnId() throws Exception {
        var body = objectMapper.writeValueAsString(Map.of(
            "name", "Kid One",
            "birthday", LocalDate.now().minusYears(8),
            "avatar", "avatar-1",
            "npcVoiceEnabled", true,
            "npcEnabled", true,
            "npcVoiceVolume", 100,
            "colorVisionMode", "NONE"
        ));

        MvcResult result = mockMvc.perform(post("/api/v1/family/children")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("id").asLong();
    }
}
