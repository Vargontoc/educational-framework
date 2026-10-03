package es.vargontoc.educational.framework.content.ports.out;

import java.util.List;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.RecognitionElement;

public interface RecognitionElementRepository {

    List<RecognitionElement> findByTopicIdAndStatus(Long topicId, ContentStatus status);

    RecognitionElement save(RecognitionElement element);

    boolean existsById(Long id);

    List<RecognitionElement> findAllById(List<Long> ids);

    void addCuriosity(String code, String[] curiosities);

    byte[] getCuriosityAudio(String code);
}
