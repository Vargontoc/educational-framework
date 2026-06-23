package es.vargontoc.educational.framework.world.ports.in;

import es.vargontoc.educational.framework.world.model.WorldActivityProposalResult;

public interface WorldActivityProposalUseCase {

    WorldActivityProposalResult proposeActivity(Long childSessionId, Long childProfileId,
                                              Long activityId, Long topicId);
}