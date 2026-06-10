package es.vargontoc.educational.framework.avatar.infrastructure.cache;

import es.vargontoc.educational.framework.avatar.infrastructure.tts.TtsException;
import es.vargontoc.educational.framework.avatar.infrastructure.tts.TtsToneMapper;
import es.vargontoc.educational.framework.avatar.ports.out.TtsClient;
import es.vargontoc.educational.framework.content.model.AvatarTone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CachingTtsClient implements TtsClient {

    private static final Logger log = LoggerFactory.getLogger(CachingTtsClient.class);

    private final TtsClient delegate;
    private final AvatarAudioCache cache;
    private final TtsToneMapper toneMapper;
    private final AvatarAudioCacheProperties properties;

    public CachingTtsClient(
            TtsClient delegate,
            AvatarAudioCache cache,
            TtsToneMapper toneMapper,
            AvatarAudioCacheProperties properties) {
        this.delegate = delegate;
        this.cache = cache;
        this.toneMapper = toneMapper;
        this.properties = properties;
    }

    @Override
    public byte[] synthesize(String text, String locale, AvatarTone tone, String voiceProfile) {
        AvatarCacheKey key = buildKey(text, locale, tone, voiceProfile);

        byte[] cached = cache.get(key);
        if (cached != null) {
            log.debug("Cache hit for avatar audio: {} bytes, key={}", cached.length, key);
            return cached;
        }

        log.debug("Cache miss for avatar audio, calling TTS: text={}, locale={}, tone={}, voiceProfile={}",
            text, locale, tone, voiceProfile);

        byte[] audioData = delegate.synthesize(text, locale, tone, voiceProfile);

        cache.put(key, audioData);
        log.debug("Stored avatar audio in cache: {} bytes, key={}", audioData.length, key);

        return audioData;
    }

    private AvatarCacheKey buildKey(String text, String locale, AvatarTone tone, String voiceProfile) {
        String mappedTone = toneMapper.map(tone);
        int textHash = text != null ? text.hashCode() : 0;
        return new AvatarCacheKey(
            locale,
            mappedTone,
            textHash,
            voiceProfile,
            properties.getAudioFormatVersion()
        );
    }
}