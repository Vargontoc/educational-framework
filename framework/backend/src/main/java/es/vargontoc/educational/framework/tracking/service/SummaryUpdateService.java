package es.vargontoc.educational.framework.tracking.service;

import es.vargontoc.educational.framework.content.ports.out.RecognitionElementRepository;
import es.vargontoc.educational.framework.tracking.config.ElementMasteryProperties;
import es.vargontoc.educational.framework.tracking.model.ActivityAttempt;
import es.vargontoc.educational.framework.tracking.model.ActivitySummary;
import es.vargontoc.educational.framework.tracking.model.ElementMasteryState;
import es.vargontoc.educational.framework.tracking.model.ElementSummary;
import es.vargontoc.educational.framework.tracking.model.TopicPerformanceBand;
import es.vargontoc.educational.framework.tracking.model.TopicSummary;
import es.vargontoc.educational.framework.tracking.ports.out.ActivitySummaryRepository;
import es.vargontoc.educational.framework.tracking.ports.out.ElementSummaryRepository;
import es.vargontoc.educational.framework.tracking.ports.out.TopicSummaryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Transactional
public class SummaryUpdateService {

    private static final Logger log = LoggerFactory.getLogger(SummaryUpdateService.class);

    private final ActivitySummaryRepository activitySummaryRepository;
    private final TopicSummaryRepository topicSummaryRepository;
    private final ElementSummaryRepository elementSummaryRepository;
    private final ElementMasteryProperties elementMasteryProperties;
    private final RecognitionElementRepository recognitionElementRepository;

    public SummaryUpdateService(
            ActivitySummaryRepository activitySummaryRepository,
            TopicSummaryRepository topicSummaryRepository,
            ElementSummaryRepository elementSummaryRepository,
            ElementMasteryProperties elementMasteryProperties,
            RecognitionElementRepository recognitionElementRepository) {
        this.activitySummaryRepository = activitySummaryRepository;
        this.topicSummaryRepository = topicSummaryRepository;
        this.elementSummaryRepository = elementSummaryRepository;
        this.elementMasteryProperties = elementMasteryProperties;
        this.recognitionElementRepository = recognitionElementRepository;
    }

    public void updateSummaries(ActivityAttempt attempt) {
        updateActivitySummary(attempt);
        updateTopicSummary(attempt);
        if (attempt.getElementId() != null) {
            updateElementSummary(attempt);
        }
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

    private void updateElementSummary(ActivityAttempt attempt) {
        if (!recognitionElementRepository.existsById(attempt.getElementId())) {
            log.warn("Skipping element summary update: elementId={} does not exist in recognition_element", attempt.getElementId());
            return;
        }

        var summary = elementSummaryRepository
            .findByChildProfileIdAndElementId(attempt.getChildProfileId(), attempt.getElementId())
            .orElseGet(() -> createElementSummary(attempt));

        summary.setTotalAttempts(summary.getTotalAttempts() + 1);

        switch (attempt.getResult()) {
            case CORRECT -> summary.setTotalCorrect(summary.getTotalCorrect() + 1);
            case INCORRECT -> summary.setTotalIncorrect(summary.getTotalIncorrect() + 1);
            case TIMEOUT -> summary.setTotalIncorrect(summary.getTotalIncorrect() + 1);
        }

        summary.setSuccessRatePercent(calculateSuccessRate(
            summary.getTotalCorrect(), summary.getTotalAttempts()));

        if (attempt.getResponseTimeMs() != null) {
            summary.setAverageResponseTimeMs(calculateIncrementalAverage(
                summary.getAverageResponseTimeMs(),
                attempt.getResponseTimeMs(),
                summary.getTotalAttempts()));
        }

        summary.setLastSeenAt(LocalDateTime.now());
        summary.setMasteryState(determineMasteryState(summary));

        elementSummaryRepository.save(summary);
    }

    private ElementSummary createElementSummary(ActivityAttempt attempt) {
        var summary = new ElementSummary();
        summary.setChildProfileId(attempt.getChildProfileId());
        summary.setElementId(attempt.getElementId());
        summary.setTotalAttempts(0);
        summary.setTotalCorrect(0);
        summary.setTotalIncorrect(0);
        summary.setAverageResponseTimeMs(0);
        summary.setMasteryState(ElementMasteryState.NOT_STARTED);
        return summary;
    }

    ElementMasteryState determineMasteryState(ElementSummary summary) {
        if (summary.getTotalAttempts() < elementMasteryProperties.getMinAttemptsForMastery()) {
            return ElementMasteryState.LEARNING;
        }
        if (summary.getSuccessRatePercent() != null
                && summary.getSuccessRatePercent().compareTo(
                        BigDecimal.valueOf(elementMasteryProperties.getMasteredSuccessRatePercent())) >= 0) {
            return ElementMasteryState.MASTERED;
        }
        return ElementMasteryState.LEARNING;
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
