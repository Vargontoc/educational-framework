package es.vargontoc.educational.framework.content.service;

import es.vargontoc.educational.framework.content.model.ContentLocale;
import es.vargontoc.educational.framework.content.model.EntityType;
import es.vargontoc.educational.framework.content.ports.in.ContentLocaleUseCase;
import es.vargontoc.educational.framework.content.ports.out.ContentLocaleRepository;
import es.vargontoc.educational.framework.content.validation.ContentLocaleValidator;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Transactional
public class ContentLocaleService implements ContentLocaleUseCase {

    private final ContentLocaleRepository contentLocaleRepository;
    private final ContentLocaleValidator contentLocaleValidator;

    public ContentLocaleService(ContentLocaleRepository contentLocaleRepository) {
        this.contentLocaleRepository = contentLocaleRepository;
        this.contentLocaleValidator = new ContentLocaleValidator();
    }

    @Override
    public ContentLocale createLocale(EntityType entityType, Long entityId, String localeCode, String name, String description) {
        contentLocaleValidator.validateForCreate(entityType, entityId, localeCode, name);

        var contentLocale = new ContentLocale();
        contentLocale.setEntityType(entityType);
        contentLocale.setEntityId(entityId);
        contentLocale.setLocaleCode(localeCode);
        contentLocale.setName(name);
        contentLocale.setDescription(description);
        contentLocale.setCreatedAt(LocalDateTime.now());

        return contentLocaleRepository.save(contentLocale);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContentLocale> listByEntity(EntityType entityType, Long entityId) {
        return contentLocaleRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }

    @Override
    public ContentLocale updateLocale(Long id, String name, String description) {
        var existing = contentLocaleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("ContentLocale not found with id: " + id));

        existing.setName(name);
        existing.setDescription(description);
        existing.setUpdatedAt(LocalDateTime.now());

        return contentLocaleRepository.save(existing);
    }
}
