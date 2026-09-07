package es.vargontoc.educational.framework.audio.infrastructure.adapters.out;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import es.vargontoc.educational.framework.audio.application.ports.out.AudioPort;
import es.vargontoc.educational.framework.audio.domain.AudioRequest;
import es.vargontoc.educational.framework.shared.exception.AppException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ChatterboxAdapter implements AudioPort {

    private final RestClient client;
    

    public ChatterboxAdapter(@Qualifier("audio-client") RestClient client) {
        this.client = client;
    }


    @Override
    public boolean isAvailable() {
        try {
            client.get().uri("/ping").retrieve().toBodilessEntity();
            return true;
        }catch(Exception e) {
            log.error("No se pudo establecer conexión con el servicio de audio: {}", e.getMessage(), e);
            return false;
        }
    }


    @Override
    public byte[] synthesizeAudio(AudioRequest request) {
        if(!isAvailable())
            throw new AppException("Servicio audio no disponible", HttpStatus.INTERNAL_SERVER_ERROR);
        
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("input", request.text());
        body.put("voice", "nubi-npc-voice");
        body.put("response_format", "wav");
        body.put("exaggeration", request.exageration());
        body.put("cfg_weight", request.cfg());
        body.put("temperature", request.temperature());

        return client.post().uri("/audio/speech")
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .retrieve()
            .body(byte[].class);
    }
    
}
