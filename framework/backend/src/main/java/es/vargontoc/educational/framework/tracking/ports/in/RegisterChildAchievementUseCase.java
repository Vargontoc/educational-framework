package es.vargontoc.educational.framework.tracking.ports.in;

public interface RegisterChildAchievementUseCase {

    void registerAchievement(Long childProfileId, String achievementCode, Long activityId, Long topicId);
}
