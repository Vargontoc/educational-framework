package es.vargontoc.educational.framework.agents.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import es.vargontoc.educational.framework.agents.model.AgentStatus;
import es.vargontoc.educational.framework.agents.model.AgentStatusType;
import es.vargontoc.educational.framework.agents.ports.in.CheckStatusModelsUseCase;
import es.vargontoc.educational.framework.agents.ports.in.SendMessageChatbotUseCase;
import es.vargontoc.educational.framework.agents.ports.out.OllamaManagementPort;
import es.vargontoc.educational.framework.agents.utils.AgentsConstants;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AgentsService implements CheckStatusModelsUseCase, SendMessageChatbotUseCase {
    
    private static final Logger LOG = LoggerFactory.getLogger(AgentsService.class);


    private final OllamaManagementPort ollamaPort;
    private final Map<String, ChatClient> agents;

    public AgentsService(@Qualifier("list-agents") Map<String, ChatClient> agents, OllamaManagementPort port) {
        this.agents = agents;
        this.ollamaPort = port;
    }

    @Override
    public List<AgentStatus> checkAllAvailableModels(){
        return List.of(checkStatus(AgentsConstants.CHATBOT_MODEL),
            checkStatus(AgentsConstants.NPC_MODEL));
    }

    private AgentStatus checkStatus(String model) {
        LOG.info("Check model '{}' status", model);
        try {

            if(ollamaPort.isRunning(model)){
                LOG.info("Model '{}' is running", model); 
                return new AgentStatus(model, AgentStatusType.RUNNING);
            }

            if(ollamaPort.isPulled(model)) {
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

    @Override
    public String sendMessage(String message) throws ValidationException {
        
        if(message == null || message.trim().isEmpty())
            throw new ValidationException("El mensaje no puede estar vacio");

        // Sanitizamos el texto
        String sanitized = sanitize(message);

        // Arrancamos modelo
        if(checkStatus(AgentsConstants.CHATBOT_MODEL).status() == AgentStatusType.STOPPED)
            ollamaPort.run(AgentsConstants.CHATBOT_MODEL, false, null);

        // Lanzamos el prompt y recibimos el contenido
        String response = agents.get(AgentsConstants.CHATBOT_MODEL).prompt(sanitized).call().content();

        // Detenemos y devolvemos la info
        ollamaPort.stop(AgentsConstants.CHATBOT_MODEL);

        return response;
    }

    
    private String sanitize(String input) {
        return Jsoup.clean(input, Safelist.none());
    }

}
