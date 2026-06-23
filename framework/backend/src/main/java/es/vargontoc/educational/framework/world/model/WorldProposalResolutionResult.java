package es.vargontoc.educational.framework.world.model;

import java.time.LocalDateTime;

public class WorldProposalResolutionResult {

    private Long trackingProposalId;
    private String outcome;
    private LocalDateTime resolvedAt;

    public WorldProposalResolutionResult() {
    }

    public WorldProposalResolutionResult(Long trackingProposalId, String outcome, LocalDateTime resolvedAt) {
        this.trackingProposalId = trackingProposalId;
        this.outcome = outcome;
        this.resolvedAt = resolvedAt;
    }

    public Long getTrackingProposalId() {
        return trackingProposalId;
    }

    public void setTrackingProposalId(Long trackingProposalId) {
        this.trackingProposalId = trackingProposalId;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}