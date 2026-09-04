package es.vargontoc.educational.framework.tracking.infrastructure.persistence;

import es.vargontoc.educational.framework.tracking.model.ElementMasteryState;
import es.vargontoc.educational.framework.tracking.model.ElementSummary;
import es.vargontoc.educational.framework.tracking.ports.out.ElementSummaryRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ElementSummaryPersistenceAdapter implements ElementSummaryRepository {

    private final ElementSummaryJpaRepository jpaRepository;

    public ElementSummaryPersistenceAdapter(ElementSummaryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<ElementSummary> findByChildProfileIdAndElementId(Long childProfileId, Long elementId) {
        return jpaRepository.findByChildProfileIdAndElementId(childProfileId, elementId)
                .map(ElementSummaryPersistenceAdapter::toDomain);
    }

    @Override
    public List<ElementSummary> findByChildProfileId(Long childProfileId) {
        return jpaRepository.findByChildProfileId(childProfileId)
                .stream()
                .map(ElementSummaryPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public ElementSummary save(ElementSummary summary) {
        return toDomain(jpaRepository.save(toJpa(summary)));
    }

    static ElementSummary toDomain(ElementSummaryJpaEntity source) {
        var target = new ElementSummary();
        target.setId(source.getId());
        target.setChildProfileId(source.getChildProfileId());
        target.setElementId(source.getElementId());
        target.setTotalAttempts(source.getTotalAttempts());
        target.setTotalCorrect(source.getTotalCorrect());
        target.setTotalIncorrect(source.getTotalIncorrect());
        target.setSuccessRatePercent(source.getSuccessRatePercent());
        target.setAverageResponseTimeMs(source.getAverageResponseTimeMs());
        target.setLastSeenAt(source.getLastSeenAt());
        target.setMasteryState(source.getMasteryState() != null
                ? ElementMasteryState.valueOf(source.getMasteryState()) : ElementMasteryState.NOT_STARTED);
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    static ElementSummaryJpaEntity toJpa(ElementSummary source) {
        var target = new ElementSummaryJpaEntity();
        target.setId(source.getId());
        target.setChildProfileId(source.getChildProfileId());
        target.setElementId(source.getElementId());
        target.setTotalAttempts(source.getTotalAttempts());
        target.setTotalCorrect(source.getTotalCorrect());
        target.setTotalIncorrect(source.getTotalIncorrect());
        target.setSuccessRatePercent(source.getSuccessRatePercent());
        target.setAverageResponseTimeMs(source.getAverageResponseTimeMs());
        target.setLastSeenAt(source.getLastSeenAt());
        target.setMasteryState(source.getMasteryState() != null
                ? source.getMasteryState().name() : ElementMasteryState.NOT_STARTED.name());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }
}
