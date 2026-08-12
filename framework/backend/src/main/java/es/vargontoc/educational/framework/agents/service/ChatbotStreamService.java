package es.vargontoc.educational.framework.agents.service;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import es.vargontoc.educational.framework.agents.infrastructure.websocket.ChatbotEventPublisher;
import es.vargontoc.educational.framework.agents.model.AgentStatus;
import es.vargontoc.educational.framework.agents.model.AgentStatusType;
import es.vargontoc.educational.framework.agents.model.ChatbotConversation;
import es.vargontoc.educational.framework.agents.model.ChatbotStreamEvent;
import es.vargontoc.educational.framework.agents.model.ToolCallInfo;
import es.vargontoc.educational.framework.agents.ports.in.ChatbotHistoryUseCase;
import es.vargontoc.educational.framework.agents.ports.in.ChatbotStreamUseCase;
import es.vargontoc.educational.framework.agents.ports.out.OllamaManagementPort;
import es.vargontoc.educational.framework.agents.utils.AgentsConstants;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import es.vargontoc.educational.framework.shared.security.SanitizationText;
import reactor.core.publisher.Flux;


@Service
public class ChatbotStreamService implements ChatbotStreamUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(ChatbotStreamService.class);
    private final ConcurrentHashMap<Long, UUID> activeStreams = new ConcurrentHashMap<>();

    private final ChatbotEventPublisher publisher;
    private final OllamaManagementPort ollamaPort;
    private final ChatClient chatbot;
    private final ChatbotHistoryUseCase historyChatbot;

    public ChatbotStreamService(ChatbotEventPublisher publisher,
        OllamaManagementPort ollamaPort,
        @Qualifier("chatbot-agent")ChatClient chatbot,
        ChatbotHistoryUseCase historyChatbot){
            this.publisher = publisher;
            this.ollamaPort = ollamaPort;
            this.chatbot = chatbot;
            this.historyChatbot = historyChatbot;
    }

    @Override
    public void sendMessage(String message, Long familyId) throws ValidationException {
        
        // Validar mensaje
        if(message == null || message.trim().isEmpty())
            throw new ValidationException("El mensaje no puede estar vacio");

        // Comprobar conversacion activa
        if(activeStreams.contains(familyId)){
            UUID existingConversation = activeStreams.get(familyId);
            publisher.publish(familyId, ChatbotStreamEvent.error(existingConversation, "Conversación en progreso", 0));
            return;
        }

        // Sanitizar texto
        String sanitized = SanitizationText.sanitaze(message);

        // Crear conversacion
        ChatbotConversation conversation = historyChatbot.createConversation(familyId);
        UUID conversationId = conversation.getConversationId();

        // Registrar conversacion activa
        activeStreams.put(familyId, conversationId);

        try {
            // Arrancar modelo
            if(checkStatus().status() == AgentStatusType.STOPPED)
                ollamaPort.run(AgentsConstants.CHATBOT_MODEL, false, null);
            
            // Guardar mensaje del usuario
            historyChatbot.addMessage(conversation.getId(), "USER", sanitized);
    
            // Streaming
            StringBuilder fullResponse = new StringBuilder();
            Flux<ChatResponse> flux = chatbot.prompt(sanitized).stream().chatResponse();
            flux.timeout(Duration.ofSeconds(60)).doOnNext(chatResponse -> {
                // 1. Extraer tools calls
                extractToolsCalls(chatResponse, familyId, conversationId);

                // 2. Extraer tokens
                String token = chatResponse.getResult().getOutput().getText();
                if(token != null && !token.isEmpty()){
                    fullResponse.append(token);
                    publisher.publish(familyId, ChatbotStreamEvent.token(conversationId, token));
                }
            }).doOnComplete(() -> {
                // 1. Guardar respuesta completa
                historyChatbot.addMessage(conversation.getId(), "ASSISTANT", fullResponse.toString());

                // 2. Publicar COMPLETE
                publisher.publish(familyId, ChatbotStreamEvent.complete(conversationId, fullResponse.toString()));
            
                // 3. Limpiar stream activo
                activeStreams.remove(familyId);

                LOG.info("Streaming completado para familyId={}, conversation={}", familyId, conversationId);
            }).doOnError(error -> {
                LOG.error("Error en streaming para familyId={}: {}", familyId, error.getMessage());
                
                // 1. Limpiar stream activo
                activeStreams.remove(familyId);
            }).subscribe();

        }catch(Exception e) {
            LOG.error("Error iniciando streaming para familyId={}: {}", familyId, e.getMessage());
            publisher.publish(familyId, ChatbotStreamEvent.error(conversationId, "Error al iniciar conversación", 1));
            activeStreams.remove(familyId);
        }

    }
    

    private void extractToolsCalls(ChatResponse response, Long familyId, UUID conversationId){
        try {
            var generation = response.getResult();
            if(generation == null || generation.getOutput() == null)
                return;

            var toolCalls = generation.getOutput().getToolCalls();
            if(toolCalls == null || toolCalls.isEmpty())
                return;

            toolCalls.forEach(t -> {
                String name = t.name();
                String args = t.arguments();

                var toolCallInfo = ToolCallInfo.start(name, args);
                publisher.publish(familyId, ChatbotStreamEvent.toolCallStart(conversationId, toolCallInfo));

                LOG.info("Tool call iniciada: {} con argumentos: {}", name, args);
            });
        }catch(Exception e){
            LOG.warn("Error extrayendo tool calls: {}", e.getMessage());
        }
    }

    private AgentStatus checkStatus() {
        String model = AgentsConstants.CHATBOT_MODEL;
        try {
            if(ollamaPort.isRunning(model))
            {
                return new AgentStatus(model, AgentStatusType.RUNNING);
            }

            if(ollamaPort.isPulled(model))
            {
                return new AgentStatus(model, AgentStatusType.STOPPED);
            }

            return new AgentStatus(model, AgentStatusType.UNREACHABLE);
        }catch(Exception e){
            LOG.error("Error al intentar comprobar estado chatbot: {}", e.getMessage(), e);
            return new AgentStatus(model, AgentStatusType.UNREACHABLE);
        }
    }
}
