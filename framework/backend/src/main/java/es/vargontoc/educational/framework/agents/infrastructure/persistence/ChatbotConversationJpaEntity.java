package es.vargontoc.educational.framework.agents.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.UUID;


import es.vargontoc.educational.framework.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "chatbot_conversation")
public class ChatbotConversationJpaEntity extends BaseEntity {
    
    @Column(name = "conversation_id", nullable = false, unique = true)
    private UUID conversationId;
    @Column(name = "family_id", nullable = false)
    private Long familyId;
    @Column(name = "last_message_at", nullable = false)
    private LocalDateTime lastMessageAt;
    @Column(name = "title")
    private String title;

    public UUID getConversationId() {
        return conversationId;
    }
    public void setConversationId(UUID conversationId) {
        this.conversationId = conversationId;
    }
    public Long getFamilyId() {
        return familyId;
    }
    public void setFamilyId(Long familyId) {
        this.familyId = familyId;
    }
    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }
    public void setLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    
    
}
