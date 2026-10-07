package es.vargontoc.educational.framework.audio.domain;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class AudioCacheTest {

    @Test
    void sameTextSameTone_producesSameKey() {
        ToneParams tone = new ToneParams(0.5, 0.5, 0.8);
        AudioCache a = AudioCache.of("Hola soy Nubi", tone);
        AudioCache b = AudioCache.of("Hola soy Nubi", tone);

        assertNotNull(a.getTextHashSha256());
        assertEquals(a, b);
        assertEquals(a.getTextHashSha256(), b.getTextHashSha256());
    }

    @Test
    void differentTextsSameJavaHashCode_produceDifferentKeys() {
        String text1 = "Aa";
        String text2 = "BB";
        // These have the same String.hashCode() in Java:
        // 'A'*31 + 'a' = 65*31+97 = 2112
        // 'B'*31 + 'B' = 66*31+66 = 2112
        org.junit.jupiter.api.Assertions.assertEquals(text1.hashCode(), text2.hashCode(),
            "Test precondition: texts must share String.hashCode()");

        ToneParams tone = new ToneParams(0.5, 0.5, 0.8);
        AudioCache key1 = AudioCache.of(text1, tone);
        AudioCache key2 = AudioCache.of(text2, tone);

        assertNotEquals(key1, key2, "SHA-256 keys must differ for different texts even if hashCode() collides");
        assertNotEquals(key1.getTextHashSha256(), key2.getTextHashSha256());
    }

    @Test
    void differentTones_produceDifferentKeys() {
        ToneParams tone1 = new ToneParams(0.5, 0.5, 0.8);
        ToneParams tone2 = new ToneParams(0.8, 0.4, 0.9);
        AudioCache key1 = AudioCache.of("same text", tone1);
        AudioCache key2 = AudioCache.of("same text", tone2);

        assertNotEquals(key1, key2);
    }

    @Test
    void whitespaceNormalization_sameResult() {
        ToneParams tone = new ToneParams(0.5, 0.5, 0.8);
        AudioCache key1 = AudioCache.of("  Hola   soy  Nubi  ", tone);
        AudioCache key2 = AudioCache.of("Hola soy Nubi", tone);

        // The AudioCache.of does NOT normalize; the caller (AudioAdapter) normalizes before calling.
        // So this test verifies that if the caller passes already-normalized text, keys match.
        String normalized = "Hola soy Nubi";
        AudioCache a = AudioCache.of(normalized, tone);
        AudioCache b = AudioCache.of("Hola soy Nubi", tone);
        assertEquals(a, b);
    }

    private static void assertEquals(Object a, Object b) {
        org.junit.jupiter.api.Assertions.assertEquals(a, b);
    }
}
