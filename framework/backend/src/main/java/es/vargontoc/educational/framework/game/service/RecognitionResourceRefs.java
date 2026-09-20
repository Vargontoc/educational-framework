package es.vargontoc.educational.framework.game.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Reads individual fields from {@code RecognitionElement.resourceRefs}, a JSON object stored as text
 * (for example {@code {"nubi-audio": "...", "color": "#FF0000", "icon": "apple"}}).
 */
public final class RecognitionResourceRefs {

    public static final String NUBI_AUDIO = "nubi-audio";
    public static final String COLOR = "color";
    public static final String ICON = "icon";

    private static final Logger log = LoggerFactory.getLogger(RecognitionResourceRefs.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private RecognitionResourceRefs() {
    }

    /**
     * @return the text value stored under {@code key}, or {@code null} when the JSON is blank, malformed,
     *         or does not contain a non-null value for that key
     */
    public static String get(String resourceRefs, String key) {
        if (resourceRefs == null || resourceRefs.isBlank()) {
            return null;
        }
        try {
            JsonNode value = OBJECT_MAPPER.readTree(resourceRefs).get(key);
            return (value == null || value.isNull()) ? null : value.asString();
        } catch (JacksonException e) {
            log.warn("Failed to parse resourceRefs JSON: {}", e.getMessage());
            return null;
        }
    }

    public static String nubiAudio(String resourceRefs) {
        return get(resourceRefs, NUBI_AUDIO);
    }

    public static String colorHex(String resourceRefs) {
        return get(resourceRefs, COLOR);
    }

    public static String icon(String resourceRefs) {
        return get(resourceRefs, ICON);
    }
}
