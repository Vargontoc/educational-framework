package es.vargontoc.educational.framework.agents.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.vargontoc.educational.framework.agents.model.ChatbotConversation;
import es.vargontoc.educational.framework.agents.model.ChatbotMessage;
import es.vargontoc.educational.framework.agents.ports.in.ChatbotHistoryUseCase;
import es.vargontoc.educational.framework.agents.ports.out.ChatbotConversationRepository;

@Service
@Transactional
public class ChatbotHistoryService implements ChatbotHistoryUseCase {
    
    private final ChatbotConversationRepository repository;

    public ChatbotHistoryService(ChatbotConversationRepository repository){
        this.repository = repository;
    }

    @Override
    public ChatbotConversation createConversation(Long familyId) {
        return repository.save(new ChatbotConversation(familyId));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ChatbotConversation> getConversation(UUID conversationId, Long familyId) {
        Optional<ChatbotConversation> conversation = repository.findByConversationIdAndFamilyId(conversationId, familyId);
        conversation.ifPresent(c -> {
            List<ChatbotMessage> messages = repository.findMessagesByConversationId(c.getId());
            c.getMessages().addAll(messages);
        });
        return conversation;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatbotConversation> listConversations(Long familyId, int limit) {
        return repository.findByFamilyIdOrderByLastMessageAtDesc(familyId, limit);
    }

    @Override
    public ChatbotMessage addMessage(Long conversationId, String role, String content) {
        return repository.saveMessage(new ChatbotMessage(conversationId, role, content));
    }

    
}
