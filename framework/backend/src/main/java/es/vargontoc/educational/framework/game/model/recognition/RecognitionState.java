package es.vargontoc.educational.framework.game.model.recognition;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import es.vargontoc.educational.framework.game.model.enums.RecognitionCategory;

public class RecognitionState {

    private RecognitionCategory recognitionCategory;
    private int roundIndex;
    private int totalRounds;
    private String targetElementId;
    private List<String> optionIds;
    private String selectedOptionId;
    private List<String> roundsShownElementIds;
    private int currentRoundAttemptCount;
    private int currentRoundConsecutiveFailures;
    private int totalIncorrectAttempts;
    private int totalCorrectFirstTry;
    private boolean hintActive;
    private Integer hintTriggeredAtAttempt;
    private LocalDateTime roundStartedAt;
    private LocalDateTime lastActionAt;
    private long totalResponseTimeMs;
    private List<String> candidateElementIds;
    private List<RoundAttemptRecord> roundAttempts;
    private Integer optionCount;
    private DistractorStrategy distractorStrategy;
    private boolean guideChromEnabled;
    private int touchEnableDelayMs;
    private boolean nonChromaticKeyRequired;
    private List<CandidateMetadata> candidateMetadata;
    private boolean showIcon;

    public RecognitionState() {
        this.totalRounds = RecognitionDefaults.DEFAULT_TOTAL_ROUNDS;
        this.optionIds = new ArrayList<>();
        this.roundsShownElementIds = new ArrayList<>();
        this.roundAttempts = new ArrayList<>();
        this.candidateMetadata = new ArrayList<>();
    }

    public RecognitionCategory getRecognitionCategory() {
        return recognitionCategory;
    }

    public void setRecognitionCategory(RecognitionCategory recognitionCategory) {
        this.recognitionCategory = recognitionCategory;
    }

    public int getRoundIndex() {
        return roundIndex;
    }

    public void setRoundIndex(int roundIndex) {
        this.roundIndex = roundIndex;
    }

    public int getTotalRounds() {
        return totalRounds;
    }

    public void setTotalRounds(int totalRounds) {
        this.totalRounds = totalRounds;
    }

    public String getTargetElementId() {
        return targetElementId;
    }

    public void setTargetElementId(String targetElementId) {
        this.targetElementId = targetElementId;
    }

    public List<String> getOptionIds() {
        return optionIds;
    }

    public void setOptionIds(List<String> optionIds) {
        this.optionIds = optionIds;
    }

    public String getSelectedOptionId() {
        return selectedOptionId;
    }

    public void setSelectedOptionId(String selectedOptionId) {
        this.selectedOptionId = selectedOptionId;
    }

    public List<String> getRoundsShownElementIds() {
        return roundsShownElementIds;
    }

    public void setRoundsShownElementIds(List<String> roundsShownElementIds) {
        this.roundsShownElementIds = roundsShownElementIds;
    }

    public int getCurrentRoundAttemptCount() {
        return currentRoundAttemptCount;
    }

    public void setCurrentRoundAttemptCount(int currentRoundAttemptCount) {
        this.currentRoundAttemptCount = currentRoundAttemptCount;
    }

    public int getCurrentRoundConsecutiveFailures() {
        return currentRoundConsecutiveFailures;
    }

    public void setCurrentRoundConsecutiveFailures(int currentRoundConsecutiveFailures) {
        this.currentRoundConsecutiveFailures = currentRoundConsecutiveFailures;
    }

    public int getTotalIncorrectAttempts() {
        return totalIncorrectAttempts;
    }

    public void setTotalIncorrectAttempts(int totalIncorrectAttempts) {
        this.totalIncorrectAttempts = totalIncorrectAttempts;
    }

    public int getTotalCorrectFirstTry() {
        return totalCorrectFirstTry;
    }

    public void setTotalCorrectFirstTry(int totalCorrectFirstTry) {
        this.totalCorrectFirstTry = totalCorrectFirstTry;
    }

    public boolean isHintActive() {
        return hintActive;
    }

    public void setHintActive(boolean hintActive) {
        this.hintActive = hintActive;
    }

    public Integer getHintTriggeredAtAttempt() {
        return hintTriggeredAtAttempt;
    }

    public void setHintTriggeredAtAttempt(Integer hintTriggeredAtAttempt) {
        this.hintTriggeredAtAttempt = hintTriggeredAtAttempt;
    }

    public LocalDateTime getRoundStartedAt() {
        return roundStartedAt;
    }

    public void setRoundStartedAt(LocalDateTime roundStartedAt) {
        this.roundStartedAt = roundStartedAt;
    }

    public LocalDateTime getLastActionAt() {
        return lastActionAt;
    }

    public void setLastActionAt(LocalDateTime lastActionAt) {
        this.lastActionAt = lastActionAt;
    }

    public long getTotalResponseTimeMs() {
        return totalResponseTimeMs;
    }

    public void setTotalResponseTimeMs(long totalResponseTimeMs) {
        this.totalResponseTimeMs = totalResponseTimeMs;
    }

    public List<String> getCandidateElementIds() {
        return candidateElementIds;
    }

    public void setCandidateElementIds(List<String> candidateElementIds) {
        this.candidateElementIds = candidateElementIds;
    }

    public List<RoundAttemptRecord> getRoundAttempts() {
        return roundAttempts;
    }

    public void setRoundAttempts(List<RoundAttemptRecord> roundAttempts) {
        this.roundAttempts = roundAttempts;
    }

    public Integer getOptionCount() {
        return optionCount;
    }

    public void setOptionCount(Integer optionCount) {
        this.optionCount = optionCount;
    }

    public DistractorStrategy getDistractorStrategy() {
        return distractorStrategy;
    }

    public void setDistractorStrategy(DistractorStrategy distractorStrategy) {
        this.distractorStrategy = distractorStrategy;
    }

    public boolean isGuideChromEnabled() {
        return guideChromEnabled;
    }

    public void setGuideChromEnabled(boolean guideChromEnabled) {
        this.guideChromEnabled = guideChromEnabled;
    }

    public int getTouchEnableDelayMs() {
        return touchEnableDelayMs;
    }

    public void setTouchEnableDelayMs(int touchEnableDelayMs) {
        this.touchEnableDelayMs = touchEnableDelayMs;
    }

    public boolean isNonChromaticKeyRequired() {
        return nonChromaticKeyRequired;
    }

    public void setNonChromaticKeyRequired(boolean nonChromaticKeyRequired) {
        this.nonChromaticKeyRequired = nonChromaticKeyRequired;
    }

    public List<CandidateMetadata> getCandidateMetadata() {
        return candidateMetadata;
    }

    public void setCandidateMetadata(List<CandidateMetadata> candidateMetadata) {
        this.candidateMetadata = candidateMetadata;
    }

    public boolean isShowIcon() {
        return showIcon;
    }

    public void setShowIcon(boolean showIcon) {
        this.showIcon = showIcon;
    }
}
