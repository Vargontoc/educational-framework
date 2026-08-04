package es.vargontoc.educational.framework.contact.ports.out;

import java.time.LocalDateTime;

import es.vargontoc.educational.framework.contact.model.ContactMessage;

public interface ContactMessageRepository {
    
    ContactMessage save(ContactMessage message);

    long countByClientIpAndCreatedAtAfter(String clientIp, LocalDateTime since);
}
