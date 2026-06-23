package es.vargontoc.educational.framework.tracking.service;

import es.vargontoc.educational.framework.shared.exception.ValidationException;
import es.vargontoc.educational.framework.tracking.model.ActivityProposalLog;
import es.vargontoc.educational.framework.tracking.model.ActivityProposalLogResult;
import es.vargontoc.educational.framework.tracking.model.ActivityProposalOutcome;
import es.vargontoc.educational.framework.tracking.ports.in.RegisterActivityProposalUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.ResolveActivityProposalUseCase;
import es.vargontoc.educational.framework.tracking.ports.out.ActivityProposalLogRepository;
import es.vargontoc.educational.framework.tracking.validation.ActivityProposalLogValidator;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Transactional
public class ActivityProposalLogService implements RegisterActivityProposalUseCase, ResolveActivityProposalUseCase {

    private final ActivityProposalLogRepository repository;
    private final ActivityProposalLogValidator validator;

    public ActivityProposalLogService(ActivityProposalLogRepository repository) {
        this.repository = repository;
        this.validator = new ActivityProposalLogValidator();
    }

    @Override
    public ActivityProposalLogResult registerActivityProposal(
            Long childProfileId,
            Long childSessionId,
            Long activityId,
            Long topicId) {

        validator.validateForRegistration(childProfileId, childSessionId, activityId);

        var proposal = new ActivityProposalLog();
        proposal.setChildProfileId(childProfileId);
        proposal.setChildSessionId(childSessionId);
        proposal.setActivityId(activityId);
        proposal.setTopicId(topicId);
        proposal.setProposedAt(LocalDateTime.now());

        var saved = repository.save(proposal);

        return new ActivityProposalLogResult(saved.getId(), saved.getCreatedAt());
    }

    @Override
    public ActivityProposalLogResult resolveActivityProposal(
            Long proposalId,
            ActivityProposalOutcome outcome) {

        var existingProposal = repository.findById(proposalId)
            .orElseThrow(() -> new ValidationException("proposal not found"));

        validator.validateForResolution(existingProposal, outcome);

        existingProposal.setOutcome(outcome);
        existingProposal.setResolvedAt(LocalDateTime.now());

        var saved = repository.save(existingProposal);

        return new ActivityProposalLogResult(saved.getId(), saved.getCreatedAt());
    }
}
