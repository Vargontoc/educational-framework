package es.vargontoc.educational.framework.world.model;

public class SelectedWorldActivity {

    public enum Source {
        TOPIC_RECOMMENDATION,
        FALLBACK
    }

    private Long activityId;
    private String engineType;
    private Long topicId;
    private Source source;

    public SelectedWorldActivity() {
    }

    public SelectedWorldActivity(Long activityId, String engineType, Long topicId, Source source) {
        this.activityId = activityId;
        this.engineType = engineType;
        this.topicId = topicId;
        this.source = source;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public String getEngineType() {
        return engineType;
    }

    public void setEngineType(String engineType) {
        this.engineType = engineType;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }
}