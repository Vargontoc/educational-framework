package es.vargontoc.educational.framework.contact.ports.in;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import es.vargontoc.educational.framework.contact.infrastructure.dto.ContactRequest;
import es.vargontoc.educational.framework.contact.model.ContactMessage;

public interface ContactUseCase {
    ContactMessage submit(ContactRequest request, String clientIp) throws TelegramApiException;
}
