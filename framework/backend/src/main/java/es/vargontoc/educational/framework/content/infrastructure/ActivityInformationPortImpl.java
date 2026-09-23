package es.vargontoc.educational.framework.content.infrastructure;

import es.vargontoc.educational.framework.content.infrastructure.persistence.ActivityJpaRepository;
import es.vargontoc.educational.framework.content.infrastructure.persistence.ActivityTopicJpaRepository;
import es.vargontoc.educational.framework.content.infrastructure.persistence.TopicJpaRepository;
import es.vargontoc.educational.framework.content.model.RecognitionType;
import es.vargontoc.educational.framework.content.ports.out.DifficultyLevelRepository;
import es.vargontoc.educational.framework.tracking.ports.out.ActivityInformationPort;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class ActivityInformationPortImpl implements ActivityInformationPort {

    private final ActivityJpaRepository activityJpaRepository;
    private final ActivityTopicJpaRepository activityTopicJpaRepository;
    private final TopicJpaRepository topicJpaRepository;
    private final DifficultyLevelRepository difficultyLevelRepository;

    public ActivityInformationPortImpl(
            ActivityJpaRepository activityJpaRepository,
            ActivityTopicJpaRepository activityTopicJpaRepository,
            TopicJpaRepository topicJpaRepository,
            DifficultyLevelRepository difficultyLevelRepository) {
        this.activityJpaRepository = activityJpaRepository;
        this.activityTopicJpaRepository = activityTopicJpaRepository;
        this.topicJpaRepository = topicJpaRepository;
        this.difficultyLevelRepository = difficultyLevelRepository;
    }

    @Override
    public Map<Long, String> getGameEngineTypeByActivityIds(Set<Long> activityIds) {
        if (activityIds == null || activityIds.isEmpty()) {
            return Map.of();
        }

        return activityJpaRepository.findAllById(activityIds).stream()
            .filter(entity -> entity.getGameEngineType() != null)
            .collect(Collectors.toMap(
                entity -> entity.getId(),
                entity -> entity.getGameEngineType()
            ));
    }

    @Override
    public Map<Long, ActivityDetail> getDetailsByActivityIds(Set<Long> activityIds) {
        if (activityIds == null || activityIds.isEmpty()) {
            return Map.of();
        }

        var activities = activityJpaRepository.findAllById(activityIds);
        var result = new HashMap<Long, ActivityDetail>();

        for (var activity : activities) {
            var topicIds = activityTopicJpaRepository.findByActivityId(activity.getId()).stream()
                .map(at -> at.getTopicId())
                .toList();

            var resolution = resolveCategory(topicIds);

            result.put(activity.getId(), new ActivityDetail(
                activity.getName(),
                activity.getGameEngineType(),
                resolution.category(),
                resolution.subcategory()
            ));
        }

        return result;
    }

    @Override
    public Map<Long, String> getDifficultyCodesByIds(Set<Long> difficultyLevelIds) {
        if (difficultyLevelIds == null || difficultyLevelIds.isEmpty()) {
            return Map.of();
        }

        var result = new HashMap<Long, String>();
        for (var id : difficultyLevelIds) {
            difficultyLevelRepository.findById(id).ifPresent(dl ->
                result.put(id, dl.getDifficultyCode().name())
            );
        }
        return result;
    }

    private CategoryResolution resolveCategory(java.util.List<Long> topicIds) {
        if (topicIds == null || topicIds.isEmpty()) {
            return new CategoryResolution(null, null);
        }

        var topics = topicJpaRepository.findAllById(topicIds);
        var recognitionTypes = topics.stream()
            .map(t -> t.getRecognitionType())
            .filter(rt -> rt != null)
            .collect(Collectors.toSet());

        if (recognitionTypes.isEmpty()) {
            return new CategoryResolution(null, null);
        }

        if (recognitionTypes.contains(RecognitionType.COMPARISON)) {
            return new CategoryResolution("COMPARISON", null);
        }
        if (recognitionTypes.contains(RecognitionType.MEMORY)) {
            return new CategoryResolution("MEMORY", null);
        }

        // RECOGNITION: la subcategoria es el tipo (LETTER/NUMBER/SHAPE/COLOR/ANIMAL) de sus topics.
        // Si mezcla mas de uno, se queda con el primero (orden del enum) en vez de dejarlo ambiguo.
        String subcategory = recognitionTypes.stream()
            .sorted()
            .findFirst()
            .map(Enum::name)
            .orElse(null);
        return new CategoryResolution("RECOGNITION", subcategory);
    }

    private record CategoryResolution(String category, String subcategory) {
    }
}
