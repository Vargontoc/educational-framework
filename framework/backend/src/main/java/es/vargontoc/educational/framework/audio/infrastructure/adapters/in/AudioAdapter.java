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
    
    public AudioAdapter(AudioPort port, AudioCacheStorage cache) {
        this.port = port;
        this.cache = cache;
    }

    @Override
    public byte[] getAudio(AudioRequest request) {

        AudioCache key = build(request.text(), new ToneParams(request.exageration(), request.cfg(), request.temperature()));
        byte[] cached = cache.get(key);

        if(cached != null) {
            return cached;
        }

        log.info("Cache miss, calling audio service: text={}", request.text());
        byte[] audioData = port.synthesizeAudio(request);

        cache.put(key, audioData);
        log.info("Stored audio in cache: key={}", key);

        return audioData;
    }

    private AudioCache build(String text, ToneParams toneParams) {
        int textHash = text != null ? text.hashCode() : 0;
        return new AudioCache(toneParams, textHash);
    }

    


    
}
