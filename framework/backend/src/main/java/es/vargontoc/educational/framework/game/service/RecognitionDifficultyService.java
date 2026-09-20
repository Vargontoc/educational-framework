package es.vargontoc.educational.framework.game.service;

import es.vargontoc.educational.framework.content.model.DifficultyCode;
import es.vargontoc.educational.framework.family.model.ColorVisionMode;
import es.vargontoc.educational.framework.game.model.enums.RecognitionCategory;
import es.vargontoc.educational.framework.game.model.recognition.RecognitionDifficultyConfig;
import es.vargontoc.educational.framework.game.model.recognition.RoundParameters;

public class RecognitionDifficultyService {

    private final RecognitionDifficultyConfig config;

    public RecognitionDifficultyService(RecognitionDifficultyConfig config) {
        this.config = config;
    }

    public RoundParameters resolveRoundParameters(
            DifficultyCode difficultyCode,
            RecognitionCategory category,
            ColorVisionMode colorVisionMode) {
        var tier = config.tierFor(difficultyCode);
        boolean nonChromaticKeyRequired =
                category == RecognitionCategory.COLOR && colorVisionMode != ColorVisionMode.NONE;

        // showIcon is true for EASY and MEDIUM, false for HARD
        boolean showIcon = difficultyCode != DifficultyCode.HARD;

        return new RoundParameters(
                tier.optionCount(),
                tier.distractorStrategy(),
                tier.guideChromEnabled(),
                tier.touchEnableDelayMs(),
                nonChromaticKeyRequired,
                showIcon);
    }
}
