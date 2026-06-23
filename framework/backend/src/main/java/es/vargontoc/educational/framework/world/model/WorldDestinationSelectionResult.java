package es.vargontoc.educational.framework.world.model;

public class WorldDestinationSelectionResult {

    private Long childSessionId;
    private Long topicId;
    private WorldDestination destination;
    private SelectedWorldActivity selectedActivity;
    private boolean priorityAdjustmentApplied;

    public WorldDestinationSelectionResult() {
    }

    public WorldDestinationSelectionResult(Long childSessionId, Long topicId, WorldDestination destination,
                                          SelectedWorldActivity selectedActivity, boolean priorityAdjustmentApplied) {
        this.childSessionId = childSessionId;
        this.topicId = topicId;
        this.destination = destination;
        this.selectedActivity = selectedActivity;
        this.priorityAdjustmentApplied = priorityAdjustmentApplied;
    }

    public Long getChildSessionId() {
        return childSessionId;
    }

    public void setChildSessionId(Long childSessionId) {
        this.childSessionId = childSessionId;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public WorldDestination getDestination() {
        return destination;
    }

    public void setDestination(WorldDestination destination) {
        this.destination = destination;
    }

    public SelectedWorldActivity getSelectedActivity() {
        return selectedActivity;
    }

    public void setSelectedActivity(SelectedWorldActivity selectedActivity) {
        this.selectedActivity = selectedActivity;
    }

    public boolean isPriorityAdjustmentApplied() {
        return priorityAdjustmentApplied;
    }

    public void setPriorityAdjustmentApplied(boolean priorityAdjustmentApplied) {
        this.priorityAdjustmentApplied = priorityAdjustmentApplied;
    }
}