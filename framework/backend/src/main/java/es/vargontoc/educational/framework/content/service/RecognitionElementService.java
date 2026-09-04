package es.vargontoc.educational.framework.content.service;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.RecognitionElement;
import es.vargontoc.educational.framework.content.ports.in.RecognitionElementUseCase;
import es.vargontoc.educational.framework.content.ports.out.RecognitionElementRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public class RecognitionElementService implements RecognitionElementUseCase {

    private final RecognitionElementRepository recognitionElementRepository;

    public RecognitionElementService(RecognitionElementRepository recognitionElementRepository) {
        this.recognitionElementRepository = recognitionElementRepository;
    }

    @Override
    public List<RecognitionElement> listActiveElementsByTopicId(Long topicId) {
        return recognitionElementRepository.findByTopicIdAndStatus(topicId, ContentStatus.ACTIVE);
    }
}
