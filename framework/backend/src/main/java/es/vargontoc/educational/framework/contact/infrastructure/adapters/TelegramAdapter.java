package es.vargontoc.educational.framework.contact.infrastructure.adapters;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.db.MapDBContext;
import org.telegram.abilitybots.api.db.Var;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;

import es.vargontoc.educational.framework.contact.infrastructure.config.TelegramBotConfig;
import es.vargontoc.educational.framework.contact.model.ContactMessage;
import es.vargontoc.educational.framework.contact.model.ContactSendException;
import es.vargontoc.educational.framework.contact.ports.out.TelegramPort;

@Component
@ConditionalOnExpression("'${app.telegram.bot.token:}' != ''")
public class TelegramAdapter extends AbilityBot implements TelegramPort {

    private static final String CHAT_ID_VAR = "notificationChatId";
    private static final String DB_NAME = "cache/telegram-bot-db";

    private final Var<Long> chatId;

    public TelegramAdapter(TelegramBotConfig config) {
        super(config.getToken(), config.getName(), MapDBContext.offlineInstance(DB_NAME));
        this.chatId = db().getVar(CHAT_ID_VAR);
    }

    @Override
    public long creatorId() {
        return 0L;
    }

    public Ability startCommand() {
        return Ability.builder()
            .name("start")
            .info("Registra este chat para recibir las notificaciones de soporte de My Friend Nubi")
            .locality(Locality.USER)
            .privacy(Privacy.PUBLIC)
            .action(ctx -> {
                chatId.set(ctx.chatId());
                silent.send("¡Listo! Este chat recibirá las notificaciones de soporte de My Friend Nubi.", ctx.chatId());
            })
            .build();
    }

    @Override
    public void sendToTelegram(ContactMessage message) {
        Long target = chatId.get();
        if (target == null) {
            throw new ContactSendException("Ningún chat de Telegram registrado. Envía /start al bot desde Telegram.");
        }

        var result = silent.send(format(message), target);
        if (result.isEmpty()) {
            throw new ContactSendException("No se pudo enviar el mensaje a Telegram");
        }
    }

    private String format(ContactMessage message) {
        return switch (message.getType()) {
            case ERROR -> "[ERROR] " + message.getMessage();
            case SUGGEST -> "[SUGGEST] " + message.getMessage();
            case COMMENT -> "[COMMENT] " + message.getMessage();
        };
    }
}
