package es.vargontoc.educational.framework.tracking.service;

import es.vargontoc.educational.framework.tracking.infrastructure.dto.ActivityPerformanceResponse;
import es.vargontoc.educational.framework.tracking.infrastructure.dto.ActivityResponseTime;
import es.vargontoc.educational.framework.tracking.infrastructure.dto.ChildTrackingSummaryResponse;
import es.vargontoc.educational.framework.tracking.infrastructure.dto.DifficultyChangeRecord;
import es.vargontoc.educational.framework.tracking.infrastructure.dto.DifficultyEvolutionResponse;
import es.vargontoc.educational.framework.tracking.infrastructure.dto.ResponseTimeMetricsResponse;
import es.vargontoc.educational.framework.tracking.infrastructure.dto.TopicPerformanceResponse;
import es.vargontoc.educational.framework.tracking.model.ActivitySummary;
import es.vargontoc.educational.framework.tracking.model.ChildAchievement;
import es.vargontoc.educational.framework.tracking.model.ChildLearningProgress;
import es.vargontoc.educational.framework.tracking.model.DifficultyEvolution;
import es.vargontoc.educational.framework.tracking.model.TopicSummary;
import es.vargontoc.educational.framework.tracking.ports.out.ActivitySummaryRepository;
import es.vargontoc.educational.framework.tracking.ports.out.ChildAchievementRepository;
import es.vargontoc.educational.framework.tracking.ports.out.ChildLearningCompletedStepRepository;
import es.vargontoc.educational.framework.tracking.ports.out.ChildLearningProgressRepository;
import es.vargontoc.educational.framework.tracking.ports.out.DifficultyEvolutionRepository;
import es.vargontoc.educational.framework.tracking.ports.out.TopicSummaryRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
public class TrackingDashboardService {

    private final ActivitySummaryRepository activitySummaryAdapter;
    private final TopicSummaryRepository topicSummaryAdapter;
    private final ChildAchievementRepository achievementAdapter;
    private final ChildLearningProgressRepository progressAdapter;
    private final ChildLearningCompletedStepRepository completedStepAdapter;
    private final DifficultyEvolutionRepository difficultyEvolutionAdapter;

    public TrackingDashboardService(
            ActivitySummaryRepository activitySummaryAdapter,
            TopicSummaryRepository topicSummaryAdapter,
            ChildAchievementRepository achievementAdapter,
            ChildLearningProgressRepository progressAdapter,
            ChildLearningCompletedStepRepository completedStepAdapter,
            DifficultyEvolutionRepository difficultyEvolutionAdapter) {
        this.activitySummaryAdapter = activitySummaryAdapter;
        this.topicSummaryAdapter = topicSummaryAdapter;
        this.achievementAdapter = achievementAdapter;
        this.progressAdapter = progressAdapter;
        this.completedStepAdapter = completedStepAdapter;
        this.difficultyEvolutionAdapter = difficultyEvolutionAdapter;
    }

    public ChildTrackingSummaryResponse getChildTrackingSummary(Long childProfileId) {
        var activitySummaries = activitySummaryAdapter.findByChildProfileId(childProfileId);
        var topicSummaries = topicSummaryAdapter.findByChildProfileId(childProfileId);
        var achievements = achievementAdapter.findByChildProfileId(childProfileId);
        var learningProgressList = progressAdapter.findByChildProfileId(childProfileId);

        int totalAttempts = activitySummaries.stream().mapToInt(ActivitySummary::getTotalAttempts).sum();
        int totalCorrect = activitySummaries.stream().mapToInt(ActivitySummary::getTotalCorrect).sum();
        int totalIncorrect = activitySummaries.stream().mapToInt(ActivitySummary::getTotalIncorrect).sum();
        int totalTimeouts = activitySummaries.stream().mapToInt(ActivitySummary::getTotalTimeouts).sum();

        BigDecimal overallSuccessRate = BigDecimal.ZERO;
        if (totalAttempts > 0) {
            overallSuccessRate = BigDecimal.valueOf(totalCorrect)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalAttempts), 2, RoundingMode.HALF_UP);
        }

        LocalDateTime lastActivity = activitySummaries.stream()
                .map(ActivitySummary::getUpdatedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        int totalStepsCompleted = completedStepAdapter.findByChildProfileId(childProfileId).size();

        var response = new ChildTrackingSummaryResponse();
        response.setChildProfileId(childProfileId);
        response.setTotalActivities(activitySummaries.size());
        response.setTotalTopics(topicSummaries.size());
        response.setTotalAttempts(totalAttempts);
        response.setTotalCorrect(totalCorrect);
        response.setTotalIncorrect(totalIncorrect);
        response.setTotalTimeouts(totalTimeouts);
        response.setOverallSuccessRate(overallSuccessRate);
        response.setTotalAchievements(achievements.size());
        response.setTotalLearningPathsStarted(learningProgressList.size());
        response.setTotalLearningStepsCompleted(totalStepsCompleted);
        response.setLastActivityAt(lastActivity);

        return response;
    }

    public List<ActivityPerformanceResponse> getActivityPerformance(Long childProfileId) {
        var summaries = activitySummaryAdapter.findByChildProfileId(childProfileId);
        return summaries.stream()
                .map(this::toActivityPerformanceResponse)
                .toList();
    }

    public List<TopicPerformanceResponse> getTopicPerformance(Long childProfileId) {
        var summaries = topicSummaryAdapter.findByChildProfileId(childProfileId);
        return summaries.stream()
                .map(this::toTopicPerformanceResponse)
                .toList();
    }

    public DifficultyEvolutionResponse getDifficultyEvolution(Long childProfileId, Long activityId) {
        var response = new DifficultyEvolutionResponse();
        response.setChildProfileId(childProfileId);

        List<DifficultyEvolution> history;
        Long currentDifficultyLevelId = null;
        Integer attemptsSinceLastChange = null;

        if (activityId != null) {
            var summary = activitySummaryAdapter.findByChildProfileIdAndActivityId(childProfileId, activityId)
                    .orElse(null);
            if (summary != null) {
                currentDifficultyLevelId = summary.getCurrentDifficultyLevelId();
                attemptsSinceLastChange = summary.getAttemptsSinceLastDifficultyChange();
            }
            history = difficultyEvolutionAdapter.findByChildProfileIdAndActivityId(childProfileId, activityId);
        } else {
            history = difficultyEvolutionAdapter.findByChildProfileId(childProfileId);
            var latestOverall = history.stream()
                    .max(Comparator.comparing(DifficultyEvolution::getChangedAt))
                    .orElse(null);
            if (latestOverall != null) {
                currentDifficultyLevelId = latestOverall.getDifficultyLevelId();
            }
        }

        response.setActivityId(activityId);
        response.setCurrentDifficultyLevelId(currentDifficultyLevelId);
        response.setAttemptsSinceLastChange(attemptsSinceLastChange);
        response.setHistory(history.stream()
                .map(e -> new DifficultyChangeRecord(e.getDifficultyLevelId(), e.getDirection(), e.getChangedAt()))
                .toList());

        return response;
    }

    public ResponseTimeMetricsResponse getResponseTimeMetrics(Long childProfileId) {
        var summaries = activitySummaryAdapter.findByChildProfileId(childProfileId);

        var activityResponseTimes = summaries.stream()
                .filter(s -> s.getAverageResponseTimeMs() != null)
                .map(s -> new ActivityResponseTime(
                        s.getActivityId(),
                        s.getAverageResponseTimeMs(),
                        null,
                        null
                ))
                .toList();

        Integer overallAvg = null;
        Integer overallMin = null;
        Integer overallMax = null;

        if (!activityResponseTimes.isEmpty()) {
            overallAvg = (int) activityResponseTimes.stream()
                    .mapToInt(ActivityResponseTime::getAverageResponseTimeMs)
                    .average()
                    .orElse(0);

            List<Integer> nonNullAvg = activityResponseTimes.stream()
                    .map(ActivityResponseTime::getAverageResponseTimeMs)
                    .filter(v -> v != null)
                    .collect(Collectors.toList());

            if (!nonNullAvg.isEmpty()) {
                overallMin = nonNullAvg.stream().min(Integer::compare).orElse(null);
                overallMax = nonNullAvg.stream().max(Integer::compare).orElse(null);
            }
        }

        var response = new ResponseTimeMetricsResponse();
        response.setChildProfileId(childProfileId);
        response.setOverallAverageResponseTimeMs(overallAvg);
        response.setOverallMinResponseTimeMs(overallMin);
        response.setOverallMaxResponseTimeMs(overallMax);
        response.setByActivity(activityResponseTimes);

        return response;
    }

    public List<ChildAchievement> getChildAchievements(Long childProfileId, Long activityId) {
        var achievements = achievementAdapter.findByChildProfileId(childProfileId);
        if (activityId != null) {
            return achievements.stream()
                    .filter(a -> activityId.equals(a.getActivityId()))
                    .toList();
        }
        return achievements;
    }

    public List<ChildLearningProgress> getChildLearningProgress(Long childProfileId, Long learningPathId) {
        var allProgress = progressAdapter.findByChildProfileId(childProfileId);
        if (learningPathId != null) {
            return allProgress.stream()
                    .filter(p -> learningPathId.equals(p.getLearningPathId()))
                    .toList();
        }
        return allProgress;
    }

    private ActivityPerformanceResponse toActivityPerformanceResponse(ActivitySummary summary) {
        var response = new ActivityPerformanceResponse();
        response.setActivityId(summary.getActivityId());
        response.setTotalAttempts(summary.getTotalAttempts());
        response.setTotalCorrect(summary.getTotalCorrect());
        response.setTotalIncorrect(summary.getTotalIncorrect());
        response.setTotalTimeouts(summary.getTotalTimeouts());
        response.setSuccessRatePercent(summary.getSuccessRatePercent());
        response.setAverageResponseTimeMs(summary.getAverageResponseTimeMs());
        response.setCurrentDifficultyLevelId(summary.getCurrentDifficultyLevelId());
        response.setAttemptsSinceLastDifficultyChange(summary.getAttemptsSinceLastDifficultyChange());
        response.setLastAttemptAt(summary.getUpdatedAt());
        return response;
    }

    private TopicPerformanceResponse toTopicPerformanceResponse(TopicSummary summary) {
        var response = new TopicPerformanceResponse();
        response.setTopicId(summary.getTopicId());
        response.setTotalAttempts(summary.getTotalAttempts());
        response.setTotalCorrect(summary.getTotalCorrect());
        response.setTotalIncorrect(summary.getTotalIncorrect());
        response.setTotalTimeouts(summary.getTotalTimeouts());
        response.setSuccessRatePercent(summary.getSuccessRatePercent());
        response.setFailureRatePercent(summary.getFailureRatePercent());
        response.setAverageResponseTimeMs(summary.getAverageResponseTimeMs());
        response.setPerformanceBand(summary.getPerformanceBand());
        response.setLastAttemptAt(summary.getUpdatedAt());
        return response;
    }
}
