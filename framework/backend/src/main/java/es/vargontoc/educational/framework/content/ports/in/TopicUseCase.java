package es.vargontoc.educational.framework.content.ports.in;

import es.vargontoc.educational.framework.content.model.Biome;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.RecognitionType;
import es.vargontoc.educational.framework.content.model.Topic;

import java.util.List;

public interface TopicUseCase {

    Topic createTopic(String name, String description, Long categoryId, ContentStatus status, Integer minAge, Integer maxAge, List<String> compatibleVariants);

    Topic getTopic(Long id);

    List<Topic> listTopics();

    List<Topic> listTopicsByCategory(Long categoryId);

    List<Topic> listTopicsByRecognitionType(RecognitionType recognitionType);

    List<Topic> listTopicsByRecognitionTypeAndHabitat(RecognitionType recognitionType, Biome habitatTag);

    Topic updateTopic(Long id, String name, String description, Long categoryId, ContentStatus status, Integer minAge, Integer maxAge, List<String> compatibleVariants);
}
