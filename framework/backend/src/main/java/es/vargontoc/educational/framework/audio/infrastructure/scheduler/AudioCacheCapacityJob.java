package es.vargontoc.educational.framework.audio.infrastructure.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import es.vargontoc.educational.framework.audio.infrastructure.cache.AudioCacheStorage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AudioCacheCapacityJob {

    private final AudioCacheStorage audioCacheStorage;

    public AudioCacheCapacityJob(AudioCacheStorage audioCacheStorage) {
        this.audioCacheStorage = audioCacheStorage;
    }

    @Scheduled(fixedDelayString = "${app.audio.cache.enforce-capacity-interval-seconds:300}000")
    public void enforceCapacity() {
        log.debug("Running periodic audio cache disk capacity enforcement");
        audioCacheStorage.enforceDiskCapacity();
    }
}
