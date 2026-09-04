package es.vargontoc.educational.framework.tracking.infrastructure.persistence;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.RecognitionElement;
import es.vargontoc.educational.framework.content.ports.out.RecognitionElementRepository;
import es.vargontoc.educational.framework.tracking.model.ElementSummary;
import es.vargontoc.educational.framework.tracking.ports.out.ElementProgressPort;
import es.vargontoc.educational.framework.tracking.ports.out.ElementSummaryRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class ElementProgressPortAdapter implements ElementProgressPort {

    private final ElementSummaryRepository elementSummaryRepository;
    private final RecognitionElementRepository recognitionElementRepository;

    public ElementProgressPortAdapter(
            ElementSummaryRepository elementSummaryRepository,
            RecognitionElementRepository recognitionElementRepository) {
        this.elementSummaryRepository = elementSummaryRepository;
        this.recognitionElementRepository = recognitionElementRepository;
    }

    @Override
    public List<ElementSummary> getElementSummariesForChildInTopic(Long childProfileId, Long topicId) {
        List<RecognitionElement> elements = recognitionElementRepository.findByTopicIdAndStatus(topicId, ContentStatus.ACTIVE);
        Set<Long> elementIds = elements.stream()
                .map(re -> re.getId())
                .collect(Collectors.toSet());

        if (elementIds.isEmpty()) {
            return List.of();
        }

        return elementSummaryRepository.findByChildProfileId(childProfileId).stream()
                .filter(s -> elementIds.contains(s.getElementId()))
                .toList();
    }
}
