package es.vargontoc.educational.framework.tracking.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.tracking.number-unlock")
public class NumberUnlockProperties {

    private int minSuccessRatePercent = 80;
    private int minAttemptsPerCategory = 10;

    public int getMinSuccessRatePercent() {
        return minSuccessRatePercent;
    }

    public void setMinSuccessRatePercent(int minSuccessRatePercent) {
        this.minSuccessRatePercent = minSuccessRatePercent;
    }

    public int getMinAttemptsPerCategory() {
        return minAttemptsPerCategory;
    }

    public void setMinAttemptsPerCategory(int minAttemptsPerCategory) {
        this.minAttemptsPerCategory = minAttemptsPerCategory;
    }
}
