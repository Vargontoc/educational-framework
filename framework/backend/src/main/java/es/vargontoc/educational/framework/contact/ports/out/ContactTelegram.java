package es.vargontoc.educational.framework.contact.ports.out;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import es.vargontoc.educational.framework.contact.model.ContactMessage;

public interface ContactTelegram {
    void sendToTelegram(ContactMessage message) throws TelegramApiException;
}
