package es.vargontoc.educational.framework.agents.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatbotMessageJpaRepository extends JpaRepository<ChatbotMessageJpaEntity, Long> {
    
    List<ChatbotMessageJpaEntity> findByConversationIdOrderByCreatedAt(Long conversationId);
}
