package es.vargontoc.educational.framework.contact.service;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.vargontoc.educational.framework.contact.model.ContactMessage;
import es.vargontoc.educational.framework.contact.model.ContactMessageType;
import es.vargontoc.educational.framework.contact.model.ContactSendException;
import es.vargontoc.educational.framework.contact.ports.in.ContactUseCase;
import es.vargontoc.educational.framework.contact.ports.out.ContactMessageRepository;
import es.vargontoc.educational.framework.contact.ports.out.TelegramPort;
import es.vargontoc.educational.framework.shared.exception.ValidationException;

@Service
@Transactional
public class ContactService implements ContactUseCase {

    private static final int MAX_MESSAGE_LENGTH = 2000;
    private final ContactMessageRepository contactMessageRepository;
    private final TelegramPort contactTelegram;

    public ContactService(ContactMessageRepository contactMessageRepository, TelegramPort contactTelegram) {
        this.contactMessageRepository = contactMessageRepository;
        this.contactTelegram = contactTelegram;
    }

    @Override
    public ContactMessage submit(String message, ContactMessageType type, String clientIp) {
        String sanitized = sanitize(message);

        if (sanitized.isBlank() || sanitized.length() > MAX_MESSAGE_LENGTH) {
            throw new ValidationException("Mensaje invalido o vacio");
        }

        ContactMessage result = new ContactMessage(type, sanitized, clientIp);

        try {
            contactTelegram.sendToTelegram(result);
        } catch (ContactSendException e) {
            throw e;
        } catch (Exception e) {
            throw new ContactSendException("Error enviando mensaje a Telegram", e);
        }

        return contactMessageRepository.save(result);
    }

    private String sanitize(String input) {
        return Jsoup.clean(input, Safelist.none());
    }
}
