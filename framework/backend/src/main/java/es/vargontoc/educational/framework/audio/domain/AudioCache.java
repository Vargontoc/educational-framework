package es.vargontoc.educational.framework.audio.domain;

import java.util.Objects;

public class AudioCache {
    
    private final ToneParams tone;
    private final int textHash;
    
    public AudioCache(ToneParams tone, int textHash) {
        this.tone = tone;
        this.textHash = textHash;
    }


    public ToneParams getTone() { return tone; }
    public int getTextHash() { return textHash; }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;

        AudioCache that = (AudioCache)obj;
        return textHash == that.textHash && Objects.equals(tone, that.tone);
    }


    @Override
    public int hashCode() {
        return Objects.hash(tone, textHash);
    }

    @Override
    public String toString() {
        return "AudioCache{tone=(cfg="+ tone.cfgWeight() +
            ",exageration=" + tone.exageration() + 
            ",temperature=" + tone.exageration() + 
            "), textHash=" + textHash +"}";
    }
    
}
