package es.vargontoc.educational.framework.tracking.service;

import es.vargontoc.educational.framework.tracking.model.GameSessionFinalStatus;
import es.vargontoc.educational.framework.tracking.model.GameSessionSummary;
import es.vargontoc.educational.framework.tracking.model.GameSessionSummaryResult;
import es.vargontoc.educational.framework.tracking.ports.out.GameSessionSummaryRepository;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameSessionSummaryServiceTest {

    @Mock
    private GameSessionSummaryRepository repository;

    @InjectMocks
    private GameSessionSummaryService service;

    @Test
    void registerGameSessionSummary_withAllRequiredFields_persistsSummary() {
        var captor = ArgumentCaptor.forClass(GameSessionSummary.class);
        when(repository.save(any())).thenAnswer(inv -> {
            var summary = inv.getArgument(0, GameSessionSummary.class);
            summary.setId(1L);
            summary.setCreatedAt(LocalDateTime.now());
            return summary;
        });

        LocalDateTime startedAt = LocalDateTime.now().minusMinutes(10);
        LocalDateTime endedAt = LocalDateTime.now();

        var result = service.registerGameSessionSummary(
                10L, 20L, 30L, 40L, 41L, 150, 10, 7, 2, startedAt, endedAt, GameSessionFinalStatus.COMPLETED);

        assertNotNull(result);
        assertEquals(1L, result.summaryId());
        assertNotNull(result.createdAt());
        verify(repository).save(captor.capture());

        var saved = captor.getValue();
        assertEquals(10L, saved.getChildProfileId());
        assertEquals(20L, saved.getChildSessionId());
        assertEquals(30L, saved.getActivityId());
        assertEquals(40L, saved.getDifficultyLevelStartId());
        assertEquals(41L, saved.getDifficultyLevelEndId());
        assertEquals(150, saved.getScore());
        assertEquals(10, saved.getTotalAttempts());
        assertEquals(7, saved.getTotalCorrect());
        assertEquals(2, saved.getTotalTimeouts());
        assertEquals(GameSessionFinalStatus.COMPLETED, saved.getFinalStatus());
    }

    @Test
    void registerGameSessionSummary_withAbandonedStatus_persistsSummary() {
        var captor = ArgumentCaptor.forClass(GameSessionSummary.class);
        when(repository.save(any())).thenAnswer(inv -> {
            var summary = inv.getArgument(0, GameSessionSummary.class);
            summary.setId(2L);
            summary.setCreatedAt(LocalDateTime.now());
            return summary;
        });

        LocalDateTime startedAt = LocalDateTime.now().minusMinutes(5);
        LocalDateTime endedAt = LocalDateTime.now();

        var result = service.registerGameSessionSummary(
                10L, 20L, 30L, 40L, 40L, 50, 8, 5, 3, startedAt, endedAt, GameSessionFinalStatus.ABANDONED);

        assertNotNull(result);
        verify(repository).save(captor.capture());

        var saved = captor.getValue();
        assertEquals(GameSessionFinalStatus.ABANDONED, saved.getFinalStatus());
    }

    @Test
    void registerGameSessionSummary_missingChildProfileId_throwsValidationException() {
        LocalDateTime startedAt = LocalDateTime.now().minusMinutes(10);
        LocalDateTime endedAt = LocalDateTime.now();

        assertThrows(ValidationException.class, () ->
            service.registerGameSessionSummary(
                null, 20L, 30L, 40L, 41L, 150, 10, 7, 2, startedAt, endedAt, GameSessionFinalStatus.COMPLETED));
    }

    @Test
    void registerGameSessionSummary_missingChildSessionId_throwsValidationException() {
        LocalDateTime startedAt = LocalDateTime.now().minusMinutes(10);
        LocalDateTime endedAt = LocalDateTime.now();

        assertThrows(ValidationException.class, () ->
            service.registerGameSessionSummary(
                10L, null, 30L, 40L, 41L, 150, 10, 7, 2, startedAt, endedAt, GameSessionFinalStatus.COMPLETED));
    }

    @Test
    void registerGameSessionSummary_invalidFinalStatus_throwsValidationException() {
        LocalDateTime startedAt = LocalDateTime.now().minusMinutes(10);
        LocalDateTime endedAt = LocalDateTime.now();

        assertThrows(ValidationException.class, () ->
            service.registerGameSessionSummary(
                10L, 20L, 30L, 40L, 41L, 150, 10, 7, 2, startedAt, endedAt, null));
    }

    @Test
    void registerGameSessionSummary_endedAtBeforeStartedAt_throwsValidationException() {
        LocalDateTime startedAt = LocalDateTime.now();
        LocalDateTime endedAt = LocalDateTime.now().minusMinutes(10);

        assertThrows(ValidationException.class, () ->
            service.registerGameSessionSummary(
                10L, 20L, 30L, 40L, 41L, 150, 10, 7, 2, startedAt, endedAt, GameSessionFinalStatus.COMPLETED));
    }
}