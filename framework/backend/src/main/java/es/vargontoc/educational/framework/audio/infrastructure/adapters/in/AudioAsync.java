package es.vargontoc.educational.framework.audio.infrastructure.adapters.in;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import es.vargontoc.educational.framework.audio.application.ports.out.AudioPort;
import es.vargontoc.educational.framework.audio.domain.AudioCache;
import es.vargontoc.educational.framework.audio.domain.AudioRequest;
import es.vargontoc.educational.framework.audio.infrastructure.cache.AudioCacheStorage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AudioAsync {
    
    @Async
    public void generateAudio(AudioCache key, AudioRequest request, AudioPort audioPort, AudioCacheStorage storage) {
        log.info("Cache miss, calling audio service: text={}", request.text());

        byte[] audioData = audioPort.synthesizeAudio(request);

        storage.put(key, audioData);
        log.info("Stored audio in cache: key={}", key);

    }
}
