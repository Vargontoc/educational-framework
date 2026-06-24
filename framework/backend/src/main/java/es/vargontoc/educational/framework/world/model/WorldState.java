package es.vargontoc.educational.framework.world.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class WorldState {

    private Long childSessionId;
    private Long childProfileId;
    private WorldRuntimeStatus status;
    private WorldDestination currentDestination;
    private List<WorldDiscoveryProposal> visibleDiscoveryElements = new ArrayList<>();
    private Long pendingProposalId;
    private WorldNarrativeCompletionStatus narrativeCompletionStatus = WorldNarrativeCompletionStatus.NO_PENDING;
    private LocalDateTime lastWorldActivityAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getChildSessionId() {
        return childSessionId;
    }

    public void setChildSessionId(Long childSessionId) {
        this.childSessionId = childSessionId;
    }

    public Long getChildProfileId() {
        return childProfileId;
    }

    public void setChildProfileId(Long childProfileId) {
        this.childProfileId = childProfileId;
    }

    public WorldRuntimeStatus getStatus() {
        return status;
    }

    public void setStatus(WorldRuntimeStatus status) {
        this.status = status;
    }

    public WorldDestination getCurrentDestination() {
        return currentDestination;
    }

    public void setCurrentDestination(WorldDestination currentDestination) {
        this.currentDestination = currentDestination;
    }

    public List<WorldDiscoveryProposal> getVisibleDiscoveryElements() {
        return visibleDiscoveryElements;
    }

    public void setVisibleDiscoveryElements(List<WorldDiscoveryProposal> visibleDiscoveryElements) {
        this.visibleDiscoveryElements = visibleDiscoveryElements;
    }

    public Long getPendingProposalId() {
        return pendingProposalId;
    }

    public void setPendingProposalId(Long pendingProposalId) {
        this.pendingProposalId = pendingProposalId;
    }

    public WorldNarrativeCompletionStatus getNarrativeCompletionStatus() {
        return narrativeCompletionStatus;
    }

    public void setNarrativeCompletionStatus(WorldNarrativeCompletionStatus narrativeCompletionStatus) {
        this.narrativeCompletionStatus = narrativeCompletionStatus;
    }

    public LocalDateTime getLastWorldActivityAt() {
        return lastWorldActivityAt;
    }

    public void setLastWorldActivityAt(LocalDateTime lastWorldActivityAt) {
        this.lastWorldActivityAt = lastWorldActivityAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}