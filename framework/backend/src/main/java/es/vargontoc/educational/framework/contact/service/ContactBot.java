package es.vargontoc.educational.framework.contact.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.vargontoc.educational.framework.contact.infrastructure.config.TelegramBotConfig;
import es.vargontoc.educational.framework.contact.model.ContactMessage;
import es.vargontoc.educational.framework.contact.model.ContactSendException;
import es.vargontoc.educational.framework.contact.ports.out.ContactTelegram;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@ConditionalOnExpression("'${app.telegram.bot.token:}' != ''")
public class ContactBot implements ContactTelegram {

    private static final Logger LOG = LoggerFactory.getLogger(ContactBot.class);
    private static final String TELEGRAM_API_BASE = "https://api.telegram.org/bot%s/sendMessage";

    private final TelegramBotConfig config;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private Long chatId;

    public ContactBot(TelegramBotConfig config) {
        this.config = config;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    void resolveChatId() {
        String url = String.format("https://api.telegram.org/bot%s/getUpdates", config.getToken());
        try {
            JsonNode root = objectMapper.readTree(restTemplate.getForEntity(url, String.class).getBody());
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
            LOG.warn("No se pudo obtener el chatId de Telegram: {}", e.getMessage());
        }
    }

    @Override
    public void sendToTelegram(ContactMessage message) {
        if (chatId == null) {
            throw new ContactSendException("No se pudo determinar el chatId de Telegram. Envía un mensaje al bot y reinicia la aplicación.");
        }
        try {
            String url = String.format(TELEGRAM_API_BASE, config.getToken());
            String payload = objectMapper.writeValueAsString(java.util.Map.of(
                "chat_id", chatId,
                "text", format(message),
                "parse_mode", "html"
            ));
            var headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            var entity = new org.springframework.http.HttpEntity<>(payload, headers);
            restTemplate.postForEntity(url, entity, String.class);
        } catch (ContactSendException e) {
            throw e;
        } catch (Exception e) {
            throw new ContactSendException("Error enviando mensaje a Telegram", e);
        }
    }

    private String format(ContactMessage message) {
        return switch (message.getType()) {
            case ERROR -> "<b>[ERROR]</b> " + message.getMessage();
            case SUGGEST -> "<b>[SUGGEST]</b> " + message.getMessage();
            case COMMENT -> "<b>[COMMENT]</b> " + message.getMessage();
        };
    }
}
