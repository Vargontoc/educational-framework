package es.vargontoc.educational.framework.tracking.ports.in;

import es.vargontoc.educational.framework.tracking.model.ActivityProposalOutcome;
import es.vargontoc.educational.framework.tracking.model.ActivityProposalLogResult;

public interface ResolveActivityProposalUseCase {

    ActivityProposalLogResult resolveActivityProposal(
            Long proposalId,
            ActivityProposalOutcome outcome);
}
