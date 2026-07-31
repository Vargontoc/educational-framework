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
            .andExpect(jsonPath("$.data.name", is("Family One")))
            .andExpect(jsonPath("$.data.audioGeneralEnabled", is(true)))
            .andExpect(jsonPath("$.data.audioGeneralVolume", is(100)))
            .andExpect(jsonPath("$.data.npcEnabled", is(true)))
            .andExpect(jsonPath("$.data.npcVoiceEnabled", is(true)))
            .andExpect(jsonPath("$.data.npcVoiceVolume", is(100)))
            .andExpect(jsonPath("$.data.narrativeVoiceEnabled", is(true)))
            .andExpect(jsonPath("$.data.narrativeVoiceVolume", is(100)));
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

    @Test
    void patchFamily_partialUpdate_onlyUpdatesAudioGeneralVolume() throws Exception {
        createFamily();

        var body = "{\"audioGeneralVolume\": 50}";

        mockMvc.perform(patch("/api/v1/family").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.audioGeneralVolume", is(50)))
            .andExpect(jsonPath("$.data.audioGeneralEnabled", is(true)))
            .andExpect(jsonPath("$.data.npcVoiceVolume", is(100)))
            .andExpect(jsonPath("$.data.narrativeVoiceVolume", is(100)));
    }

    @Test
    void patchFamily_volumeClamping_above100_returns100() throws Exception {
        createFamily();

        var body = "{\"audioGeneralVolume\": 150}";

        mockMvc.perform(patch("/api/v1/family").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.audioGeneralVolume", is(100)));
    }

    @Test
    void patchFamily_volumeClamping_below0_returns0() throws Exception {
        createFamily();

        var body = "{\"audioGeneralVolume\": -10}";

        mockMvc.perform(patch("/api/v1/family").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.audioGeneralVolume", is(0)));
    }

    @Test
    void patchFamily_updatesAllGlobalConfigFields() throws Exception {
        createFamily();

        var body = objectMapper.writeValueAsString(Map.of(
            "audioGeneralEnabled", false,
            "audioGeneralVolume", 75,
            "npcEnabled", false,
            "npcVoiceEnabled", false,
            "npcVoiceVolume", 50,
            "narrativeVoiceEnabled", false,
            "narrativeVoiceVolume", 25
        ));

        mockMvc.perform(patch("/api/v1/family").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.audioGeneralEnabled", is(false)))
            .andExpect(jsonPath("$.data.audioGeneralVolume", is(75)))
            .andExpect(jsonPath("$.data.npcEnabled", is(false)))
            .andExpect(jsonPath("$.data.npcVoiceEnabled", is(false)))
            .andExpect(jsonPath("$.data.npcVoiceVolume", is(50)))
            .andExpect(jsonPath("$.data.narrativeVoiceEnabled", is(false)))
            .andExpect(jsonPath("$.data.narrativeVoiceVolume", is(25)));
    }

    @Test
    void patchFamily_legacyTtsDisabled_doesNotAffectAudioGeneralEnabled() throws Exception {
        createFamily();

        var body = objectMapper.writeValueAsString(Map.of(
            "ttsEnabled", false,
            "audioGeneralEnabled", true
        ));

        mockMvc.perform(patch("/api/v1/family").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.ttsEnabled", is(false)))
            .andExpect(jsonPath("$.data.audioGeneralEnabled", is(true)));
    }

    @Test
    void patchFamily_withValidPin_updatesSuccessfully() throws Exception {
        createFamily();

        var body = objectMapper.writeValueAsString(Map.of(
            "pin", "5678",
            "ttsEnabled", true,
            "agentEnabled", true
        ));

        mockMvc.perform(patch("/api/v1/family").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk());
    }

    @Test
    void patchFamily_withInvalidPin_returns400() throws Exception {
        createFamily();

        var body = objectMapper.writeValueAsString(Map.of(
            "pin", "123",
            "ttsEnabled", true,
            "agentEnabled", true
        ));

        mockMvc.perform(patch("/api/v1/family").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest());
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
