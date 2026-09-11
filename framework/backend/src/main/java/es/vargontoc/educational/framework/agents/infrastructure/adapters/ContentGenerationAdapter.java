package es.vargontoc.educational.framework.agents.infrastructure.adapters;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import es.vargontoc.educational.framework.agents.application.ports.in.ContentGenerationUseCase;
import es.vargontoc.educational.framework.agents.domain.request.GenerateGameAvatarRequest;
import es.vargontoc.educational.framework.agents.domain.response.GenerateAvatarEventResponse;
import es.vargontoc.educational.framework.audio.domain.enums.TonePreset;
import es.vargontoc.educational.framework.avatar.domain.enums.AvatarEventType;
import es.vargontoc.educational.framework.content.model.AvatarEventCatalog;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.service.AvatarEventCatalogService;
import jakarta.transaction.Transactional;

@Transactional
@Component
public class ContentGenerationAdapter implements ContentGenerationUseCase {

    @Value("classpath:prompts/request_events.st")
    private Resource userEventPrompt;

    private final ChatClient client;
    private final AvatarEventCatalogService eventService;

    public ContentGenerationAdapter(@Qualifier("content-agent") ChatClient client, AvatarEventCatalogService eventService) {
        this.client = client;
        this.eventService = eventService;
    }



    @Override
    public List<AvatarEventCatalog> generateAvatarEvents(GenerateGameAvatarRequest request) {
        
        List<AvatarEventCatalog> events = new ArrayList<>();

        GenerateAvatarEventResponse res = client.prompt().user(u -> 
            u.text(userEventPrompt)
                .param("type", request.type().name())
                .param("description", getShortDescription(request.type()))
        ).call().entity(GenerateAvatarEventResponse.class);

        res.phrases().forEach(p -> {
            events.add(eventService.createAvatarEvent(request.type(), TonePreset.NEUTRAL, "es-ES", p, ContentStatus.ACTIVE));
        });

        return events;
    }



    private String getShortDescription(AvatarEventType type) {
        return switch(type) {
            case WELCOME -> "Evento de bienvenida al niño al entrar en el juego";
            case FAREWELL -> "Evento de despedida al niño cuando termina la sesión de juego";
            default -> throw new NotImplementedException("Type not implemented yet");
        };
    }
    
    
}
