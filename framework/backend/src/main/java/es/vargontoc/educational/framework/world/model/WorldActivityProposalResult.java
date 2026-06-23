package es.vargontoc.educational.framework.world.model;

public class WorldActivityProposalResult {

    public enum Status {
        PENDING,
        STARTED,
        IGNORED
    }

    private Long childSessionId;
    private String proposalRuntimeId;
    private Long trackingProposalId;
    private Long activityId;
    private Long topicId;
    private Status status;

    public WorldActivityProposalResult() {
    }

    public WorldActivityProposalResult(Long childSessionId, String proposalRuntimeId, Long trackingProposalId,
                                     Long activityId, Long topicId, Status status) {
        this.childSessionId = childSessionId;
        this.proposalRuntimeId = proposalRuntimeId;
        this.trackingProposalId = trackingProposalId;
        this.activityId = activityId;
        this.topicId = topicId;
        this.status = status;
    }

    public Long getChildSessionId() {
        return childSessionId;
    }

    public void setChildSessionId(Long childSessionId) {
        this.childSessionId = childSessionId;
    }

    public String getProposalRuntimeId() {
        return proposalRuntimeId;
    }

    public void setProposalRuntimeId(String proposalRuntimeId) {
        this.proposalRuntimeId = proposalRuntimeId;
    }

    public Long getTrackingProposalId() {
        return trackingProposalId;
    }

    public void setTrackingProposalId(Long trackingProposalId) {
        this.trackingProposalId = trackingProposalId;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}