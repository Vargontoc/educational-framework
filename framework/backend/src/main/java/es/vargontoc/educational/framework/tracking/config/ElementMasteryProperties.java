package es.vargontoc.educational.framework.tracking.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.tracking.element-mastery")
public class ElementMasteryProperties {

    private int masteredSuccessRatePercent = 80;
    private int minAttemptsForMastery = 3;

    public int getMasteredSuccessRatePercent() {
        return masteredSuccessRatePercent;
    }

    public void setMasteredSuccessRatePercent(int masteredSuccessRatePercent) {
        this.masteredSuccessRatePercent = masteredSuccessRatePercent;
    }

    public int getMinAttemptsForMastery() {
        return minAttemptsForMastery;
    }

    public void setMinAttemptsForMastery(int minAttemptsForMastery) {
        this.minAttemptsForMastery = minAttemptsForMastery;
    }
}
