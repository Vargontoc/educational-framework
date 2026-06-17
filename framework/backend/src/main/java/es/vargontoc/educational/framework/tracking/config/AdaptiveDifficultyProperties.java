package es.vargontoc.educational.framework.tracking.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.tracking.adaptive-difficulty")
public class AdaptiveDifficultyProperties {

    private int slidingWindowAttempts = 10;
    private int increaseThresholdPercent = 75;
    private int decreaseThresholdPercent = 40;
    private int minAttemptsBeforeChange = 5;
    private int cooldownAttempts = 3;
    private int targetResponseTimeMs = 5000;
    private int timeoutRateThresholdPercent = 30;
    private double timeoutPenaltyWeight = 0.3;
    private double accuracyWeight = 0.7;
    private double speedWeight = 0.3;

    public int getSlidingWindowAttempts() {
        return slidingWindowAttempts;
    }

    public void setSlidingWindowAttempts(int slidingWindowAttempts) {
        this.slidingWindowAttempts = slidingWindowAttempts;
    }

    public int getIncreaseThresholdPercent() {
        return increaseThresholdPercent;
    }

    public void setIncreaseThresholdPercent(int increaseThresholdPercent) {
        this.increaseThresholdPercent = increaseThresholdPercent;
    }

    public int getDecreaseThresholdPercent() {
        return decreaseThresholdPercent;
    }

    public void setDecreaseThresholdPercent(int decreaseThresholdPercent) {
        this.decreaseThresholdPercent = decreaseThresholdPercent;
    }

    public int getMinAttemptsBeforeChange() {
        return minAttemptsBeforeChange;
    }

    public void setMinAttemptsBeforeChange(int minAttemptsBeforeChange) {
        this.minAttemptsBeforeChange = minAttemptsBeforeChange;
    }

    public int getCooldownAttempts() {
        return cooldownAttempts;
    }

    public void setCooldownAttempts(int cooldownAttempts) {
        this.cooldownAttempts = cooldownAttempts;
    }

    public int getTargetResponseTimeMs() {
        return targetResponseTimeMs;
    }

    public void setTargetResponseTimeMs(int targetResponseTimeMs) {
        this.targetResponseTimeMs = targetResponseTimeMs;
    }

    public int getTimeoutRateThresholdPercent() {
        return timeoutRateThresholdPercent;
    }

    public void setTimeoutRateThresholdPercent(int timeoutRateThresholdPercent) {
        this.timeoutRateThresholdPercent = timeoutRateThresholdPercent;
    }

    public double getTimeoutPenaltyWeight() {
        return timeoutPenaltyWeight;
    }

    public void setTimeoutPenaltyWeight(double timeoutPenaltyWeight) {
        this.timeoutPenaltyWeight = timeoutPenaltyWeight;
    }

    public double getAccuracyWeight() {
        return accuracyWeight;
    }

    public void setAccuracyWeight(double accuracyWeight) {
        this.accuracyWeight = accuracyWeight;
    }

    public double getSpeedWeight() {
        return speedWeight;
    }

    public void setSpeedWeight(double speedWeight) {
        this.speedWeight = speedWeight;
    }
}
