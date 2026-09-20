package es.vargontoc.educational.framework.game.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RecognitionResourceRefsTest {

    private static final String COLOR_REFS =
            "{\"nubi-audio\": \"¿Dónde está el color rojo?\", \"color\": \"#FF0000\", \"icon\": \"apple\"}";

    @Test
    void extractsEachKnownField() {
        assertEquals("¿Dónde está el color rojo?", RecognitionResourceRefs.nubiAudio(COLOR_REFS));
        assertEquals("#FF0000", RecognitionResourceRefs.colorHex(COLOR_REFS));
        assertEquals("apple", RecognitionResourceRefs.icon(COLOR_REFS));
    }

    @Test
    void missingKey_returnsNull() {
        assertNull(RecognitionResourceRefs.icon("{\"color\": \"#FF0000\"}"));
    }

    @Test
    void nullJsonValue_returnsNull() {
        assertNull(RecognitionResourceRefs.colorHex("{\"color\": null}"));
    }

    @Test
    void blankOrNullInput_returnsNull() {
        assertNull(RecognitionResourceRefs.colorHex(null));
        assertNull(RecognitionResourceRefs.colorHex("   "));
    }

    @Test
    void malformedJson_returnsNullInsteadOfThrowing() {
        assertNull(RecognitionResourceRefs.colorHex("{not json"));
    }

    @Test
    void nonStringValue_isReadAsText() {
        assertEquals("3", RecognitionResourceRefs.get("{\"n\": 3}", "n"));
    }
}
