package es.vargontoc.educational.framework.content.ports.out;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.RecognitionElement;

import java.util.List;

public interface RecognitionElementRepository {

    List<RecognitionElement> findByTopicIdAndStatus(Long topicId, ContentStatus status);

    RecognitionElement save(RecognitionElement element);
}
