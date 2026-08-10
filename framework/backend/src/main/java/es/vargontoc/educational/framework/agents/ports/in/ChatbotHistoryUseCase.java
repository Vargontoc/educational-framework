package es.vargontoc.educational.framework.agents.ports.in;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import es.vargontoc.educational.framework.agents.model.ChatbotConversation;
import es.vargontoc.educational.framework.agents.model.ChatbotMessage;

/**
 * Interface de caso de uso para gestión de historial
 */
public interface ChatbotHistoryUseCase {
    
    /**
     * Crea una nueva conversación
     * @param familyId
     * @return
     */
    ChatbotConversation createConversation(Long familyId);

    /**
     * Obtiene una conversación
     * @param conversationId
     * @param familyId
     * @return
     */
    Optional<ChatbotConversation> getConversation(UUID conversationId, Long familyId);

    /**
     * Obtiene una lista de conversaciones
     * @param familyId
     * @param limit
     * @return
     */
    List<ChatbotConversation> listConversations(Long familyId, int limit);

    /**
     * Agrega un nuevo mensaje a una conversación
     * @param conversationId
     * @param role
     * @param content
     * @return
     */
    ChatbotMessage addMessage(Long conversationId, String role, String content);
}
