package es.vargontoc.educational.framework.contact.service;

import java.time.LocalDateTime;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import es.vargontoc.educational.framework.contact.infrastructure.dto.ContactRequest;
import es.vargontoc.educational.framework.contact.model.ContactMessage;
import es.vargontoc.educational.framework.contact.ports.in.ContactUseCase;
import es.vargontoc.educational.framework.contact.ports.out.ContactMessageRepository;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class ContactService implements ContactUseCase {

    private static final int MAX_MESSAGE_LENGTH = 2000;
    private final ContactMessageRepository contactMessageRepository;
    private final ContactBot bot;

    public ContactService(ContactMessageRepository contactMessageRepository, ContactBot bot) {
        this.contactMessageRepository = contactMessageRepository;
        this.bot = bot;
    }

    @Override
    public ContactMessage submit(ContactRequest request, String clientIp) throws TelegramApiException {
        // Sanitizar mensaje
        String sanitized = sanitize(request.message());

        // Validación
        if(sanitized.isBlank() || sanitized.length() > MAX_MESSAGE_LENGTH){
            throw new ValidationException("Mensaje invalido o vacio");
        }
    
        // Crear modelo
        ContactMessage result =  new ContactMessage(request.type(), sanitized, clientIp);
        result.setCreatedAt(LocalDateTime.now());
    
        // Send to telegram
        bot.sendToTelegram(result);
        
        // Save
        return contactMessageRepository.save(result);
    }

    private String sanitize(String input) {
        return Jsoup.clean(input, Safelist.none());
    }
}
