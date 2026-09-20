package es.vargontoc.educational.framework.content.infrastructure.seed;

import es.vargontoc.educational.framework.game.service.RecognitionResourceRefs;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Structural check of the animals seed: every element carries the nubi-audio text (normalised) that
 * RoundAudioService reads, and the biome/group metadata used for the adaptive difficulty.
 */
class AnimalRecognitionSeedTest {

    private static final String SEED = "/seeds/20-recognition-elements-animals.json";

    private JsonNode loadSeed() throws Exception {
        try (InputStream in = getClass().getResourceAsStream(SEED)) {
            assertNotNull(in, SEED + " must be on the classpath");
            return new ObjectMapper().readTree(in);
        }
    }

    @Test
    void everyAnimalHasNubiAudioResourceRefsMatchingItsNubiText() throws Exception {
        JsonNode seed = loadSeed();
        assertEquals(22, seed.size());

        for (JsonNode element : seed) {
            String code = element.get("code").asString();
            String nubiAudio = RecognitionResourceRefs.nubiAudio(element.get("resourceRefs").asString());

            assertNotNull(nubiAudio, code + ": nubi-audio");
            assertTrue(!nubiAudio.isBlank() && !nubiAudio.contains("  "), code + ": nubi-audio must be normalised text");
            assertEquals(element.get("nubi").asString().replaceAll("\\s+", " ").trim(), nubiAudio, code);
        }
    }

    @Test
    void everyAnimalHasAtLeastOneBiome_andGroupsAreOnlyTheKnownOnes() throws Exception {
        Set<String> knownGroups = Set.of("horns", "equine", "peck", "wool");
        for (JsonNode element : loadSeed()) {
            String code = element.get("code").asString();
            assertTrue(element.get("biome").size() > 0, code + ": biome");
            for (JsonNode group : element.get("group")) {
                assertTrue(knownGroups.contains(group.asString()), code + ": " + group);
            }
        }
    }
}
