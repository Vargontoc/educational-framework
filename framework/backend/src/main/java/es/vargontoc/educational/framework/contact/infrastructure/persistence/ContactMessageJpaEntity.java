package es.vargontoc.educational.framework.contact.infrastructure.persistence;

import es.vargontoc.educational.framework.contact.model.ContactMessageType;
import es.vargontoc.educational.framework.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "contact_message")
public class ContactMessageJpaEntity extends BaseEntity {
    
    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ContactMessageType type;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "clientIp", nullable = false)
    private String clientIp;


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


    
}
