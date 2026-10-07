package es.vargontoc.educational.framework.audio.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

public class AudioCache {
    
    private static final String VOICE = "nubi-npc-voice";

    private final ToneParams tone;
    private final String textHashSha256;
    
    public AudioCache(ToneParams tone, String textHashSha256) {
        this.tone = tone;
        this.textHashSha256 = textHashSha256;
    }

    public static AudioCache of(String normalizedText, ToneParams tone) {
        String hash = sha256(normalizedText, tone, VOICE);
        return new AudioCache(tone, hash);
    }

    private static String sha256(String text, ToneParams tone, String voice) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String canonical = text + "|" + tone.exageration() + "|" + tone.cfgWeight() + "|" + tone.temperature() + "|" + voice;
            byte[] digest = md.digest(canonical.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public ToneParams getTone() { return tone; }
    public String getTextHashSha256() { return textHashSha256; }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;

        AudioCache that = (AudioCache)obj;
        return Objects.equals(textHashSha256, that.textHashSha256) && Objects.equals(tone, that.tone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tone, textHashSha256);
    }

    @Override
    public String toString() {
        return "AudioCache{tone=(cfg="+ tone.cfgWeight() +
            ",exageration=" + tone.exageration() + 
            ",temperature=" + tone.temperature() + 
            "), sha256=" + textHashSha256 +"}";
    }
    
}
