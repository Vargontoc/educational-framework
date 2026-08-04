package es.vargontoc.educational.framework.contact.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.vargontoc.educational.framework.contact.infrastructure.config.TelegramBotConfig;
import es.vargontoc.educational.framework.contact.model.ContactMessage;
import es.vargontoc.educational.framework.contact.ports.out.ContactTelegram;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ContactBot extends AbilityBot implements ContactTelegram {
    
    private final static Logger LOG = LoggerFactory.getLogger(ContactBot.class);
    private Long chatId;

    public ContactBot(TelegramBotConfig config) {
        super(config.getToken(), config.getName());

        String urlTarget = String.format("https://api.telegram.org/bot%s/getUpdates", config.getToken());
        RestTemplate restTemplate = new RestTemplate();

        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(restTemplate.getForEntity(urlTarget, String.class).getBody());
            JsonNode results = root.path("result");
            for (int i = results.size() - 1; i >= 0 && chatId == null; i--) {
                JsonNode chat = results.get(i).path("message").path("chat");
                if (chat.has("id")) {
                    chatId = chat.get("id").asLong();
                }
            }
            if (chatId == null) {
                
                LOG.warn("No hay mensajes pendientes en Telegram para obtener el chatId. Envía un mensaje al bot y reinicia la aplicación.");
            } else {
                LOG.info("chatId de Telegram obtenido: {}", chatId);
            }
        } catch (Exception e) {
            LOG.error("No se pudo obtener el chatId de telegram", e);
        }
    }

    @Override
    public long creatorId() {
        return chatId != null ? chatId : 0L;
    }

    @Override
    public void sendToTelegram(ContactMessage message) throws TelegramApiException {
        if (chatId == null) {
            throw new TelegramApiException("No se pudo determinar el chatId de Telegram. Envía un mensaje al bot y reinicia la aplicación.");
        }
        SendMessage send = new SendMessage();
        send.setChatId(chatId);
        send.setText(format(message));
        send.setParseMode("html");
        sender.execute(send);
    }



    private @NonNull String format(ContactMessage message) {
        switch (message.getType()) {
            case ERROR:
                return "<b>[ERROR]</b> " + message.getMessage();
            case SUGGEST:
                return "<b>[SUGGET]</b> " + message.getMessage();
            case COMMENT:
            default:
                return "<b>[COMMENT]</b> " + message.getMessage();
        }
    }
}
