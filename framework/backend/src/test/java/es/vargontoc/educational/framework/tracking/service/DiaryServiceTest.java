package es.vargontoc.educational.framework.tracking.service;

import es.vargontoc.educational.framework.tracking.model.ActivitySummary;
import es.vargontoc.educational.framework.tracking.model.DiaryPeriod;
import es.vargontoc.educational.framework.tracking.model.GameSessionAbandonReason;
import es.vargontoc.educational.framework.tracking.model.GameSessionFinalStatus;
import es.vargontoc.educational.framework.tracking.model.GameSessionSummary;
import es.vargontoc.educational.framework.tracking.ports.out.ActivityInformationPort;
import es.vargontoc.educational.framework.tracking.ports.out.ActivitySummaryRepository;
import es.vargontoc.educational.framework.tracking.ports.out.GameSessionSummaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiaryServiceTest {

    @Mock
    private GameSessionSummaryRepository gameSessionSummaryRepository;

    @Mock
    private ActivitySummaryRepository activitySummaryRepository;

    @Mock
    private ActivityInformationPort activityInformationPort;

    private DiaryService diaryService;

    @BeforeEach
    void setUp() {
        diaryService = new DiaryService(gameSessionSummaryRepository, activitySummaryRepository, activityInformationPort);
    }

    @Test
    void getSummary_today_filtersByToday() {
        var session = createSession(1L, GameSessionFinalStatus.COMPLETED, todayAt(10, 0), todayAt(10, 30));
        when(gameSessionSummaryRepository.findByChildProfileIdAndStartedAtBetween(eq(1L), any(), any()))
            .thenReturn(List.of(session));

        var result = diaryService.getSummary(1L, DiaryPeriod.TODAY);

        assertEquals(30, result.getPlayedTimeMinutes());
        assertEquals(1, result.getUniqueActivitiesCompleted());
    }

    @Test
    void getSummary_week_filtersByWeek() {
        var session = createSession(1L, GameSessionFinalStatus.COMPLETED, mondayAt(9, 0), mondayAt(10, 0));
        when(gameSessionSummaryRepository.findByChildProfileIdAndStartedAtBetween(eq(1L), any(), any()))
            .thenReturn(List.of(session));

        var result = diaryService.getSummary(1L, DiaryPeriod.WEEK);

        assertEquals(60, result.getPlayedTimeMinutes());
        assertEquals(1, result.getUniqueActivitiesCompleted());
    }

    @Test
    void getSummary_month_filtersByMonth() {
        var session = createSession(1L, GameSessionFinalStatus.COMPLETED, firstOfMonthAt(8, 0), firstOfMonthAt(9, 0));
        when(gameSessionSummaryRepository.findByChildProfileIdAndStartedAtBetween(eq(1L), any(), any()))
            .thenReturn(List.of(session));

        var result = diaryService.getSummary(1L, DiaryPeriod.MONTH);

        assertEquals(60, result.getPlayedTimeMinutes());
        assertEquals(1, result.getUniqueActivitiesCompleted());
    }

    @Test
    void getSummary_all_noDateFilter() {
        var session = createSession(1L, GameSessionFinalStatus.COMPLETED,
            LocalDateTime.of(2020, 1, 1, 10, 0), LocalDateTime.of(2020, 1, 1, 11, 0));
        when(gameSessionSummaryRepository.findByChildProfileId(1L)).thenReturn(List.of(session));

        var result = diaryService.getSummary(1L, DiaryPeriod.ALL);

        assertEquals(60, result.getPlayedTimeMinutes());
        assertEquals(1, result.getUniqueActivitiesCompleted());
    }

    @Test
    void getSummary_uniqueActivities_notDuplicated() {
        var session1 = createSession(1L, GameSessionFinalStatus.COMPLETED, todayAt(10, 0), todayAt(10, 30));
        var session2 = createSession(1L, GameSessionFinalStatus.COMPLETED, todayAt(11, 0), todayAt(11, 30));
        var session3 = createSession(2L, GameSessionFinalStatus.COMPLETED, todayAt(12, 0), todayAt(12, 30));
        when(gameSessionSummaryRepository.findByChildProfileIdAndStartedAtBetween(eq(1L), any(), any()))
            .thenReturn(List.of(session1, session2, session3));

        var result = diaryService.getSummary(1L, DiaryPeriod.TODAY);

        assertEquals(2, result.getUniqueActivitiesCompleted());
    }

    @Test
    void getSummary_playedTime_calculatedCorrectly() {
        var session1 = createSession(1L, GameSessionFinalStatus.COMPLETED, todayAt(10, 0), todayAt(10, 15));
        var session2 = createSession(2L, GameSessionFinalStatus.COMPLETED, todayAt(11, 0), todayAt(11, 45));
        when(gameSessionSummaryRepository.findByChildProfileIdAndStartedAtBetween(eq(1L), any(), any()))
            .thenReturn(List.of(session1, session2));

        var result = diaryService.getSummary(1L, DiaryPeriod.TODAY);

        assertEquals(60, result.getPlayedTimeMinutes());
    }

    @Test
    void getSummary_emptyPeriod_returnsZeros() {
        when(gameSessionSummaryRepository.findByChildProfileIdAndStartedAtBetween(eq(1L), any(), any()))
            .thenReturn(List.of());

        var result = diaryService.getSummary(1L, DiaryPeriod.TODAY);

        assertEquals(0, result.getPlayedTimeMinutes());
        assertEquals(0, result.getUniqueActivitiesCompleted());
    }

    @Test
    void getActivities_currentDifficulty_fromActivitySummary() {
        var session = createSession(1L, GameSessionFinalStatus.COMPLETED, todayAt(10, 0), todayAt(10, 30));
        when(gameSessionSummaryRepository.findByChildProfileIdAndStartedAtBetween(eq(1L), any(), any()))
            .thenReturn(List.of(session));

        var activitySummary = new ActivitySummary();
        activitySummary.setActivityId(1L);
        activitySummary.setCurrentDifficultyLevelId(2L);
        when(activitySummaryRepository.findByChildProfileId(1L)).thenReturn(List.of(activitySummary));

        when(activityInformationPort.getDetailsByActivityIds(Set.of(1L))).thenReturn(Map.of(
            1L, new ActivityInformationPort.ActivityDetail("Letras", "recognition", "RECOGNITION", "LETTER")
        ));
        when(activityInformationPort.getDifficultyCodesByIds(Set.of(2L))).thenReturn(Map.of(2L, "MEDIUM"));

        var result = diaryService.getActivities(1L, DiaryPeriod.TODAY);

        assertEquals(1, result.size());
        assertEquals("MEDIUM", result.get(0).getCurrentDifficulty());
        assertEquals("LETTER", result.get(0).getSubcategory());
    }

    @Test
    void getActivities_fixedOrder_recognitionComparisonMemory() {
        var session1 = createSession(1L, GameSessionFinalStatus.COMPLETED, todayAt(10, 0), todayAt(10, 30));
        var session2 = createSession(2L, GameSessionFinalStatus.COMPLETED, todayAt(11, 0), todayAt(11, 30));
        var session3 = createSession(3L, GameSessionFinalStatus.COMPLETED, todayAt(12, 0), todayAt(12, 30));
        when(gameSessionSummaryRepository.findByChildProfileIdAndStartedAtBetween(eq(1L), any(), any()))
            .thenReturn(List.of(session3, session1, session2));

        when(activityInformationPort.getDetailsByActivityIds(Set.of(1L, 2L, 3L))).thenReturn(Map.of(
            1L, new ActivityInformationPort.ActivityDetail("Memory Game", "memory", "MEMORY", null),
            2L, new ActivityInformationPort.ActivityDetail("Compare Items", "comparison", "COMPARISON", null),
            3L, new ActivityInformationPort.ActivityDetail("Letters", "recognition", "RECOGNITION", "LETTER")
        ));
        when(activityInformationPort.getDifficultyCodesByIds(anySet())).thenReturn(Map.of());
        when(activitySummaryRepository.findByChildProfileId(1L)).thenReturn(List.of());

        var result = diaryService.getActivities(1L, DiaryPeriod.TODAY);

        assertEquals(3, result.size());
        assertEquals("RECOGNITION", result.get(0).getCategory());
        assertEquals("COMPARISON", result.get(1).getCategory());
        assertEquals("MEMORY", result.get(2).getCategory());
    }

    @Test
    void getActivities_abandonedSessions_notCounted() {
        var completed = createSession(1L, GameSessionFinalStatus.COMPLETED, todayAt(10, 0), todayAt(10, 30));
        var abandoned = createSession(2L, GameSessionFinalStatus.ABANDONED, todayAt(11, 0), todayAt(11, 30));
        abandoned.setAbandonReason(GameSessionAbandonReason.CLIENT_REQUESTED);
        when(gameSessionSummaryRepository.findByChildProfileIdAndStartedAtBetween(eq(1L), any(), any()))
            .thenReturn(List.of(completed, abandoned));

        when(activityInformationPort.getDetailsByActivityIds(Set.of(1L))).thenReturn(Map.of(
            1L, new ActivityInformationPort.ActivityDetail("Letras", "recognition", "RECOGNITION", "LETTER")
        ));
        when(activityInformationPort.getDifficultyCodesByIds(anySet())).thenReturn(Map.of());
        when(activitySummaryRepository.findByChildProfileId(1L)).thenReturn(List.of());

        var result = diaryService.getActivities(1L, DiaryPeriod.TODAY);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getActivityId());
    }

    @Test
    void getAbandonmentSignal_4plusAbandonments_returnsSignal() {
        var abandonments = List.of(
            createAbandonment(1L),
            createAbandonment(1L),
            createAbandonment(1L),
            createAbandonment(1L)
        );
        when(gameSessionSummaryRepository.findRecentInitialAbandonmentsByChildAndActivity(1L, 1L, 6))
            .thenReturn(abandonments);

        var result = diaryService.getAbandonmentSignal(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getActivityId());
        assertEquals(4, result.getAbandonmentCount());
    }

    @Test
    void getAbandonmentSignal_3orLessAbandonments_returnsNull() {
        var abandonments = List.of(
            createAbandonment(1L),
            createAbandonment(1L),
            createAbandonment(1L)
        );
        when(gameSessionSummaryRepository.findRecentInitialAbandonmentsByChildAndActivity(1L, 1L, 6))
            .thenReturn(abandonments);

        var result = diaryService.getAbandonmentSignal(1L, 1L);

        assertNull(result);
    }

    @Test
    void getAbandonmentSignal_emptyAbandonments_returnsNull() {
        when(gameSessionSummaryRepository.findRecentInitialAbandonmentsByChildAndActivity(1L, 1L, 6))
            .thenReturn(List.of());

        var result = diaryService.getAbandonmentSignal(1L, 1L);

        assertNull(result);
    }

    @Test
    void calculatePeriodStart_today() {
        var start = DiaryService.calculatePeriodStart(DiaryPeriod.TODAY);
        assertEquals(LocalDate.now().atStartOfDay(), start);
    }

    @Test
    void calculatePeriodStart_week() {
        var start = DiaryService.calculatePeriodStart(DiaryPeriod.WEEK);
        var expectedMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        assertEquals(expectedMonday.atStartOfDay(), start);
    }

    @Test
    void calculatePeriodStart_month() {
        var start = DiaryService.calculatePeriodStart(DiaryPeriod.MONTH);
        var expectedFirst = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
        assertEquals(expectedFirst.atStartOfDay(), start);
    }

    @Test
    void calculatePeriodStart_all() {
        var start = DiaryService.calculatePeriodStart(DiaryPeriod.ALL);
        assertEquals(LocalDateTime.of(LocalDate.MIN, LocalTime.MIN), start);
    }

    private GameSessionSummary createSession(Long activityId, GameSessionFinalStatus status,
                                              LocalDateTime startedAt, LocalDateTime endedAt) {
        var session = new GameSessionSummary();
        session.setActivityId(activityId);
        session.setFinalStatus(status);
        session.setStartedAt(startedAt);
        session.setEndedAt(endedAt);
        return session;
    }

    private GameSessionSummary createAbandonment(Long activityId) {
        var session = new GameSessionSummary();
        session.setActivityId(activityId);
        session.setFinalStatus(GameSessionFinalStatus.ABANDONED);
        session.setAbandonReason(GameSessionAbandonReason.CLIENT_REQUESTED);
        session.setStartedAt(LocalDateTime.now().minusHours(1));
        session.setEndedAt(LocalDateTime.now());
        return session;
    }

    private LocalDateTime todayAt(int hour, int minute) {
        return LocalDate.now().atTime(hour, minute);
    }

    private LocalDateTime mondayAt(int hour, int minute) {
        return LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atTime(hour, minute);
    }

    private LocalDateTime firstOfMonthAt(int hour, int minute) {
        return LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).atTime(hour, minute);
    }
}
