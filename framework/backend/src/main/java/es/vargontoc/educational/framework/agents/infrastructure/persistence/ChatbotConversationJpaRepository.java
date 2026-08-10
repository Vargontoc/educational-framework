package es.vargontoc.educational.framework.agents.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;


public interface ChatbotConversationJpaRepository extends JpaRepository<ChatbotConversationJpaEntity, Long> {
    
    Optional<ChatbotConversationJpaEntity> findByConversationIdAndFamilyId(UUID conversationId, Long familyId);

    @Query("SELECT c FROM ChatbotConversationJpaEntity c WHERE c.familyId = :familyId ORDER BY c.lastMessageAt DESC")
    List<ChatbotConversationJpaEntity> findByFamilyIdOrderByLastMessageAtDesc(@Param("familyId") Long familyid);
}
