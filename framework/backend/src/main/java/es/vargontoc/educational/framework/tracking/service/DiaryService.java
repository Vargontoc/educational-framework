package es.vargontoc.educational.framework.tracking.service;

import es.vargontoc.educational.framework.tracking.infrastructure.dto.AbandonmentSignalResponse;
import es.vargontoc.educational.framework.tracking.infrastructure.dto.DiaryActivityResponse;
import es.vargontoc.educational.framework.tracking.infrastructure.dto.DiarySummaryResponse;
import es.vargontoc.educational.framework.tracking.model.DiaryPeriod;
import es.vargontoc.educational.framework.tracking.model.GameSessionAbandonReason;
import es.vargontoc.educational.framework.tracking.model.GameSessionFinalStatus;
import es.vargontoc.educational.framework.tracking.ports.out.ActivityInformationPort;
import es.vargontoc.educational.framework.tracking.ports.out.ActivitySummaryRepository;
import es.vargontoc.educational.framework.tracking.ports.out.GameSessionSummaryRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
public class DiaryService {

    private static final int ABANDONMENT_THRESHOLD = 4;
    private static final int ABANDONMENT_WINDOW = 6;

    private static final List<String> CATEGORY_ORDER = List.of("RECOGNITION", "COMPARISON", "MEMORY");

    private final GameSessionSummaryRepository gameSessionSummaryRepository;
    private final ActivitySummaryRepository activitySummaryRepository;
    private final ActivityInformationPort activityInformationPort;

    public DiaryService(
            GameSessionSummaryRepository gameSessionSummaryRepository,
            ActivitySummaryRepository activitySummaryRepository,
            ActivityInformationPort activityInformationPort) {
        this.gameSessionSummaryRepository = gameSessionSummaryRepository;
        this.activitySummaryRepository = activitySummaryRepository;
        this.activityInformationPort = activityInformationPort;
    }

    public DiarySummaryResponse getSummary(Long childProfileId, DiaryPeriod period) {
        var sessions = fetchSessionsInPeriod(childProfileId, period);

        long totalDurationMs = sessions.stream()
            .filter(s -> s.getStartedAt() != null && s.getEndedAt() != null)
            .mapToLong(s -> java.time.Duration.between(s.getStartedAt(), s.getEndedAt()).toMillis())
            .sum();
        int playedTimeMinutes = (int) (totalDurationMs / 60000);

        long uniqueActivities = sessions.stream()
            .filter(s -> s.getFinalStatus() == GameSessionFinalStatus.COMPLETED)
            .map(s -> s.getActivityId())
            .distinct()
            .count();

        return new DiarySummaryResponse(playedTimeMinutes, (int) uniqueActivities);
    }

    public List<DiaryActivityResponse> getActivities(Long childProfileId, DiaryPeriod period) {
        var sessions = fetchSessionsInPeriod(childProfileId, period);

        Set<Long> completedActivityIds = sessions.stream()
            .filter(s -> s.getFinalStatus() == GameSessionFinalStatus.COMPLETED)
            .map(s -> s.getActivityId())
            .collect(Collectors.toSet());

        if (completedActivityIds.isEmpty()) {
            return List.of();
        }

        var activityDetails = activityInformationPort.getDetailsByActivityIds(completedActivityIds);

        var activitySummaries = activitySummaryRepository.findByChildProfileId(childProfileId);
        Map<Long, Long> currentDifficultyLevelIdByActivityId = activitySummaries.stream()
            .filter(s -> s.getCurrentDifficultyLevelId() != null)
            .collect(Collectors.toMap(
                s -> s.getActivityId(),
                s -> s.getCurrentDifficultyLevelId(),
                (a, b) -> a
            ));

        Set<Long> difficultyLevelIds = currentDifficultyLevelIdByActivityId.values().stream()
            .collect(Collectors.toSet());
        Map<Long, String> difficultyCodesById = activityInformationPort.getDifficultyCodesByIds(difficultyLevelIds);

        Map<String, List<DiaryActivityResponse>> groupedByCategory = new LinkedHashMap<>();
        for (String cat : CATEGORY_ORDER) {
            groupedByCategory.put(cat, new ArrayList<>());
        }

        for (Long activityId : completedActivityIds) {
            var detail = activityDetails.get(activityId);
            if (detail == null) {
                continue;
            }

            String category = detail.category() != null ? detail.category() : "RECOGNITION";
            Long diffLevelId = currentDifficultyLevelIdByActivityId.get(activityId);
            String difficulty = diffLevelId != null ? difficultyCodesById.get(diffLevelId) : null;
            if (difficulty == null) {
                difficulty = "EASY";
            }

            var response = new DiaryActivityResponse(
                activityId,
                detail.name(),
                category,
                detail.subcategory(),
                detail.gameEngineType(),
                difficulty
            );

            List<DiaryActivityResponse> categoryList = groupedByCategory.computeIfAbsent(category, k -> new ArrayList<>());
            categoryList.add(response);
        }

        List<DiaryActivityResponse> result = new ArrayList<>();
        for (String cat : CATEGORY_ORDER) {
            var list = groupedByCategory.get(cat);
            if (list != null) {
                list.sort(Comparator.comparing(DiaryActivityResponse::getName, Comparator.nullsLast(Comparator.naturalOrder())));
                result.addAll(list);
            }
        }
        for (var entry : groupedByCategory.entrySet()) {
            if (!CATEGORY_ORDER.contains(entry.getKey())) {
                entry.getValue().sort(Comparator.comparing(DiaryActivityResponse::getName, Comparator.nullsLast(Comparator.naturalOrder())));
                result.addAll(entry.getValue());
            }
        }

        return result;
    }

    public AbandonmentSignalResponse getAbandonmentSignal(Long childProfileId, Long activityId) {
        var recentAbandonments = gameSessionSummaryRepository
            .findRecentInitialAbandonmentsByChildAndActivity(childProfileId, activityId, ABANDONMENT_WINDOW);

        int abandonmentCount = (int) recentAbandonments.stream()
            .filter(s -> s.getFinalStatus() == GameSessionFinalStatus.ABANDONED
                && s.getAbandonReason() == GameSessionAbandonReason.CLIENT_REQUESTED)
            .count();

        if (abandonmentCount < ABANDONMENT_THRESHOLD) {
            return null;
        }

        return new AbandonmentSignalResponse(activityId, abandonmentCount);
    }

    private List<es.vargontoc.educational.framework.tracking.model.GameSessionSummary> fetchSessionsInPeriod(
            Long childProfileId, DiaryPeriod period) {
        if (period == DiaryPeriod.ALL) {
            return gameSessionSummaryRepository.findByChildProfileId(childProfileId);
        }

        LocalDateTime start = calculatePeriodStart(period);
        LocalDateTime end = LocalDateTime.now();
        return gameSessionSummaryRepository.findByChildProfileIdAndStartedAtBetween(childProfileId, start, end);
    }

    static LocalDateTime calculatePeriodStart(DiaryPeriod period) {
        LocalDate today = LocalDate.now();
        return switch (period) {
            case TODAY -> today.atStartOfDay();
            case WEEK -> today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
            case MONTH -> today.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
            case ALL -> LocalDateTime.of(LocalDate.MIN, LocalTime.MIN);
        };
    }
}
