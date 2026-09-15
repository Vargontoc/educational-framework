package es.vargontoc.educational.framework.game.model.recognition;

import es.vargontoc.educational.framework.content.model.DifficultyCode;

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

    public RecognitionDifficultyConfig() {
        this.tiers = new EnumMap<>(DifficultyCode.class);
        tiers.put(DifficultyCode.EASY, new LadderTier(2, DistractorStrategy.SEMANTICALLY_FAR, true, 500));
        tiers.put(DifficultyCode.MEDIUM, new LadderTier(3, DistractorStrategy.SAME_CATEGORY, false, 800));
        tiers.put(DifficultyCode.HARD, new LadderTier(4, DistractorStrategy.SIMILAR_OUTLINE, false, 0));
    }

    public LadderTier tierFor(DifficultyCode difficultyCode) {
        return tiers.get(difficultyCode);
    }
}
