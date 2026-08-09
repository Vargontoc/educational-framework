package es.vargontoc.educational.framework.contact.infrastructure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import es.vargontoc.educational.framework.contact.infrastructure.adapters.TelegramAdapter;

@Configuration
@ConditionalOnExpression("'${app.telegram.bot.token:}' != ''")
public class TelegramBotRegistration {

    @Bean
    public TelegramBotsApi telegramBotsApi(TelegramAdapter telegramAdapter) throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(telegramAdapter);
        return botsApi;
    }
}
