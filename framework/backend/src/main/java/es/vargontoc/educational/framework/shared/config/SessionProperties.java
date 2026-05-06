package es.vargontoc.educational.framework.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.session")
public class SessionProperties {

    private int defaultHeartbeatIntervalSeconds = 30;
    private int heartbeatGraceMultiplier = 2;
    private int retentionDays = 30;

    public int getDefaultHeartbeatIntervalSeconds() {
        return defaultHeartbeatIntervalSeconds;
    }

    public void setDefaultHeartbeatIntervalSeconds(int defaultHeartbeatIntervalSeconds) {
        this.defaultHeartbeatIntervalSeconds = defaultHeartbeatIntervalSeconds;
    }

    public int getHeartbeatGraceMultiplier() {
        return heartbeatGraceMultiplier;
    }

    public void setHeartbeatGraceMultiplier(int heartbeatGraceMultiplier) {
        this.heartbeatGraceMultiplier = heartbeatGraceMultiplier;
    }

    public int getRetentionDays() {
        return retentionDays;
    }

    public void setRetentionDays(int retentionDays) {
        this.retentionDays = retentionDays;
    }
}
