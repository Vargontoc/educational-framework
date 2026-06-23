package es.vargontoc.educational.framework.tracking.service;

import es.vargontoc.educational.framework.tracking.model.ActivityProposalLog;
import es.vargontoc.educational.framework.tracking.model.ActivityProposalOutcome;
import es.vargontoc.educational.framework.tracking.ports.out.ActivityProposalLogRepository;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityProposalLogServiceTest {

    @Mock
    private ActivityProposalLogRepository repository;

    @InjectMocks
    private ActivityProposalLogService service;

    @Test
    void registerActivityProposal_withAllRequiredFields_createsPendingLog() {
        var captor = ArgumentCaptor.forClass(ActivityProposalLog.class);
        when(repository.save(any())).thenAnswer(inv -> {
            var proposal = inv.getArgument(0, ActivityProposalLog.class);
            proposal.setId(1L);
            proposal.setCreatedAt(LocalDateTime.now());
            return proposal;
        });

        var result = service.registerActivityProposal(10L, 20L, 30L, 5L);

        assertNotNull(result);
        assertEquals(1L, result.proposalId());
        assertNotNull(result.createdAt());
        verify(repository).save(captor.capture());

        var saved = captor.getValue();
        assertEquals(10L, saved.getChildProfileId());
        assertEquals(20L, saved.getChildSessionId());
        assertEquals(30L, saved.getActivityId());
        assertEquals(5L, saved.getTopicId());
        assertNotNull(saved.getProposedAt());
        assertNull(saved.getOutcome());
        assertNull(saved.getResolvedAt());
    }

    @Test
    void registerActivityProposal_withoutTopicId_createsPendingLog() {
        var captor = ArgumentCaptor.forClass(ActivityProposalLog.class);
        when(repository.save(any())).thenAnswer(inv -> {
            var proposal = inv.getArgument(0, ActivityProposalLog.class);
            proposal.setId(2L);
            proposal.setCreatedAt(LocalDateTime.now());
            return proposal;
        });

        var result = service.registerActivityProposal(10L, 20L, 30L, null);

        assertNotNull(result);
        verify(repository).save(captor.capture());

        var saved = captor.getValue();
        assertNull(saved.getTopicId());
    }

    @Test
    void registerActivityProposal_missingChildProfileId_throwsValidationException() {
        assertThrows(ValidationException.class, () ->
            service.registerActivityProposal(null, 20L, 30L, 5L));
    }

    @Test
    void registerActivityProposal_missingChildSessionId_throwsValidationException() {
        assertThrows(ValidationException.class, () ->
            service.registerActivityProposal(10L, null, 30L, 5L));
    }

    @Test
    void registerActivityProposal_missingActivityId_throwsValidationException() {
        assertThrows(ValidationException.class, () ->
            service.registerActivityProposal(10L, 20L, null, 5L));
    }

    @Test
    void resolveActivityProposal_asStarted_updatesProposal() {
        var captor = ArgumentCaptor.forClass(ActivityProposalLog.class);
        var existingProposal = new ActivityProposalLog();
        existingProposal.setId(1L);
        existingProposal.setChildProfileId(10L);
        existingProposal.setChildSessionId(20L);
        existingProposal.setActivityId(30L);
        existingProposal.setTopicId(5L);
        existingProposal.setProposedAt(LocalDateTime.now().minusMinutes(5));

        when(repository.findById(1L)).thenReturn(Optional.of(existingProposal));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0, ActivityProposalLog.class));

        var result = service.resolveActivityProposal(1L, ActivityProposalOutcome.STARTED);

        assertNotNull(result);
        assertEquals(1L, result.proposalId());
        verify(repository).save(captor.capture());

        var saved = captor.getValue();
        assertEquals(ActivityProposalOutcome.STARTED, saved.getOutcome());
        assertNotNull(saved.getResolvedAt());
    }

    @Test
    void resolveActivityProposal_asIgnored_updatesProposal() {
        var captor = ArgumentCaptor.forClass(ActivityProposalLog.class);
        var existingProposal = new ActivityProposalLog();
        existingProposal.setId(2L);
        existingProposal.setChildProfileId(10L);
        existingProposal.setChildSessionId(20L);
        existingProposal.setActivityId(30L);
        existingProposal.setProposedAt(LocalDateTime.now().minusMinutes(10));

        when(repository.findById(2L)).thenReturn(Optional.of(existingProposal));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0, ActivityProposalLog.class));

        var result = service.resolveActivityProposal(2L, ActivityProposalOutcome.IGNORED);

        assertNotNull(result);
        verify(repository).save(captor.capture());

        var saved = captor.getValue();
        assertEquals(ActivityProposalOutcome.IGNORED, saved.getOutcome());
        assertNotNull(saved.getResolvedAt());
    }

    @Test
    void resolveActivityProposal_doubleResolution_throwsValidationException() {
        var existingProposal = new ActivityProposalLog();
        existingProposal.setId(1L);
        existingProposal.setChildProfileId(10L);
        existingProposal.setChildSessionId(20L);
        existingProposal.setActivityId(30L);
        existingProposal.setProposedAt(LocalDateTime.now().minusMinutes(5));
        existingProposal.setOutcome(ActivityProposalOutcome.IGNORED);
        existingProposal.setResolvedAt(LocalDateTime.now().minusMinutes(1));

        when(repository.findById(1L)).thenReturn(Optional.of(existingProposal));

        assertThrows(ValidationException.class, () ->
            service.resolveActivityProposal(1L, ActivityProposalOutcome.STARTED));
    }

    @Test
    void resolveActivityProposal_proposalNotFound_throwsValidationException() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () ->
            service.resolveActivityProposal(999L, ActivityProposalOutcome.STARTED));
    }

    @Test
    void resolveActivityProposal_missingOutcome_throwsValidationException() {
        var existingProposal = new ActivityProposalLog();
        existingProposal.setId(1L);
        existingProposal.setChildProfileId(10L);
        existingProposal.setChildSessionId(20L);
        existingProposal.setActivityId(30L);
        existingProposal.setProposedAt(LocalDateTime.now().minusMinutes(5));

        when(repository.findById(1L)).thenReturn(Optional.of(existingProposal));

        assertThrows(ValidationException.class, () ->
            service.resolveActivityProposal(1L, null));
    }
}
