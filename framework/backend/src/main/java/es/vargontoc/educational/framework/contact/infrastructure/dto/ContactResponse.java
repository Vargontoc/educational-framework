package es.vargontoc.educational.framework.contact.infrastructure.dto;

import java.time.OffsetDateTime;

public record ContactResponse(boolean sent, OffsetDateTime timestamp) {
    public static ContactResponse ok() {
        return new ContactResponse(true, OffsetDateTime.now());
    }

    public static ContactResponse ko(){
        return new ContactResponse(false, OffsetDateTime.now());
    }
}
