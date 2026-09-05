package es.vargontoc.educational.framework.contact.infrastructure.adapters;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import es.vargontoc.educational.framework.contact.infrastructure.config.TelegramBotConfig;
import es.vargontoc.educational.framework.contact.infrastructure.persistence.TelegramNotificationChatJpaEntity;
import es.vargontoc.educational.framework.contact.infrastructure.persistence.TelegramNotificationChatJpaRepository;
import es.vargontoc.educational.framework.contact.model.ContactMessage;
import es.vargontoc.educational.framework.contact.model.ContactSendException;
import es.vargontoc.educational.framework.contact.ports.out.TelegramPort;

@Component
@ConditionalOnExpression("'${app.telegram.bot.token:}' != ''")
public class TelegramAdapter implements SpringLongPollingBot, LongPollingUpdateConsumer, TelegramPort {

    private static final Logger log = LoggerFactory.getLogger(TelegramAdapter.class);

    private final TelegramBotConfig config;
    private final TelegramClient telegramClient;
    private final TelegramNotificationChatJpaRepository chatRepository;

    public TelegramAdapter(TelegramBotConfig config,
                           TelegramClient telegramClient,
                           TelegramNotificationChatJpaRepository chatRepository) {
        this.config = config;
        this.telegramClient = telegramClient;
        this.chatRepository = chatRepository;
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(List<Update> updates) {
        for (Update update : updates) {
            processUpdate(update);
        }
    }

    private void processUpdate(Update update) {
        if (update.hasMessage() && update.getMessage().isCommand()) {
            String command = update.getMessage().getText();
            if ("/start".equals(command)) {
                Long chatId = update.getMessage().getChatId();
                saveChatId(chatId);
                sendConfirmation(chatId);
            }
        }
    }

    @Override
    public void sendToTelegram(ContactMessage message) {
        Long target = getRegisteredChatId();
        if (target == null) {
            throw new ContactSendException("Ningún chat de Telegram registrado. Envía /start al bot desde Telegram.");
        }

        try {
            SendMessage sendMessage = SendMessage.builder()
                    .chatId(target)
                    .text(format(message))
                    .build();
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new ContactSendException("No se pudo enviar el mensaje a Telegram", e);
        }
    }

    private void saveChatId(Long chatId) {
        Optional<TelegramNotificationChatJpaEntity> existing = chatRepository.findTopByOrderByIdDesc();
        TelegramNotificationChatJpaEntity entity;
        if (existing.isPresent()) {
            entity = existing.get();
            entity.setChatId(chatId);
        } else {
            entity = new TelegramNotificationChatJpaEntity();
            entity.setChatId(chatId);
        }
        chatRepository.save(entity);
        log.info("Telegram chatId registered: {}", chatId);
    }

    private void sendConfirmation(Long chatId) {
        try {
            SendMessage confirmation = SendMessage.builder()
                    .chatId(chatId)
                    .text("¡Listo! Este chat recibirá las notificaciones de soporte de My Friend Nubi.")
                    .build();
            telegramClient.execute(confirmation);
        } catch (TelegramApiException e) {
            log.error("Failed to send confirmation message to chat {}", chatId, e);
        }
    }

    private Long getRegisteredChatId() {
        return chatRepository.findTopByOrderByIdDesc()
                .map(e -> e.getChatId())
                .orElse(null);
    }

    private String format(ContactMessage message) {
        return switch (message.getType()) {
            case ERROR -> "[ERROR] " + message.getMessage();
            case SUGGEST -> "[SUGGEST] " + message.getMessage();
            case COMMENT -> "[COMMENT] " + message.getMessage();
        };
    }
}
