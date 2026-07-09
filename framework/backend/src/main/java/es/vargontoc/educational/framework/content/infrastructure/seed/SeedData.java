package es.vargontoc.educational.framework.content.infrastructure.seed;

import java.util.List;

public final class SeedData {

    private SeedData() {}

    public record CategorySeed(
        String name,
        String description,
        String status,
        Integer displayOrder
    ) {}

    public record TopicSeed(
        String name,
        String categoryName,
        String description,
        String status,
        Integer minAge,
        Integer maxAge,
        String recognitionType,
        String habitatTag
    ) {}

    public record CuriositySeed(
        String text,
        String topicName,
        Integer minAge,
        Integer maxAge,
        List<String> tags,
        String locale,
        String phoneticHint
    ) {}

    public record ActivitySeed(
        String name,
        String description,
        String gameEngineType,
        Integer minAge,
        Integer maxAge,
        String status,
        List<String> topicNames
    ) {}

    public record DifficultyLevelSeed(
        String activityName,
        String difficultyCode,
        String engineParams
    ) {}

    public record AvatarEventSeed(
        String eventType,
        String tone,
        String locale,
        String messageText,
        String status
    ) {}

    public record LearningPathSeed(
        String name,
        String description,
        Integer minAge,
        Integer maxAge,
        String locale,
        String status
    ) {}

    public record LearningPathStepSeed(
        String learningPathName,
        String activityName,
        Integer stepOrder,
        String unlockCondition
    ) {}

    public record TracingPatternSeed(
        String topicName,
        String name,
        String description,
        String patternType,
        List<List<Double>> points,
        Integer minAge,
        Integer maxAge,
        String status
    ) {}

    public record StorySeed(
        String title,
        String description,
        Integer minAge,
        Integer maxAge,
        Integer estimatedDurationMinutes,
        List<String> topicNames,
        String status
    ) {}

    public record StoryPageSeed(
        String storyTitle,
        Integer pageOrder,
        String text,
        String status
    ) {}

    public record WorldHostSeed(
        String code,
        String displayName,
        String biome,
        String description,
        Integer minAge,
        Integer maxAge,
        String status,
        Integer sortOrder,
        String visualAssetKey
    ) {}

    public record WorldNarrativeSituationSeed(
        String code,
        String displayText,
        String situationType,
        String tone,
        Integer minAge,
        Integer maxAge,
        String status,
        Integer sortOrder
    ) {}

    public record WorldDiscoveryElementSeed(
        String code,
        String displayName,
        String elementType,
        String biome,
        Integer minAge,
        Integer maxAge,
        String status,
        Long activityId,
        Long topicId,
        String visualAssetKey,
        String interactionCueType,
        Integer sortOrder
    ) {}

    public record AccessibleColorPaletteSeed(
        String colorVisionMode,
        String accessibleColorValue,
        String accessibleLabelKey
    ) {}

    public record AccessibleColorSeed(
        String conceptualIdentity,
        String labelKey,
        String shapeIcon,
        String symbol,
        String status,
        Integer sortOrder,
        List<AccessibleColorPaletteSeed> palettes
    ) {}
}
