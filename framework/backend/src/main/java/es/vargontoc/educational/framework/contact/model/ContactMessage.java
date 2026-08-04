package es.vargontoc.educational.framework.contact.model;

import java.time.LocalDateTime;

public class ContactMessage {
    
    private Long id;
    private ContactMessageType type;
    private String message;
    private String clientIp;
    private LocalDateTime createdAt;

    public ContactMessage(){}
    public ContactMessage(ContactMessageType type, String message, String clientIp){
        this.type = type;
        this.message = message;
        this.clientIp = clientIp;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public ContactMessageType getType() {
        return type;
    }
    public void setType(ContactMessageType type) {
        this.type = type;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getClientIp() {
        return clientIp;
    }
    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    
    
}
