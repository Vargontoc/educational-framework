package es.vargontoc.educational.framework.agents.infrastructure.adapters;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import es.vargontoc.educational.framework.agents.ports.out.OllamaManagementPort;

@Component
public class OllamaManagementAdapter implements OllamaManagementPort {

    private final RestClient restTemplate;

    public OllamaManagementAdapter(@Value("${spring.ai.ollama.base-url}") String url)
    {
        this.restTemplate = RestClient.builder().baseUrl(url).build();
    }

    @Override
    public boolean isRunning(String model) {

        return sendRequest("/api/ps", model);
    }

    @Override
    public boolean isPulled(String model) {

        return sendRequest("/api/tags", model);
    }

    @Override
    public void run(String model, boolean stream, Integer keepAlive) {
        
        Map<String, Object> request = Map.of(
            "model", model,
            "prompt", "",
            "stream", stream,
            "keep_alive", keepAlive != null && keepAlive > 0 ? keepAlive: "5m"
        );
        restTemplate.post().uri("/api/generate").body(request).retrieve().toBodilessEntity();
    }

    @Override
    public void stop(String model) {
        Map<String, Object> request = Map.of(
            "model", model,
            "prompt", "",
            "stream", false,
            "keep_alive", 0
        );

        restTemplate.post().uri("/api/generate").body(request).retrieve().toBodilessEntity();
    }

    @SuppressWarnings("unchecked")
    private boolean sendRequest(String uri, String model){
        Map<String, Object> response = restTemplate.get().uri(uri).retrieve().body(new ParameterizedTypeReference<Map<String,Object>>() {});
        if (response == null) {
            return false;
        }

        List<Map<String, Object>> models = (List<Map<String, Object>>) response.get("models");
        return models != null && models.stream().anyMatch(m -> model.equals(m.get("name")) || model.equals(m.get("model")));
    }
}
