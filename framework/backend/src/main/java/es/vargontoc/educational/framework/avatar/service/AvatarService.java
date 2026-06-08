package es.vargontoc.educational.framework.avatar.service;

import es.vargontoc.educational.framework.avatar.infrastructure.dto.AvatarEventRequest;
import es.vargontoc.educational.framework.avatar.model.AvatarEventResult;
import es.vargontoc.educational.framework.avatar.ports.in.AvatarUseCase;
import es.vargontoc.educational.framework.avatar.validation.AvatarValidator;
import es.vargontoc.educational.framework.content.model.AvatarEventCatalog;
import es.vargontoc.educational.framework.content.model.AvatarTone;
import es.vargontoc.educational.framework.content.ports.out.AvatarEventCatalogRepository;
import es.vargontoc.educational.framework.family.model.ChildProfile;
import es.vargontoc.educational.framework.family.ports.out.ChildProfileRepository;
import es.vargontoc.educational.framework.session.model.ChildSession;
import es.vargontoc.educational.framework.session.model.ChildSessionStatus;
import es.vargontoc.educational.framework.session.ports.out.ChildSessionRepository;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
import es.vargontoc.educational.framework.shared.exception.SessionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AvatarService implements AvatarUseCase {

    private static final String FALLBACK_TEXT = "Thank you for using the educational framework!";

    private final ChildSessionRepository childSessionRepository;
    private final ChildProfileRepository childProfileRepository;
    private final AvatarEventCatalogRepository avatarEventCatalogRepository;
    private final AvatarValidator avatarValidator;

    public AvatarService(
            ChildSessionRepository childSessionRepository,
            ChildProfileRepository childProfileRepository,
            AvatarEventCatalogRepository avatarEventCatalogRepository) {
        this.childSessionRepository = childSessionRepository;
        this.childProfileRepository = childProfileRepository;
        this.avatarEventCatalogRepository = avatarEventCatalogRepository;
        this.avatarValidator = new AvatarValidator();
    }

    @Override
    public AvatarEventResult processAvatarEvent(AvatarEventRequest request) {
        avatarValidator.validateForProcess(request);

        ChildSession session = findActiveSession(request.childSessionId());
        ChildProfile childProfile = findChildProfile(session.getChildProfileId());

        if (!childProfile.isAgentEnabled()) {
            return createSuppressedResult(request.eventType());
        }

        String text = resolveCatalogText(request.eventType(), request.locale());
        boolean audioAvailable = childProfile.isTtsEnabled();

        return createResult(request.eventType(), text, audioAvailable, false);
    }

    private ChildSession findActiveSession(Long sessionId) {
        ChildSession session = childSessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Child session not found: " + sessionId));

        if (!ChildSessionStatus.ACTIVE.equals(session.getStatus())) {
            throw new SessionException("Child session is not active");
        }

        return session;
    }

    private ChildProfile findChildProfile(Long childProfileId) {
        return childProfileRepository.findById(childProfileId)
            .orElseThrow(() -> new ResourceNotFoundException("Child profile not found: " + childProfileId));
    }

    private String resolveCatalogText(es.vargontoc.educational.framework.content.model.AvatarEventType eventType, String locale) {
        List<AvatarEventCatalog> catalogs = avatarEventCatalogRepository.findActiveByFilters(
            eventType, AvatarTone.NEUTRAL, locale
        );

        if (!catalogs.isEmpty()) {
            return catalogs.get(0).getMessageText();
        }

        return FALLBACK_TEXT;
    }

    private AvatarEventResult createResult(
            es.vargontoc.educational.framework.content.model.AvatarEventType eventType,
            String text,
            boolean audioAvailable,
            boolean suppressed) {
        AvatarEventResult result = new AvatarEventResult();
        result.setEventType(eventType);
        result.setText(text);
        result.setAudioAvailable(audioAvailable);
        result.setAudioMetadata(audioAvailable ? Map.of() : null);
        result.setSuppressed(suppressed);
        return result;
    }

    private AvatarEventResult createSuppressedResult(es.vargontoc.educational.framework.content.model.AvatarEventType eventType) {
        return createResult(eventType, null, false, true);
    }
}