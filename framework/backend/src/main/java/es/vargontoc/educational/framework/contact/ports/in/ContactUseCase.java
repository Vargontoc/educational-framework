package es.vargontoc.educational.framework.contact.ports.in;

import es.vargontoc.educational.framework.contact.model.ContactMessage;
import es.vargontoc.educational.framework.contact.model.ContactMessageType;

public interface ContactUseCase {
    ContactMessage submit(String message, ContactMessageType type, String clientIp);
}
