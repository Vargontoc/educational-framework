package es.vargontoc.educational.framework.agents.infrastructure.persistence;

import es.vargontoc.educational.framework.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "chatbot_message")
public class ChatbotMessageJpaEntity extends BaseEntity {

    @Column(name = "conversation_id", nullable = false)
    private Long conversationId;
    @Column(name = "role", nullable = false, length = 20)
    private String role;
    @Column(name = "content", nullable = false, length = 4000)
    private String content;
    
    public Long getConversationId() {
        return conversationId;
    }
    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    
}
