package es.vargontoc.educational.framework.tracking.service;

import es.vargontoc.educational.framework.shared.exception.ValidationException;
import es.vargontoc.educational.framework.tracking.config.AdaptiveDifficultyProperties;
import es.vargontoc.educational.framework.tracking.model.ActivityAttempt;
import es.vargontoc.educational.framework.tracking.model.ActivitySummary;
import es.vargontoc.educational.framework.tracking.model.AdaptiveDifficultyAction;
import es.vargontoc.educational.framework.tracking.model.AdaptiveDifficultyResult;
import es.vargontoc.educational.framework.tracking.model.AttemptResult;
import es.vargontoc.educational.framework.tracking.ports.out.ActivityAttemptRepository;
import es.vargontoc.educational.framework.tracking.ports.out.ActivitySummaryRepository;
import es.vargontoc.educational.framework.tracking.ports.out.DifficultyLevelConfigPort;
import es.vargontoc.educational.framework.tracking.ports.out.DifficultyLevelNavigationPort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public class AdaptiveDifficultyService {

    private final ActivityAttemptRepository attemptRepository;
    private final ActivitySummaryRepository summaryRepository;
    private final DifficultyLevelConfigPort difficultyLevelConfigPort;
    private final DifficultyLevelNavigationPort difficultyNavigationPort;
    private final AdaptiveDifficultyProperties properties;

    public AdaptiveDifficultyService(
            ActivityAttemptRepository attemptRepository,
            ActivitySummaryRepository summaryRepository,
            DifficultyLevelConfigPort difficultyLevelConfigPort,
            DifficultyLevelNavigationPort difficultyNavigationPort,
            AdaptiveDifficultyProperties properties) {
        this.attemptRepository = attemptRepository;
        this.summaryRepository = summaryRepository;
        this.difficultyLevelConfigPort = difficultyLevelConfigPort;
        this.difficultyNavigationPort = difficultyNavigationPort;
        this.properties = properties;
    }

    public AdaptiveDifficultyResult evaluate(Long childProfileId, Long activityId, Long currentDifficultyLevelId) {
        List<ActivityAttempt> recentAttempts = attemptRepository.findRecentByChildAndActivity(
                childProfileId, activityId, properties.getSlidingWindowAttempts());

        if (recentAttempts.size() < properties.getMinAttemptsBeforeChange()) {
            return new AdaptiveDifficultyResult(AdaptiveDifficultyAction.MAINTAIN, "insufficient attempts");
        }

        ActivitySummary summary = summaryRepository.findByChildProfileIdAndActivityId(childProfileId, activityId)
                .orElse(null);

        if (summary == null) {
            return new AdaptiveDifficultyResult(AdaptiveDifficultyAction.MAINTAIN, "no summary found");
        }

        if (summary.getAttemptsSinceLastDifficultyChange() < properties.getCooldownAttempts()) {
            return new AdaptiveDifficultyResult(AdaptiveDifficultyAction.MAINTAIN, "cooldown active");
        }

        var config = parseOrGetDefault(currentDifficultyLevelId);

        double adaptiveScore = calculateAdaptiveScore(recentAttempts, config);

        if (adaptiveScore >= properties.getIncreaseThresholdPercent()) {
            Long newLevel = getNextDifficultyLevelId(currentDifficultyLevelId, activityId, true);
            if (newLevel != null && !newLevel.equals(currentDifficultyLevelId)) {
                updateDifficulty(summary, newLevel);
                return new AdaptiveDifficultyResult(AdaptiveDifficultyAction.INCREASE,
                        "score " + String.format("%.2f", adaptiveScore) + " exceeded threshold", newLevel);
            }
        }

        if (adaptiveScore <= properties.getDecreaseThresholdPercent()) {
            Long newLevel = getNextDifficultyLevelId(currentDifficultyLevelId, activityId, false);
            if (newLevel != null && !newLevel.equals(currentDifficultyLevelId)) {
                updateDifficulty(summary, newLevel);
                return new AdaptiveDifficultyResult(AdaptiveDifficultyAction.DECREASE,
                        "score " + String.format("%.2f", adaptiveScore) + " below threshold", newLevel);
            }
        }

        return new AdaptiveDifficultyResult(AdaptiveDifficultyAction.MAINTAIN,
                "score " + String.format("%.2f", adaptiveScore) + " within range");
    }

    private double calculateAdaptiveScore(List<ActivityAttempt> attempts, AdaptiveDifficultyConfig config) {
        int total = attempts.size();
        int correct = (int) attempts.stream().filter(a -> a.getResult() == AttemptResult.CORRECT).count();
        int timeouts = (int) attempts.stream().filter(a -> a.getResult() == AttemptResult.TIMEOUT).count();

        double accuracyScore = ((double) correct / total) * 100;

        double avgResponseTime = attempts.stream()
                .filter(a -> a.getResponseTimeMs() != null)
                .mapToInt(ActivityAttempt::getResponseTimeMs)
                .average()
                .orElse(config.targetResponseTimeMs);

        double speedScore = Math.max(0, 1 - ((double) avgResponseTime / config.targetResponseTimeMs)) * 100;

        double timeoutRate = (double) timeouts / total;
        double timeoutPenalty = 0;
        if (timeoutRate > (config.timeoutRateThresholdPercent / 100.0)) {
            double threshold = config.timeoutRateThresholdPercent / 100.0;
            timeoutPenalty = (timeoutRate - threshold) * config.timeoutPenaltyWeight * 100;
        }

        double adaptiveScore = (accuracyScore * config.accuracyWeight) + (speedScore * config.speedWeight) - timeoutPenalty;

        return Math.max(0, Math.min(100, adaptiveScore));
    }

    private void updateDifficulty(ActivitySummary summary, Long newLevelId) {
        summary.setCurrentDifficultyLevelId(newLevelId);
        summary.setAttemptsSinceLastDifficultyChange(0);
        summaryRepository.save(summary);
    }

    private Long getNextDifficultyLevelId(Long currentLevelId, Long activityId, boolean increase) {
        List<Long> orderedIds = difficultyNavigationPort.findOrderedDifficultyLevelIds(activityId);
        if (orderedIds.isEmpty()) {
            return null;
        }
        if (currentLevelId == null) {
            return orderedIds.get(orderedIds.size() / 2);
        }
        int currentIndex = orderedIds.indexOf(currentLevelId);
        if (currentIndex < 0) {
            return currentLevelId;
        }
        if (increase) {
            return currentIndex < orderedIds.size() - 1 ? orderedIds.get(currentIndex + 1) : currentLevelId;
        } else {
            return currentIndex > 0 ? orderedIds.get(currentIndex - 1) : currentLevelId;
        }
    }

    private AdaptiveDifficultyConfig parseOrGetDefault(Long difficultyLevelId) {
        String jsonConfig = difficultyLevelConfigPort.getAdaptiveThresholdConfig(difficultyLevelId);
        if (jsonConfig == null || jsonConfig.isBlank()) {
            return AdaptiveDifficultyConfig.fromProperties(properties);
        }
        return AdaptiveDifficultyConfig.parse(jsonConfig, properties);
    }

    record AdaptiveDifficultyConfig(
            int increaseThresholdPercent,
            int decreaseThresholdPercent,
            int minAttemptsBeforeChange,
            int cooldownAttempts,
            int targetResponseTimeMs,
            int timeoutRateThresholdPercent,
            double timeoutPenaltyWeight,
            double accuracyWeight,
            double speedWeight
    ) {
        static AdaptiveDifficultyConfig fromProperties(AdaptiveDifficultyProperties p) {
            return new AdaptiveDifficultyConfig(
                    p.getIncreaseThresholdPercent(),
                    p.getDecreaseThresholdPercent(),
                    p.getMinAttemptsBeforeChange(),
                    p.getCooldownAttempts(),
                    p.getTargetResponseTimeMs(),
                    p.getTimeoutRateThresholdPercent(),
                    p.getTimeoutPenaltyWeight(),
                    p.getAccuracyWeight(),
                    p.getSpeedWeight()
            );
        }

        static AdaptiveDifficultyConfig parse(String json, AdaptiveDifficultyProperties defaults) {
            return new AdaptiveDifficultyConfig(
                    getInt(json, "increaseThresholdPercent", defaults.getIncreaseThresholdPercent()),
                    getInt(json, "decreaseThresholdPercent", defaults.getDecreaseThresholdPercent()),
                    getInt(json, "minAttemptsBeforeChange", defaults.getMinAttemptsBeforeChange()),
                    getInt(json, "cooldownAttempts", defaults.getCooldownAttempts()),
                    getInt(json, "targetResponseTimeMs", defaults.getTargetResponseTimeMs()),
                    getInt(json, "timeoutRateThresholdPercent", defaults.getTimeoutRateThresholdPercent()),
                    getDouble(json, "timeoutPenaltyWeight", defaults.getTimeoutPenaltyWeight()),
                    getDouble(json, "accuracyWeight", defaults.getAccuracyWeight()),
                    getDouble(json, "speedWeight", defaults.getSpeedWeight())
            );
        }

        private static int getInt(String json, String key, int defaultValue) {
            String pattern = "\"" + key + "\"";
            int index = json.indexOf(pattern);
            if (index < 0) {
                return defaultValue;
            }
            int colon = json.indexOf(':', index);
            if (colon < 0) {
                return defaultValue;
            }
            int comma = json.indexOf(',', colon);
            int end = json.indexOf('}', colon);
            int endIdx = (comma > 0 && comma < end) ? comma : end;
            if (endIdx < 0) {
                return defaultValue;
            }
            String numStr = json.substring(colon + 1, endIdx).trim();
            try {
                return Integer.parseInt(numStr);
            } catch (NumberFormatException e) {
                throw new ValidationException("Invalid integer value for adaptiveThresholdConfig key '" + key + "': " + numStr);
            }
        }

        private static double getDouble(String json, String key, double defaultValue) {
            String pattern = "\"" + key + "\"";
            int index = json.indexOf(pattern);
            if (index < 0) {
                return defaultValue;
            }
            int colon = json.indexOf(':', index);
            if (colon < 0) {
                return defaultValue;
            }
            int comma = json.indexOf(',', colon);
            int end = json.indexOf('}', colon);
            int endIdx = (comma > 0 && comma < end) ? comma : end;
            if (endIdx < 0) {
                return defaultValue;
            }
            String numStr = json.substring(colon + 1, endIdx).trim();
            try {
                return Double.parseDouble(numStr);
            } catch (NumberFormatException e) {
                throw new ValidationException("Invalid decimal value for adaptiveThresholdConfig key '" + key + "': " + numStr);
            }
        }
    }
}
