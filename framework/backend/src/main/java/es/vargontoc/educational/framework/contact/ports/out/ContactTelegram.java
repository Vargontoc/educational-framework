package es.vargontoc.educational.framework.contact.ports.out;

import es.vargontoc.educational.framework.contact.model.ContactMessage;

public interface ContactTelegram {
    void sendToTelegram(ContactMessage message);
}
