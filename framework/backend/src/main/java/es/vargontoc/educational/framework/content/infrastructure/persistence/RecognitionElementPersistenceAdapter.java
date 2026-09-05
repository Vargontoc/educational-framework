package es.vargontoc.educational.framework.content.infrastructure.persistence;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.RecognitionElement;
import es.vargontoc.educational.framework.content.ports.out.RecognitionElementRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RecognitionElementPersistenceAdapter implements RecognitionElementRepository {

    private final RecognitionElementJpaRepository jpaRepository;

    public RecognitionElementPersistenceAdapter(RecognitionElementJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<RecognitionElement> findByTopicIdAndStatus(Long topicId, ContentStatus status) {
        return jpaRepository.findByTopicIdAndStatus(topicId, status.name())
                .stream()
                .map(RecognitionElementPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public RecognitionElement save(RecognitionElement element) {
        return toDomain(jpaRepository.save(toJpa(element)));
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<RecognitionElement> findAllById(List<Long> ids) {
        return jpaRepository.findAllById(ids)
                .stream()
                .map(RecognitionElementPersistenceAdapter::toDomain)
                .toList();
    }

    static RecognitionElement toDomain(RecognitionElementJpaEntity source) {
        var target = new RecognitionElement();
        target.setId(source.getId());
        target.setTopicId(source.getTopicId());
        target.setCode(source.getCode());
        target.setDisplayValue(source.getDisplayValue());
        target.setResourceRefs(source.getResourceRefs());
        target.setSortOrder(source.getSortOrder());
        target.setStatus(ContentStatus.valueOf(source.getStatus()));
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    static RecognitionElementJpaEntity toJpa(RecognitionElement source) {
        var target = new RecognitionElementJpaEntity();
        target.setId(source.getId());
        target.setTopicId(source.getTopicId());
        target.setCode(source.getCode());
        target.setDisplayValue(source.getDisplayValue());
        target.setResourceRefs(source.getResourceRefs());
        target.setSortOrder(source.getSortOrder());
        target.setStatus(source.getStatus().name());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }
}
