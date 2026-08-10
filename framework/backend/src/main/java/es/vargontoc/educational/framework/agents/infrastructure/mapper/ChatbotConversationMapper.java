package es.vargontoc.educational.framework.agents.infrastructure.mapper;

import org.springframework.stereotype.Component;

import es.vargontoc.educational.framework.agents.infrastructure.persistence.ChatbotConversationJpaEntity;
import es.vargontoc.educational.framework.agents.model.ChatbotConversation;
import es.vargontoc.educational.framework.shared.mapper.AbstractMapper;

@Component
public class ChatbotConversationMapper extends AbstractMapper<ChatbotConversation, ChatbotConversationJpaEntity> {

    @Override
    public ChatbotConversation toDomain(ChatbotConversationJpaEntity source) {
        
        ChatbotConversation target = new ChatbotConversation();
        target.setId(source.getId()); 
        target.setConversationId(source.getConversationId());
        target.setFamilyId(source.getFamilyId());
        target.setStartedAt(source.getCreatedAt());
        target.setLastMessageAt(source.getLastMessageAt());

        return target;
    }

    @Override
    public ChatbotConversationJpaEntity toJpa(ChatbotConversation source) {
        ChatbotConversationJpaEntity target = new ChatbotConversationJpaEntity();
        target.setConversationId(source.getConversationId());
        target.setId(source.getId());
        target.setCreatedAt(source.getStartedAt());
        target.setLastMessageAt(source.getLastMessageAt());
        
        return target;
    }
    
    
}
