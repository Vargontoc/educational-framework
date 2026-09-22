package es.vargontoc.educational.framework.content.infrastructure.seed;

import es.vargontoc.educational.framework.game.service.RecognitionResourceRefs;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Structural check of the memory seed (SPRINT-109) and of the topic/activity that expose it. */
class MemoryRecognitionSeedTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonNode load(String path) throws Exception {
        try (InputStream in = getClass().getResourceAsStream(path)) {
            assertNotNull(in, path + " must be on the classpath");
            return MAPPER.readTree(in);
        }
    }

    @Test
    void tenObjectsWithImageAndGroup_andEnoughOfEachGroupForTheHardBoard() throws Exception {
        JsonNode seed = load("/seeds/22-memory-elements.json");
        assertEquals(10, seed.size());

        Set<String> codes = new HashSet<>();
        Map<String, Integer> perGroup = new HashMap<>();
        for (JsonNode element : seed) {
            String code = element.get("code").asString();
            assertTrue(codes.add(code), "duplicated code " + code);
            assertEquals(code, RecognitionResourceRefs.get(element.get("resourceRefs").asString(), "image"));
            assertEquals("Encuentra las parejas", RecognitionResourceRefs.nubiAudio(element.get("resourceRefs").asString()), code);
            assertTrue(!element.get("displayValue").asString().isBlank(), code);
            perGroup.merge(element.get("similarityGroup").asString(), 1, Integer::sum);
        }
        assertTrue(perGroup.size() >= 2, "EASY mixes groups: at least two are needed");
        assertTrue(perGroup.values().stream().allMatch(n -> n >= 4), "HARD needs 4 elements of a single group: " + perGroup);
    }

    @Test
    void memoryTopic_isOfTypeMemory() throws Exception {
        JsonNode topic = null;
        for (JsonNode t : load("/seeds/02-topics.json")) {
            if ("Memoria".equals(t.get("name").asString())) {
                topic = t;
            }
        }
        assertNotNull(topic);
        assertEquals("MEMORY", topic.get("recognitionType").asString());
    }

    @Test
    void memoryActivity_usesTheMemoryEngineWithTheThreeDifficultyLevels() throws Exception {
        JsonNode activity = null;
        for (JsonNode a : load("/seeds/04-activities.json")) {
            if ("Memoria de Parejas".equals(a.get("name").asString())) {
                activity = a;
            }
        }
        assertNotNull(activity);
        assertEquals("MEMORY", activity.get("gameEngineType").asString());
        assertEquals("Memoria", activity.get("topicNames").get(0).asString());

        Set<String> levels = new HashSet<>();
        for (JsonNode d : load("/seeds/05-difficulty-levels.json")) {
            if ("Memoria de Parejas".equals(d.get("activityName").asString())) {
                levels.add(d.get("difficultyCode").asString());
            }
        }
        assertEquals(Set.of("EASY", "MEDIUM", "HARD"), levels);
    }
}
