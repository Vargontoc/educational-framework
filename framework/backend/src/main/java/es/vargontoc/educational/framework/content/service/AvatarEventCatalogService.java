package es.vargontoc.educational.framework.content.service;

import es.vargontoc.educational.framework.audio.domain.enums.TonePreset;
import es.vargontoc.educational.framework.avatar.domain.enums.AvatarEventType;
import es.vargontoc.educational.framework.content.model.AvatarEventCatalog;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.ports.in.AvatarEventCatalogUseCase;
import es.vargontoc.educational.framework.content.ports.out.AvatarEventCatalogRepository;
import es.vargontoc.educational.framework.content.validation.AvatarEventCatalogValidator;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
import es.vargontoc.educational.framework.shared.infrastructure.GameCacheStorage;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Transactional
public class AvatarEventCatalogService implements AvatarEventCatalogUseCase {

    private final AvatarEventCatalogRepository avatarEventCatalogRepository;
    private final AvatarEventCatalogValidator avatarEventCatalogValidator;
    private final GameCacheStorage gameCacheStorage;

    public AvatarEventCatalogService(AvatarEventCatalogRepository avatarEventCatalogRepository,
                                     GameCacheStorage gameCacheStorage) {
        this.avatarEventCatalogRepository = avatarEventCatalogRepository;
        this.avatarEventCatalogValidator = new AvatarEventCatalogValidator();
        this.gameCacheStorage = gameCacheStorage;
    }

    @Override
    public AvatarEventCatalog createAvatarEvent(AvatarEventType eventType, TonePreset tone, String locale, String messageText, ContentStatus status, String biome) {
        avatarEventCatalogValidator.validateForCreate(eventType, tone, messageText, locale, status);

        var event = new AvatarEventCatalog();
        event.setEventType(eventType);
        event.setTone(tone);
        event.setLocale(locale);
        event.setMessageText(messageText);
        event.setStatus(status);
        event.setBiome(biome);
        event.setCreatedAt(LocalDateTime.now());

        var saved = avatarEventCatalogRepository.save(event);
        gameCacheStorage.invalidateAllAvatarEvents();
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public AvatarEventCatalog getAvatarEvent(Long id) {
        return avatarEventCatalogRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Avatar event catalog not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvatarEventCatalog> listAvatarEvents() {
        return avatarEventCatalogRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvatarEventCatalog> listAvatarEventsByEventType(AvatarEventType eventType) {
        String cacheKey = "avatarEvents:type=" + eventType.name();
        Optional<List<AvatarEventCatalog>> cached = gameCacheStorage.getAvatarEvent(cacheKey);
        if (cached.isPresent()) {
            return cached.get();
        }
        List<AvatarEventCatalog> result = avatarEventCatalogRepository.findByEventType(eventType);
        gameCacheStorage.putAvatarEvent(cacheKey, result);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvatarEventCatalog> listActiveAvatarEventsByFilters(AvatarEventType eventType, TonePreset tone, String locale) {
        String cacheKey = "avatarEvents:active:type=" + eventType + ":tone=" + tone + ":locale=" + locale;
        Optional<List<AvatarEventCatalog>> cached = gameCacheStorage.getAvatarEvent(cacheKey);
        if (cached.isPresent()) {
            return cached.get();
        }
        List<AvatarEventCatalog> result = avatarEventCatalogRepository.findActiveByFilters(eventType, tone, locale);
        gameCacheStorage.putAvatarEvent(cacheKey, result);
        return result;
    }

    @Override
    public AvatarEventCatalog updateAvatarEvent(Long id, AvatarEventType eventType, TonePreset tone, String locale, String messageText, ContentStatus status, String biome) {
        avatarEventCatalogValidator.validateForUpdate(eventType, tone, messageText, locale, status);

        var existing = avatarEventCatalogRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Avatar event catalog not found with id: " + id));

        existing.setEventType(eventType);
        existing.setTone(tone);
        existing.setLocale(locale);
        existing.setMessageText(messageText);
        existing.setStatus(status);
        existing.setBiome(biome);
        existing.setUpdatedAt(LocalDateTime.now());

        var saved = avatarEventCatalogRepository.save(existing);
        gameCacheStorage.invalidateAllAvatarEvents();
        return saved;
    }
}
