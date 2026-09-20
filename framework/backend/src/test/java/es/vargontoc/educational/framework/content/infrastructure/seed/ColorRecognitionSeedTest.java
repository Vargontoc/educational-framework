package es.vargontoc.educational.framework.content.infrastructure.seed;

import es.vargontoc.educational.framework.game.service.RecognitionResourceRefs;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Structural check of the colour recognition seed: every element must carry the resourceRefs that the
 * COLOR minigame relies on (nubi-audio for Nubi, color for similarity validation, icon for EASY/MEDIUM).
 */
class ColorRecognitionSeedTest {

    private static final String SEED = "/seeds/19-recognition-elements-colors.json";
    private static final Pattern HEX_COLOR = Pattern.compile("^#[0-9A-Fa-f]{6}$");

    private JsonNode loadSeed() throws Exception {
        try (InputStream in = getClass().getResourceAsStream(SEED)) {
            assertNotNull(in, SEED + " must be on the classpath");
            return new ObjectMapper().readTree(in);
        }
    }

    @Test
    void everyColorElementHasParseableResourceRefsWithNubiAudioColorAndIcon() throws Exception {
        JsonNode seed = loadSeed();
        assertTrue(seed.size() > 0);

        for (JsonNode element : seed) {
            String code = element.get("code").asString();
            String resourceRefs = element.get("resourceRefs").asString();

            String nubiAudio = RecognitionResourceRefs.nubiAudio(resourceRefs);
            String color = RecognitionResourceRefs.colorHex(resourceRefs);
            String icon = RecognitionResourceRefs.icon(resourceRefs);

            assertNotNull(nubiAudio, code + ": nubi-audio");
            assertTrue(!nubiAudio.isBlank() && !nubiAudio.contains("  "), code + ": nubi-audio must be normalised text");
            assertNotNull(color, code + ": color");
            assertTrue(HEX_COLOR.matcher(color).matches(), code + ": color must be #RRGGBB, was " + color);
            assertNotNull(icon, code + ": icon");
            assertTrue(!icon.isBlank(), code + ": icon");
        }
    }

    @Test
    void resourceRefsColorMatchesTheTopLevelColorField() throws Exception {
        for (JsonNode element : loadSeed()) {
            assertEquals(
                    element.get("color").asString().toUpperCase(),
                    RecognitionResourceRefs.colorHex(element.get("resourceRefs").asString()).toUpperCase(),
                    element.get("code").asString());
        }
    }

    @Test
    void codesAndIconsAreUnique() throws Exception {
        Set<String> codes = new HashSet<>();
        Set<String> icons = new HashSet<>();
        for (JsonNode element : loadSeed()) {
            assertTrue(codes.add(element.get("code").asString()), "duplicate code");
            assertTrue(icons.add(RecognitionResourceRefs.icon(element.get("resourceRefs").asString())), "duplicate icon");
        }
    }
}
