package es.vargontoc.educational.framework.contact.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Configuration
@ConditionalOnExpression("'${app.telegram.bot.token:}' != ''")
public class TelegramBotConfig {

    @Value("${app.telegram.bot.token}")
    private String token;

    public String getToken() {
        return token;
    }

    @Bean
    public TelegramClient telegramClient() {
        return new OkHttpTelegramClient(token);
    }
}
