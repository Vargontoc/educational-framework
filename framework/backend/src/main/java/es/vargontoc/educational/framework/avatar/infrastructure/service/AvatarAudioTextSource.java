package es.vargontoc.educational.framework.avatar.infrastructure.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import es.vargontoc.educational.framework.audio.application.ports.in.AudioTextSource;
import es.vargontoc.educational.framework.audio.domain.AudioRequest;
import es.vargontoc.educational.framework.content.model.AvatarEventCatalog;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.ports.out.AvatarEventCatalogRepository;
import es.vargontoc.educational.framework.family.model.ChildProfile;
import es.vargontoc.educational.framework.family.ports.out.ChildProfileRepository;

/**
 * Frases de Nubi del catalogo de eventos de avatar (bienvenida, despedida, transiciones...).
 * Las que llevan {@code <name>} se expanden con el nombre de cada perfil existente, igual que hace AvatarService
 * al enviarlas; los perfiles nuevos o renombrados se cubren en ejecucion (AvatarService.generateEventWithName).
 */
@Component
public class AvatarAudioTextSource implements AudioTextSource {

    private static final String NAME_PLACEHOLDER = "<name>";

    private final AvatarEventCatalogRepository catalog;
    private final ChildProfileRepository profiles;

    public AvatarAudioTextSource(AvatarEventCatalogRepository catalog, ChildProfileRepository profiles) {
        this.catalog = catalog;
        this.profiles = profiles;
    }

    @Override
    public String name() {
        return "avatar-events";
    }

    @Override
    public List<AudioRequest> requests() {
        Set<String> names = new LinkedHashSet<>();
        for (ChildProfile profile : profiles.findAll()) {
            if (profile.getName() != null && !profile.getName().isBlank()) {
                names.add(profile.getName());
            }
        }

        List<AudioRequest> requests = new ArrayList<>();
        for (AvatarEventCatalog event : catalog.findAll()) {
            String text = event.getMessageText();
            if (event.getStatus() != ContentStatus.ACTIVE || text == null || text.isBlank() || event.getTone() == null) {
                continue;
            }
            if (text.contains(NAME_PLACEHOLDER)) {
                for (String name : names) {
                    requests.add(AudioRequest.withPreset(text.replace(NAME_PLACEHOLDER, name), event.getTone()));
                }
            } else {
                requests.add(AudioRequest.withPreset(text, event.getTone()));
            }
        }
        return requests;
    }
}
