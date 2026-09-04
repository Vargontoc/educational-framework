package es.vargontoc.educational.framework.contact.infrastructure.adapters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import es.vargontoc.educational.framework.contact.model.ContactMessage;
import es.vargontoc.educational.framework.contact.ports.out.TelegramPort;

@Component
@ConditionalOnExpression("'${app.telegram.bot.token:}' == ''")
public class NoOpTelegramAdapter implements TelegramPort {

    private static final Logger log = LoggerFactory.getLogger(NoOpTelegramAdapter.class);

    @Override
    public void sendToTelegram(ContactMessage message) {
        log.warn("Telegram no esta configurado (app.telegram.bot.token vacio); mensaje de contacto guardado sin notificar");
    }
}
