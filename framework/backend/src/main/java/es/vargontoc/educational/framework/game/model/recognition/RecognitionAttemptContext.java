package es.vargontoc.educational.framework.game.model.recognition;

import java.util.List;

import es.vargontoc.educational.framework.game.model.enums.EngineType;
import es.vargontoc.educational.framework.game.model.enums.RecognitionCategory;

public class RecognitionAttemptContext {

    private EngineType engineType;
    private RecognitionCategory recognitionCategory;
    private int roundIndex;
    private String targetElementId;
    private String selectedOptionId;
    private List<String> optionIds;
    private boolean isFirstTry;
    private boolean hintActive;
    private boolean hintTriggeredBeforeAnswer;
    private int attemptNumberInRound;
    private long responseTimeMs;

    public RecognitionAttemptContext() {
        this.engineType = EngineType.RECOGNITION;
    }

    public EngineType getEngineType() {
        return engineType;
    }

    public void setEngineType(EngineType engineType) {
        this.engineType = engineType;
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

    public String getTargetElementId() {
        return targetElementId;
    }

    public void setTargetElementId(String targetElementId) {
        this.targetElementId = targetElementId;
    }

    public String getSelectedOptionId() {
        return selectedOptionId;
    }

    public void setSelectedOptionId(String selectedOptionId) {
        this.selectedOptionId = selectedOptionId;
    }

    public List<String> getOptionIds() {
        return optionIds;
    }

    public void setOptionIds(List<String> optionIds) {
        this.optionIds = optionIds;
    }

    public boolean isFirstTry() {
        return isFirstTry;
    }

    public void setFirstTry(boolean firstTry) {
        isFirstTry = firstTry;
    }

    public boolean isHintActive() {
        return hintActive;
    }

    public void setHintActive(boolean hintActive) {
        this.hintActive = hintActive;
    }

    public boolean isHintTriggeredBeforeAnswer() {
        return hintTriggeredBeforeAnswer;
    }

    public void setHintTriggeredBeforeAnswer(boolean hintTriggeredBeforeAnswer) {
        this.hintTriggeredBeforeAnswer = hintTriggeredBeforeAnswer;
    }

    public int getAttemptNumberInRound() {
        return attemptNumberInRound;
    }

    public void setAttemptNumberInRound(int attemptNumberInRound) {
        this.attemptNumberInRound = attemptNumberInRound;
    }

    public long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }
}
