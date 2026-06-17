package es.vargontoc.educational.framework.tracking.service;

import es.vargontoc.educational.framework.tracking.model.ActivityAttempt;
import es.vargontoc.educational.framework.tracking.model.AttemptRegistrationResult;
import es.vargontoc.educational.framework.tracking.model.AttemptResult;
import es.vargontoc.educational.framework.tracking.ports.out.ActivityAttemptRepository;
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
class ActivityAttemptServiceTest {

    @Mock
    private ActivityAttemptRepository repository;

    @Mock
    private SummaryUpdateService summaryUpdateService;

    @InjectMocks
    private ActivityAttemptService service;

    @Test
    void register_withAllRequiredFields_persistsAttempt() {
        var captor = ArgumentCaptor.forClass(ActivityAttempt.class);
        when(repository.save(any())).thenAnswer(inv -> {
            var attempt = inv.getArgument(0, ActivityAttempt.class);
            attempt.setId(1L);
            attempt.setCreatedAt(LocalDateTime.now());
            return attempt;
        });

        var result = service.register(10L, 20L, 30L, 40L, 50L, AttemptResult.CORRECT, 5000, null);

        assertNotNull(result);
        assertEquals(1L, result.attemptId());
        assertNotNull(result.createdAt());
        verify(repository).save(captor.capture());
        verify(summaryUpdateService).updateSummaries(any(ActivityAttempt.class));

        var saved = captor.getValue();
        assertEquals(10L, saved.getChildProfileId());
        assertEquals(20L, saved.getActivityId());
        assertEquals(30L, saved.getChildSessionId());
        assertEquals(40L, saved.getTopicId());
        assertEquals(50L, saved.getDifficultyLevelId());
        assertEquals(AttemptResult.CORRECT, saved.getResult());
    }

    @Test
    void register_withOptionalFields_persistsAttemptContextAndResponseTime() {
        var captor = ArgumentCaptor.forClass(ActivityAttempt.class);
        when(repository.save(any())).thenAnswer(inv -> {
            var attempt = inv.getArgument(0, ActivityAttempt.class);
            attempt.setId(2L);
            attempt.setCreatedAt(LocalDateTime.now());
            return attempt;
        });

        String context = "{\"engine\":\"memory\",\"level\":3}";
        var result = service.register(10L, 20L, 30L, 40L, 50L, AttemptResult.INCORRECT, 3000, context);

        assertNotNull(result);
        verify(repository).save(captor.capture());

        var saved = captor.getValue();
        assertEquals(3000, saved.getResponseTimeMs());
        assertEquals(context, saved.getAttemptContext());
    }

    @Test
    void register_missingChildProfileId_throwsValidationException() {
        assertThrows(ValidationException.class, () ->
            service.register(null, 20L, 30L, 40L, 50L, AttemptResult.CORRECT, null, null));
    }

    @Test
    void register_missingActivityId_throwsValidationException() {
        assertThrows(ValidationException.class, () ->
            service.register(10L, null, 30L, 40L, 50L, AttemptResult.CORRECT, null, null));
    }

    @Test
    void register_missingResult_throwsValidationException() {
        assertThrows(ValidationException.class, () ->
            service.register(10L, 20L, 30L, 40L, 50L, null, null, null));
    }
}
