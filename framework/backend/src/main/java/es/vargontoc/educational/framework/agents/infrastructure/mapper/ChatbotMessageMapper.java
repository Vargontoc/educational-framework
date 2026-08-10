package es.vargontoc.educational.framework.agents.infrastructure.mapper;

import org.springframework.stereotype.Component;

import es.vargontoc.educational.framework.agents.infrastructure.persistence.ChatbotMessageJpaEntity;
import es.vargontoc.educational.framework.agents.model.ChatbotMessage;
import es.vargontoc.educational.framework.shared.mapper.AbstractMapper;

@Component
public class ChatbotMessageMapper extends AbstractMapper<ChatbotMessage, ChatbotMessageJpaEntity> {

    @Override
    public ChatbotMessage toDomain(ChatbotMessageJpaEntity source) {
        ChatbotMessage target = new ChatbotMessage();
        target.setId(source.getId());
        target.setConversationId(source.getConversationId());
        target.setContent(source.getContent());
        target.setRole(source.getRole());
        target.setCreatedAt(source.getCreatedAt());

        return target;
    }

    @Override
    public ChatbotMessageJpaEntity toJpa(ChatbotMessage source) {
        
        ChatbotMessageJpaEntity target = new ChatbotMessageJpaEntity();
        target.setId(source.getId());
        target.setConversationId(source.getConversationId());
        target.setRole(source.getRole());
        target.setCreatedAt(source.getCreatedAt());

        return target;
        
    }
    
}
