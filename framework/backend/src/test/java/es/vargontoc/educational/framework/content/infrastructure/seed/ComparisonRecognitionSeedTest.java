package es.vargontoc.educational.framework.content.infrastructure.seed;

import es.vargontoc.educational.framework.game.service.RecognitionResourceRefs;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Structural check of the comparison seed (SPRINT-108) and of the topic/activity that expose it. */
class ComparisonRecognitionSeedTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonNode load(String path) throws Exception {
        try (InputStream in = getClass().getResourceAsStream(path)) {
            assertNotNull(in, path + " must be on the classpath");
            return MAPPER.readTree(in);
        }
    }

    @Test
    void tenObjectsWithNubiAudioAndImage() throws Exception {
        JsonNode seed = load("/seeds/21-comparison-elements.json");
        assertEquals(10, seed.size());

        Set<String> codes = new HashSet<>();
        for (JsonNode element : seed) {
            String code = element.get("code").asString();
            codes.add(code);
            String refs = element.get("resourceRefs").asString();

            assertEquals(code, RecognitionResourceRefs.get(refs, "image"), code + ": image key is the code");
            String nubi = RecognitionResourceRefs.nubiAudio(refs);
            assertNotNull(nubi, code);
            assertTrue(nubi.contains("más grande"), code + ": the prompt always asks for the biggest, was " + nubi);
            assertTrue(!element.get("displayValue").asString().isBlank(), code);
            assertTrue(!element.get("similarityGroup").asString().isBlank(), code);
        }
        assertEquals(Set.of("apple", "car", "house", "tree", "flower", "ball", "book", "cup", "hat", "shoe"), codes);
    }

    @Test
    void comparisonTopic_isARecognitionTopicOfTypeComparison() throws Exception {
        JsonNode topic = null;
        for (JsonNode t : load("/seeds/02-topics.json")) {
            if ("Comparación".equals(t.get("name").asString())) {
                topic = t;
            }
        }
        assertNotNull(topic);
        assertEquals("COMPARISON", topic.get("recognitionType").asString());
    }

    @Test
    void comparisonActivity_isARecognitionActivityWithTheThreeDifficultyLevels() throws Exception {
        JsonNode activity = null;
        for (JsonNode a : load("/seeds/04-activities.json")) {
            if ("Comparación de Tamaño".equals(a.get("name").asString())) {
                activity = a;
            }
        }
        assertNotNull(activity);
        assertEquals("RECOGNITION", activity.get("gameEngineType").asString());
        assertEquals("Comparación", activity.get("topicNames").get(0).asString());

        Set<String> levels = new HashSet<>();
        for (JsonNode d : load("/seeds/05-difficulty-levels.json")) {
            if ("Comparación de Tamaño".equals(d.get("activityName").asString())) {
                levels.add(d.get("difficultyCode").asString());
            }
        }
        assertEquals(Set.of("EASY", "MEDIUM", "HARD"), levels);
    }
}
