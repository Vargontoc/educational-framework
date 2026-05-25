package es.vargontoc.educational.framework.content.service;

import es.vargontoc.educational.framework.content.model.AvatarEventCatalog;
import es.vargontoc.educational.framework.content.model.AvatarEventType;
import es.vargontoc.educational.framework.content.model.AvatarTone;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.ports.in.AvatarEventCatalogUseCase;
import es.vargontoc.educational.framework.content.ports.out.AvatarEventCatalogRepository;
import es.vargontoc.educational.framework.content.validation.AvatarEventCatalogValidator;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Transactional
public class AvatarEventCatalogService implements AvatarEventCatalogUseCase {

    private final AvatarEventCatalogRepository avatarEventCatalogRepository;
    private final AvatarEventCatalogValidator avatarEventCatalogValidator;

    public AvatarEventCatalogService(AvatarEventCatalogRepository avatarEventCatalogRepository) {
        this.avatarEventCatalogRepository = avatarEventCatalogRepository;
        this.avatarEventCatalogValidator = new AvatarEventCatalogValidator();
    }

    @Override
    public AvatarEventCatalog createAvatarEvent(AvatarEventType eventType, AvatarTone tone, String locale, String messageText, ContentStatus status) {
        avatarEventCatalogValidator.validateForCreate(eventType, tone, messageText, locale, status);

        var event = new AvatarEventCatalog();
        event.setEventType(eventType);
        event.setTone(tone);
        event.setLocale(locale);
        event.setMessageText(messageText);
        event.setStatus(status);
        event.setCreatedAt(LocalDateTime.now());

        return avatarEventCatalogRepository.save(event);
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
        return avatarEventCatalogRepository.findByEventType(eventType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvatarEventCatalog> listActiveAvatarEventsByFilters(AvatarEventType eventType, AvatarTone tone, String locale) {
        return avatarEventCatalogRepository.findActiveByFilters(eventType, tone, locale);
    }

    @Override
    public AvatarEventCatalog updateAvatarEvent(Long id, AvatarEventType eventType, AvatarTone tone, String locale, String messageText, ContentStatus status) {
        avatarEventCatalogValidator.validateForUpdate(eventType, tone, messageText, locale, status);

        var existing = avatarEventCatalogRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Avatar event catalog not found with id: " + id));

        existing.setEventType(eventType);
        existing.setTone(tone);
        existing.setLocale(locale);
        existing.setMessageText(messageText);
        existing.setStatus(status);
        existing.setUpdatedAt(LocalDateTime.now());

        return avatarEventCatalogRepository.save(existing);
    }
}
