package es.vargontoc.educational.framework.contact.infrastructure.adapters;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import es.vargontoc.educational.framework.contact.model.ContactMessage;
import es.vargontoc.educational.framework.contact.model.ContactSendException;
import es.vargontoc.educational.framework.contact.ports.out.TelegramPort;

@Component
@ConditionalOnExpression("'${app.telegram.bot.token:}' == ''")
public class NoOpTelegramAdapter implements TelegramPort {

    @Override
    public void sendToTelegram(ContactMessage message) {
        throw new ContactSendException("Telegram no esta configurado (app.telegram.bot.token vacio)");
    }
}
