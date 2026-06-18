package es.vargontoc.educational.framework.tracking.model;

import java.util.ArrayList;
import java.util.List;

public class TopicSelectionResult {

    private List<Long> selectedTopicIds;

    public TopicSelectionResult() {
        this.selectedTopicIds = new ArrayList<>();
    }

    public TopicSelectionResult(List<Long> selectedTopicIds) {
        this.selectedTopicIds = selectedTopicIds;
    }

    public List<Long> getSelectedTopicIds() {
        return selectedTopicIds;
    }

    public void setSelectedTopicIds(List<Long> selectedTopicIds) {
        this.selectedTopicIds = selectedTopicIds;
    }
}
