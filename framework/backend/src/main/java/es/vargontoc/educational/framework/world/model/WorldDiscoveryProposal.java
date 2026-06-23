package es.vargontoc.educational.framework.world.model;

public class WorldDiscoveryProposal {

    private String proposalRuntimeId;
    private Long discoveryElementId;
    private String discoveryElementCode;
    private String displayName;
    private String elementType;
    private Long activityId;
    private Long topicId;
    private String visualAssetKey;
    private String interactionCueType;
    private Long trackingProposalId;

    public String getProposalRuntimeId() {
        return proposalRuntimeId;
    }

    public void setProposalRuntimeId(String proposalRuntimeId) {
        this.proposalRuntimeId = proposalRuntimeId;
    }

    public Long getDiscoveryElementId() {
        return discoveryElementId;
    }

    public void setDiscoveryElementId(Long discoveryElementId) {
        this.discoveryElementId = discoveryElementId;
    }

    public String getDiscoveryElementCode() {
        return discoveryElementCode;
    }

    public void setDiscoveryElementCode(String discoveryElementCode) {
        this.discoveryElementCode = discoveryElementCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getElementType() {
        return elementType;
    }

    public void setElementType(String elementType) {
        this.elementType = elementType;
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

    public String getVisualAssetKey() {
        return visualAssetKey;
    }

    public void setVisualAssetKey(String visualAssetKey) {
        this.visualAssetKey = visualAssetKey;
    }

    public String getInteractionCueType() {
        return interactionCueType;
    }

    public void setInteractionCueType(String interactionCueType) {
        this.interactionCueType = interactionCueType;
    }

    public Long getTrackingProposalId() {
        return trackingProposalId;
    }

    public void setTrackingProposalId(Long trackingProposalId) {
        this.trackingProposalId = trackingProposalId;
    }
}
