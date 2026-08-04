package es.vargontoc.educational.framework.contact.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TelegramBotConfig {
    
    @Value("${app.telegram.bot.name}")
    private String name;
    @Value("${app.telegram.bot.token}")
    private String token;

    public String getName() {
        return name;
    }
    public String getToken() {
        return token;
    }
}
