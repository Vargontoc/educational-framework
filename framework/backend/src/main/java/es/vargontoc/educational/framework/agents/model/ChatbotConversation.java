package es.vargontoc.educational.framework.agents.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Representa una conversación completa de chatbot
 */
public class ChatbotConversation {
    
    private Long id;
    private UUID conversationId;
    private Long familyId;
    private LocalDateTime startedAt;
    private LocalDateTime lastMessageAt;
    private List<ChatbotMessage> messages;

    public ChatbotConversation(){
        this.messages = new ArrayList<>();
    }

    public ChatbotConversation(Long familyId){
        this();
        this.familyId = familyId;
        this.conversationId = UUID.randomUUID();
        this.startedAt = LocalDateTime.now();
        this.lastMessageAt = LocalDateTime.now();
    }

    /**
     * Agrega un mensaje a la conversación
     * @param message
     */
    public void addMessage(ChatbotMessage message) {
        messages.add(message);
        lastMessageAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public List<ChatbotMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatbotMessage> messages) {
        this.messages = messages;
    }

    


}
