package es.vargontoc.educational.framework.contact.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import es.vargontoc.educational.framework.shared.model.BaseEntity;

@Entity
@Table(name = "telegram_notification_chat")
public class TelegramNotificationChatJpaEntity extends BaseEntity {

    @Column(name = "chat_id", nullable = false)
    private Long chatId;

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }
}
