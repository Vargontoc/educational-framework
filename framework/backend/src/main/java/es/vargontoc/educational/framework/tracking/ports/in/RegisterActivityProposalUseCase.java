package es.vargontoc.educational.framework.tracking.ports.in;

import es.vargontoc.educational.framework.tracking.model.ActivityProposalLogResult;

public interface RegisterActivityProposalUseCase {

    ActivityProposalLogResult registerActivityProposal(
            Long childProfileId,
            Long childSessionId,
            Long activityId,
            Long topicId);
}
