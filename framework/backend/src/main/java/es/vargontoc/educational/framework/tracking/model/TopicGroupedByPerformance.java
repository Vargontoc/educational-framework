package es.vargontoc.educational.framework.tracking.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TopicGroupedByPerformance {

    private Map<TopicPerformanceBand, List<Long>> topicsByBand;

    public TopicGroupedByPerformance() {
        this.topicsByBand = new HashMap<>();
        for (var band : TopicPerformanceBand.values()) {
            topicsByBand.put(band, new ArrayList<>());
        }
    }

    public void addTopic(TopicPerformanceBand band, Long topicId) {
        topicsByBand.get(band).add(topicId);
    }

    public List<Long> getTopics(TopicPerformanceBand band) {
        return topicsByBand.getOrDefault(band, List.of());
    }

    public Map<TopicPerformanceBand, List<Long>> getAll() {
        return topicsByBand;
    }
}
