package es.vargontoc.educational.framework.game.model.recognition;

import es.vargontoc.educational.framework.content.model.DifficultyCode;
import es.vargontoc.educational.framework.game.application.RecognitionProperties;

import java.util.EnumMap;
import java.util.Map;

public class RecognitionDifficultyConfig {

    public record LadderTier(
            int optionCount,
            DistractorStrategy distractorStrategy,
            boolean guideChromEnabled,
            int touchEnableDelayMs) {
    }

    private final Map<DifficultyCode, LadderTier> tiers;

    public RecognitionDifficultyConfig(RecognitionProperties properties) {
        this.tiers = new EnumMap<>(DifficultyCode.class);
        RecognitionProperties.Difficulty difficulty = properties.getDifficulty();
        tiers.put(DifficultyCode.EASY, new LadderTier(
                difficulty.getEasy().getOptionCount(),
                DistractorStrategy.SEMANTICALLY_FAR,
                difficulty.getEasy().isGuideChromEnabled(),
                difficulty.getEasy().getTouchEnableDelayMs()));
        tiers.put(DifficultyCode.MEDIUM, new LadderTier(
                difficulty.getMedium().getOptionCount(),
                DistractorStrategy.SAME_CATEGORY,
                difficulty.getMedium().isGuideChromEnabled(),
                difficulty.getMedium().getTouchEnableDelayMs()));
        tiers.put(DifficultyCode.HARD, new LadderTier(
                difficulty.getHard().getOptionCount(),
                DistractorStrategy.SIMILAR_OUTLINE,
                difficulty.getHard().isGuideChromEnabled(),
                difficulty.getHard().getTouchEnableDelayMs()));
    }

    public LadderTier tierFor(DifficultyCode difficultyCode) {
        return tiers.get(difficultyCode);
    }
}
