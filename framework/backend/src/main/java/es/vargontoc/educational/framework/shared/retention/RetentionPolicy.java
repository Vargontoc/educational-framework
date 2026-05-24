package es.vargontoc.educational.framework.shared.retention;

import java.time.LocalDateTime;

public interface RetentionPolicy {

    String name();

    int retentionDays();

    int deleteExpired(LocalDateTime cutoff);
}
