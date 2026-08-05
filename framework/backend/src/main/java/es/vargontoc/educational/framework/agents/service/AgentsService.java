package es.vargontoc.educational.framework.agents.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.DefaultChatClient;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import es.vargontoc.educational.framework.agents.model.AgentStatus;
import es.vargontoc.educational.framework.agents.model.AgentStatusType;
import es.vargontoc.educational.framework.agents.ports.out.ChangeStatusAgentUseCase;
import es.vargontoc.educational.framework.agents.ports.out.CheckStatusModelsUseCase;
import es.vargontoc.educational.framework.agents.ports.out.SendMessageChatbotUseCase;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AgentsService implements CheckStatusModelsUseCase, ChangeStatusAgentUseCase, SendMessageChatbotUseCase {
    
    private static final Logger LOG = LoggerFactory.getLogger(AgentsService.class);

    private final RestClient restClient;

    private final ChatClient chatbot;
    private final ChatClient npc;

    public AgentsService(@Qualifier("chatbot-agent") ChatClient chatbot,
    @Qualifier("npc-agent") ChatClient npc,
    @Value("${spring.ai.ollama.base-url}") String ollamaBaseUrl) {
        this.chatbot = chatbot;
        this.npc = npc;
        this.restClient = RestClient.builder().baseUrl(ollamaBaseUrl).build();
    }

    @Override
    public List<AgentStatus> checkAllAvailableModels(){
        return List.of(checkStatus(chatbot), checkStatus(npc));
    }

    @Override
    public AgentStatus checkStatus(ChatClient client) {
        String model = getModelNameFromClient(client);
        LOG.info("Check model '{}' status", model);
        try {

            if(check("/api/ps", model)){
                LOG.info("Model '{}' is running", model); 
                return new AgentStatus(model, AgentStatusType.RUNNING);
            }

            if(check("/api/tags", model)) {
                LOG.warn("Model '{}' is stopped", model);
                return new AgentStatus(model, AgentStatusType.STOPPED);
            }

            LOG.error("Model '{}' is unreachable", model);
            return new AgentStatus(model, AgentStatusType.UNREACHABLE);

        }catch(Exception e) {
            LOG.error("Modelo '{}' no existe en el servidor Ollama", model, e);
            return new AgentStatus(model, AgentStatusType.UNREACHABLE);
        }
    }

    private boolean check(String uri, String model) {
            Map<String, Object> response = restClient.get().uri(uri).retrieve().body(new ParameterizedTypeReference<Map<String,Object>>() {});
            if (response == null) {
                return false;
            }
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> models = (List<Map<String, Object>>) response.get("models");
            return models != null && models.stream().anyMatch(m -> model.equals(m.get("name")) || model.equals(m.get("model")));

    }

    private String getModelNameFromClient(ChatClient client){
        if (client instanceof DefaultChatClient defaultChatClient
                && defaultChatClient.prompt() instanceof DefaultChatClient.DefaultChatClientRequestSpec requestSpec
                && requestSpec.getChatOptions() instanceof OllamaOptions ollamaOptions) {
            return ollamaOptions.getModel();
        }

        throw new IllegalArgumentException("El ChatClient provisto no pertenece a una implementacion váleda de Ollama");
    }

    @Override
    public void run(ChatClient client, boolean stream, Integer time) 
    {
        // Creamos una petición de Chat vacía
        String modelName = getModelNameFromClient(client);

        Map<String, Object> request = Map.of(
            "model", modelName,
            "prompt", "",
            "stream", stream
        );
        if(time != null && time != 0) {
            request.put("keep_alive", time);
        }
        restClient.post().uri("/api/generate").body(request).retrieve().toBodilessEntity();
    }

    @Override
    public void stop(ChatClient client) {
        String modelName = getModelNameFromClient(client);

        Map<String, Object> request = Map.of(
            "model", modelName,
            "prompt", "",
            "stream", false,
            "keep_alive", 0
        );

        restClient.post().uri("/api/generate").body(request).retrieve().toBodilessEntity();
    }

    @Override
    public String sendMessage(String message) throws ValidationException {
        
        if(message == null || message.trim().isEmpty())
            throw new ValidationException("El mensaje no puede estar vacio");

        // Sanitizamos el texto
        String sanitized = sanitize(message);

        // Arrancamos modelo
        if(checkStatus(chatbot).status() == AgentStatusType.STOPPED)
            run(chatbot, false, null);

        // Lanzamos el prompt y recibimos el contenido
        String response = chatbot.prompt(sanitized).call().content();

        // Detenemos y devolvemos la info
        //stop(chatbot);

        return response;
    }

    
    private String sanitize(String input) {
        return Jsoup.clean(input, Safelist.none());
    }

}
