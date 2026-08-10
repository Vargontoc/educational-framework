package es.vargontoc.educational.framework.agents.ports.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import es.vargontoc.educational.framework.agents.model.ChatbotConversation;
import es.vargontoc.educational.framework.agents.model.ChatbotMessage;

/**
 * Interface del repositorio para la persistencia de conversaciones
 */
public interface ChatbotConversationRepository {
    
    ChatbotConversation save(ChatbotConversation conversation);

    Optional<ChatbotConversation> findByConversationIdAndFamilyId(UUID conversationId, Long familyId);

    List<ChatbotConversation> findByFamilyIdOrderByLastMessageAtDesc(Long familyId, int limit);

    void delete(Long conversationId);

    ChatbotMessage saveMessage(ChatbotMessage message);

    List<ChatbotMessage> findMessagesByConversationId(Long conversationId);
}
