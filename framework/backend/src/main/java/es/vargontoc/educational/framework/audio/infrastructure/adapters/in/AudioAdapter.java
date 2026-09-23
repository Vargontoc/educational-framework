package es.vargontoc.educational.framework.audio.infrastructure.adapters.in;

import es.vargontoc.educational.framework.audio.application.ports.in.AudioUseCase;
import es.vargontoc.educational.framework.audio.application.ports.out.AudioPort;
import es.vargontoc.educational.framework.audio.domain.AudioCache;
import es.vargontoc.educational.framework.audio.domain.AudioRequest;
import es.vargontoc.educational.framework.audio.domain.ToneParams;
import es.vargontoc.educational.framework.audio.infrastructure.cache.AudioCacheStorage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AudioAdapter implements AudioUseCase {

    private final AudioPort port;
    private final AudioCacheStorage cache;
    private final AudioAsync audioAsync;
    public AudioAdapter(AudioPort port, AudioAsync audioAsync, AudioCacheStorage cache) {
        this.port = port;
        this.cache = cache;
        this.audioAsync = audioAsync;
    }

    @Override
    public byte[] getAudio(AudioRequest request) {

        AudioCache key = build(request.text(), new ToneParams(request.exageration(), request.cfg(), request.temperature()));
        byte[] cached = cache.get(key);

        if(cached != null) {
            return cached;
        }

        audioAsync.generateAudio(key, request, port, cache);

        return null;
    }

    private AudioCache build(String text, ToneParams toneParams) {
        int textHash = text != null ? text.hashCode() : 0;
        return new AudioCache(toneParams, textHash);
    }

    @Override
    public void cleanAudioByName(String text, ToneParams params) {

        AudioCache key = build(text, params);
        if(cache.get(key) != null) {
            cache.remove(key);
        }
    }

  

    


    
}
