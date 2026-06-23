package es.vargontoc.educational.framework.world.ports.in;

import es.vargontoc.educational.framework.tracking.model.ActivityProposalOutcome;
import es.vargontoc.educational.framework.world.model.WorldProposalResolutionResult;

public interface WorldProposalResolutionUseCase {

    WorldProposalResolutionResult resolveProposal(Long childSessionId, ActivityProposalOutcome outcome);
}