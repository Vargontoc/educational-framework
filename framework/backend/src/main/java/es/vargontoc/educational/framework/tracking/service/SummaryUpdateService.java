package es.vargontoc.educational.framework.tracking.service;

import es.vargontoc.educational.framework.tracking.model.ActivityAttempt;
import es.vargontoc.educational.framework.tracking.model.ActivitySummary;
import es.vargontoc.educational.framework.tracking.model.TopicPerformanceBand;
import es.vargontoc.educational.framework.tracking.model.TopicSummary;
import es.vargontoc.educational.framework.tracking.ports.out.ActivitySummaryRepository;
import es.vargontoc.educational.framework.tracking.ports.out.TopicSummaryRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Transactional
public class SummaryUpdateService {

    private final ActivitySummaryRepository activitySummaryRepository;
    private final TopicSummaryRepository topicSummaryRepository;

    public SummaryUpdateService(
            ActivitySummaryRepository activitySummaryRepository,
            TopicSummaryRepository topicSummaryRepository) {
        this.activitySummaryRepository = activitySummaryRepository;
        this.topicSummaryRepository = topicSummaryRepository;
    }

    public void updateSummaries(ActivityAttempt attempt) {
        updateActivitySummary(attempt);
        updateTopicSummary(attempt);
    }

    private void updateActivitySummary(ActivityAttempt attempt) {
        var summary = activitySummaryRepository
            .findByChildProfileIdAndActivityId(attempt.getChildProfileId(), attempt.getActivityId())
            .orElseGet(() -> createActivitySummary(attempt));

        summary.setTotalAttempts(summary.getTotalAttempts() + 1);

        switch (attempt.getResult()) {
            case CORRECT -> summary.setTotalCorrect(summary.getTotalCorrect() + 1);
            case INCORRECT -> summary.setTotalIncorrect(summary.getTotalIncorrect() + 1);
            case TIMEOUT -> summary.setTotalTimeouts(summary.getTotalTimeouts() + 1);
        }

        summary.setSuccessRatePercent(calculateSuccessRate(
            summary.getTotalCorrect(), summary.getTotalAttempts()));

        if (attempt.getResponseTimeMs() != null) {
            int countWithResponseTime = summary.getTotalAttempts() - summary.getTotalTimeouts();
            summary.setAverageResponseTimeMs(calculateIncrementalAverage(
                summary.getAverageResponseTimeMs(),
                attempt.getResponseTimeMs(),
                countWithResponseTime));
        }

        if (summary.getAttemptsSinceLastDifficultyChange() == null) {
            summary.setAttemptsSinceLastDifficultyChange(0);
        }
        summary.setAttemptsSinceLastDifficultyChange(summary.getAttemptsSinceLastDifficultyChange() + 1);

        summary.setCurrentDifficultyLevelId(attempt.getDifficultyLevelId());

        activitySummaryRepository.save(summary);
    }

    private void updateTopicSummary(ActivityAttempt attempt) {
        var summary = topicSummaryRepository
            .findByChildProfileIdAndTopicId(attempt.getChildProfileId(), attempt.getTopicId())
            .orElseGet(() -> createTopicSummary(attempt));

        summary.setTotalAttempts(summary.getTotalAttempts() + 1);

        switch (attempt.getResult()) {
            case CORRECT -> summary.setTotalCorrect(summary.getTotalCorrect() + 1);
            case INCORRECT -> summary.setTotalIncorrect(summary.getTotalIncorrect() + 1);
            case TIMEOUT -> summary.setTotalTimeouts(summary.getTotalTimeouts() + 1);
        }

        summary.setSuccessRatePercent(calculateSuccessRate(
            summary.getTotalCorrect(), summary.getTotalAttempts()));

        summary.setFailureRatePercent(calculateFailureRate(
            summary.getTotalIncorrect() + summary.getTotalTimeouts(),
            summary.getTotalAttempts()));

        summary.setPerformanceBand(determinePerformanceBand(summary.getFailureRatePercent()));

        if (attempt.getResponseTimeMs() != null) {
            int countWithResponseTime = summary.getTotalAttempts() - summary.getTotalTimeouts();
            summary.setAverageResponseTimeMs(calculateIncrementalAverage(
                summary.getAverageResponseTimeMs(),
                attempt.getResponseTimeMs(),
                countWithResponseTime));
        }

        topicSummaryRepository.save(summary);
    }

    private ActivitySummary createActivitySummary(ActivityAttempt attempt) {
        var summary = new ActivitySummary();
        summary.setChildProfileId(attempt.getChildProfileId());
        summary.setActivityId(attempt.getActivityId());
        summary.setTotalAttempts(0);
        summary.setTotalCorrect(0);
        summary.setTotalIncorrect(0);
        summary.setTotalTimeouts(0);
        summary.setAverageResponseTimeMs(0);
        summary.setAttemptsSinceLastDifficultyChange(0);
        return summary;
    }

    private TopicSummary createTopicSummary(ActivityAttempt attempt) {
        var summary = new TopicSummary();
        summary.setChildProfileId(attempt.getChildProfileId());
        summary.setTopicId(attempt.getTopicId());
        summary.setTotalAttempts(0);
        summary.setTotalCorrect(0);
        summary.setTotalIncorrect(0);
        summary.setTotalTimeouts(0);
        summary.setPerformanceBand(TopicPerformanceBand.MEDIUM);
        summary.setAverageResponseTimeMs(0);
        return summary;
    }

    private BigDecimal calculateSuccessRate(int correct, int total) {
        if (total == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(correct)
            .multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateFailureRate(int failures, int total) {
        if (total == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(failures)
            .multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    private Integer calculateIncrementalAverage(Integer currentAvg, Integer newValue, int count) {
        if (currentAvg == null || count == 0) {
            return newValue;
        }
        return currentAvg + (newValue - currentAvg) / count;
    }

    TopicPerformanceBand determinePerformanceBand(BigDecimal failureRatePercent) {
        if (failureRatePercent == null) {
            return TopicPerformanceBand.MEDIUM;
        }
        double failure = failureRatePercent.doubleValue();
        if (failure > 40) {
            return TopicPerformanceBand.WEAK;
        } else if (failure < 20) {
            return TopicPerformanceBand.STRONG;
        } else {
            return TopicPerformanceBand.MEDIUM;
        }
    }
}
