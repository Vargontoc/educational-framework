package es.vargontoc.educational.framework.contact.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import es.vargontoc.educational.framework.contact.model.ContactMessage;
import es.vargontoc.educational.framework.contact.ports.out.ContactTelegram;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@ConditionalOnMissingBean(ContactTelegram.class)
public class NoOpContactTelegram implements ContactTelegram {

    private static final Logger LOG = LoggerFactory.getLogger(NoOpContactTelegram.class);

    @Override
    public void sendToTelegram(ContactMessage message) {
        LOG.info("Telegram no configurado. Mensaje omitido [{}]: {}", message.getType(), message.getMessage());
    }
}
