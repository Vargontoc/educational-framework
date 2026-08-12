package es.vargontoc.educational.framework.agents.infrastructure.adapters;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import es.vargontoc.educational.framework.agents.infrastructure.mapper.ChatbotConversationMapper;
import es.vargontoc.educational.framework.agents.infrastructure.mapper.ChatbotMessageMapper;
import es.vargontoc.educational.framework.agents.infrastructure.persistence.ChatbotConversationJpaEntity;
import es.vargontoc.educational.framework.agents.infrastructure.persistence.ChatbotConversationJpaRepository;
import es.vargontoc.educational.framework.agents.infrastructure.persistence.ChatbotMessageJpaEntity;
import es.vargontoc.educational.framework.agents.infrastructure.persistence.ChatbotMessageJpaRepository;
import es.vargontoc.educational.framework.agents.model.ChatbotConversation;
import es.vargontoc.educational.framework.agents.model.ChatbotMessage;
import es.vargontoc.educational.framework.agents.ports.out.ChatbotConversationRepository;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;

@Repository
public class ChatbotConversationPersistenceAdapter implements ChatbotConversationRepository {

    private final ChatbotConversationJpaRepository conversationRepository;
    private final ChatbotMessageJpaRepository messageRepository;
    private final ChatbotConversationMapper conversationMapper;
    private final ChatbotMessageMapper messageMapper;


    public ChatbotConversationPersistenceAdapter(ChatbotConversationJpaRepository conversationRepository,
            ChatbotMessageJpaRepository messageRepository, ChatbotConversationMapper conversationMapper,
            ChatbotMessageMapper messageMapper) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.conversationMapper = conversationMapper;
        this.messageMapper = messageMapper;
    }

    @Override
    public ChatbotConversation save(ChatbotConversation conversation) {
        ChatbotConversationJpaEntity entity = conversationMapper.toJpa(conversation);
        ChatbotConversationJpaEntity saved = conversationRepository.save(entity);
        return conversationMapper.toDomain(saved);
    }

    @Override
    public Optional<ChatbotConversation> findByConversationIdAndFamilyId(UUID conversationId, Long familyId) {
        return conversationRepository.findByConversationIdAndFamilyId(conversationId, familyId)
            .map(conversationMapper::toDomain);
    }

    @Override
    public List<ChatbotConversation> findByFamilyIdOrderByLastMessageAtDesc(Long familyId, int limit) {
        return conversationRepository.findByFamilyIdOrderByLastMessageAtDesc(familyId)
            .stream()
            .limit(limit <= 0 ? 999: limit)
            .map(conversationMapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public ChatbotMessage saveMessage(ChatbotMessage message) {
        ChatbotMessageJpaEntity entity = messageMapper.toJpa(message);
        ChatbotMessageJpaEntity saved = messageRepository.save(entity);
        return messageMapper.toDomain(saved);
    }

    @Override
    public List<ChatbotMessage> findMessagesByConversationId(Long conversationId) {
        return messageRepository.findByConversationIdOrderByCreatedAt(conversationId)
            .stream()
            .map(messageMapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public void delete(Long conversationId) {
        Optional<ChatbotConversationJpaEntity> c = conversationRepository.findById(conversationId);
        if(!c.isPresent())
            throw new ResourceNotFoundException("Conversación no encontrada");
        conversationRepository.deleteById(conversationId);
        
    }
    
}
