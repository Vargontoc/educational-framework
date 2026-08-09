package es.vargontoc.educational.framework.contact.ports.out;

import es.vargontoc.educational.framework.contact.model.ContactMessage;

public interface TelegramPort {
    void sendToTelegram(ContactMessage message);
}
