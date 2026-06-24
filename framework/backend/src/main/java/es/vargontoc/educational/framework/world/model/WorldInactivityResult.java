package es.vargontoc.educational.framework.world.model;

import java.time.LocalDateTime;

public class WorldInactivityResult {

    private Long childSessionId;
    private WorldInactivityStatus status;
    private boolean closedWorldState;
    private boolean resolvedPendingProposalAsIgnored;
    private LocalDateTime occurredAt;

    public WorldInactivityResult() {
    }

    public WorldInactivityResult(Long childSessionId, WorldInactivityStatus status,
                                 boolean closedWorldState, boolean resolvedPendingProposalAsIgnored,
                                 LocalDateTime occurredAt) {
        this.childSessionId = childSessionId;
        this.status = status;
        this.closedWorldState = closedWorldState;
        this.resolvedPendingProposalAsIgnored = resolvedPendingProposalAsIgnored;
        this.occurredAt = occurredAt;
    }

    public Long getChildSessionId() {
        return childSessionId;
    }

    public void setChildSessionId(Long childSessionId) {
        this.childSessionId = childSessionId;
    }

    public WorldInactivityStatus getStatus() {
        return status;
    }

    public void setStatus(WorldInactivityStatus status) {
        this.status = status;
    }

    public boolean isClosedWorldState() {
        return closedWorldState;
    }

    public void setClosedWorldState(boolean closedWorldState) {
        this.closedWorldState = closedWorldState;
    }

    public boolean isResolvedPendingProposalAsIgnored() {
        return resolvedPendingProposalAsIgnored;
    }

    public void setResolvedPendingProposalAsIgnored(boolean resolvedPendingProposalAsIgnored) {
        this.resolvedPendingProposalAsIgnored = resolvedPendingProposalAsIgnored;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }
}