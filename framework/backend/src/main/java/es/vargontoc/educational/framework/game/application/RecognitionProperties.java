package es.vargontoc.educational.framework.game.application;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.recognition")
@Validated
public class RecognitionProperties {

    @Min(1)
    private int touchTargetSizePx = 92;

    @Valid
    private Difficulty difficulty = new Difficulty();

    public int getTouchTargetSizePx() {
        return touchTargetSizePx;
    }

    public void setTouchTargetSizePx(int touchTargetSizePx) {
        this.touchTargetSizePx = touchTargetSizePx;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public static class Difficulty {

        @Valid
        private Tier easy = new Tier(2, 500, true);

        @Valid
        private Tier medium = new Tier(3, 800, false);

        @Valid
        private Tier hard = new Tier(4, 0, false);

        public Tier getEasy() {
            return easy;
        }

        public void setEasy(Tier easy) {
            this.easy = easy;
        }

        public Tier getMedium() {
            return medium;
        }

        public void setMedium(Tier medium) {
            this.medium = medium;
        }

        public Tier getHard() {
            return hard;
        }

        public void setHard(Tier hard) {
            this.hard = hard;
        }
    }

    public static class Tier {

        @Min(1)
        private int optionCount;

        @Min(0)
        private int touchEnableDelayMs;

        private boolean guideChromEnabled;

        public Tier() {
        }

        public Tier(int optionCount, int touchEnableDelayMs, boolean guideChromEnabled) {
            this.optionCount = optionCount;
            this.touchEnableDelayMs = touchEnableDelayMs;
            this.guideChromEnabled = guideChromEnabled;
        }

        public int getOptionCount() {
            return optionCount;
        }

        public void setOptionCount(int optionCount) {
            this.optionCount = optionCount;
        }

        public int getTouchEnableDelayMs() {
            return touchEnableDelayMs;
        }

        public void setTouchEnableDelayMs(int touchEnableDelayMs) {
            this.touchEnableDelayMs = touchEnableDelayMs;
        }

        public boolean isGuideChromEnabled() {
            return guideChromEnabled;
        }

        public void setGuideChromEnabled(boolean guideChromEnabled) {
            this.guideChromEnabled = guideChromEnabled;
        }
    }
}
