package es.vargontoc.educational.framework.content.ports.in;

import es.vargontoc.educational.framework.content.model.RecognitionElement;

import java.util.List;

public interface RecognitionElementUseCase {

    List<RecognitionElement> listActiveElementsByTopicId(Long topicId);
}
