package es.vargontoc.educational.framework.world.service;

import es.vargontoc.educational.framework.tracking.model.ActivityProposalOutcome;
import es.vargontoc.educational.framework.tracking.model.ActivityProposalLogResult;
import es.vargontoc.educational.framework.tracking.ports.in.RegisterActivityProposalUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.ResolveActivityProposalUseCase;
import es.vargontoc.educational.framework.world.model.WorldActivityProposalResult;
import es.vargontoc.educational.framework.world.model.WorldProposalResolutionResult;
import es.vargontoc.educational.framework.world.model.WorldState;
import es.vargontoc.educational.framework.world.ports.in.WorldActivityProposalUseCase;
import es.vargontoc.educational.framework.world.ports.in.WorldProposalResolutionUseCase;
import es.vargontoc.educational.framework.world.ports.out.WorldStateRegistry;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class WorldProposalService implements WorldActivityProposalUseCase, WorldProposalResolutionUseCase {

    private final RegisterActivityProposalUseCase registerActivityProposalUseCase;
    private final ResolveActivityProposalUseCase resolveActivityProposalUseCase;
    private final WorldStateRegistry worldStateRegistry;

    public WorldProposalService(RegisterActivityProposalUseCase registerActivityProposalUseCase,
                               ResolveActivityProposalUseCase resolveActivityProposalUseCase,
                               WorldStateRegistry worldStateRegistry) {
        this.registerActivityProposalUseCase = registerActivityProposalUseCase;
        this.resolveActivityProposalUseCase = resolveActivityProposalUseCase;
        this.worldStateRegistry = worldStateRegistry;
    }

    @Override
    public WorldActivityProposalResult proposeActivity(Long childSessionId, Long childProfileId,
                                                      Long activityId, Long topicId) {
        resolvePendingProposalIfExists(childSessionId, ActivityProposalOutcome.IGNORED);

        ActivityProposalLogResult trackingResult = registerActivityProposalUseCase.registerActivityProposal(
            childProfileId, childSessionId, activityId, topicId);

        String proposalRuntimeId = UUID.randomUUID().toString();

        updatePendingProposalId(childSessionId, trackingResult.proposalId());

        return new WorldActivityProposalResult(
            childSessionId,
            proposalRuntimeId,
            trackingResult.proposalId(),
            activityId,
            topicId,
            WorldActivityProposalResult.Status.PENDING
        );
    }

    @Override
    public WorldProposalResolutionResult resolveProposal(Long childSessionId, ActivityProposalOutcome outcome) {
        Optional<Long> pendingProposalId = worldStateRegistry.getPendingProposalId(childSessionId);

        if (pendingProposalId.isEmpty()) {
            return new WorldProposalResolutionResult(null, outcome.name(), LocalDateTime.now());
        }

        Long proposalId = pendingProposalId.get();
        ActivityProposalLogResult trackingResult = resolveActivityProposalUseCase.resolveActivityProposal(
            proposalId, outcome);

        clearPendingProposalId(childSessionId);

        return new WorldProposalResolutionResult(
            proposalId,
            outcome.name(),
            trackingResult.createdAt()
        );
    }

    public void resolveAndCloseWorld(Long childSessionId) {
        Optional<Long> pendingProposalId = worldStateRegistry.getPendingProposalId(childSessionId);

        if (pendingProposalId.isPresent()) {
            resolveActivityProposalUseCase.resolveActivityProposal(
                pendingProposalId.get(), ActivityProposalOutcome.IGNORED);
        }

        worldStateRegistry.removeByChildSessionId(childSessionId);
    }

    private void resolvePendingProposalIfExists(Long childSessionId, ActivityProposalOutcome outcome) {
        Optional<Long> pendingProposalId = worldStateRegistry.getPendingProposalId(childSessionId);

        if (pendingProposalId.isPresent()) {
            resolveActivityProposalUseCase.resolveActivityProposal(pendingProposalId.get(), outcome);
        }
    }

    private void updatePendingProposalId(Long childSessionId, Long proposalId) {
        Optional<WorldState> stateOpt = worldStateRegistry.findByChildSessionId(childSessionId);

        if (stateOpt.isPresent()) {
            WorldState state = stateOpt.get();
            state.setPendingProposalId(proposalId);
            worldStateRegistry.save(state);
        }
    }

    private void clearPendingProposalId(Long childSessionId) {
        Optional<WorldState> stateOpt = worldStateRegistry.findByChildSessionId(childSessionId);

        if (stateOpt.isPresent()) {
            WorldState state = stateOpt.get();
            state.setPendingProposalId(null);
            worldStateRegistry.save(state);
        }
    }
}