package es.vargontoc.educational.framework.tracking.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ElementSummaryJpaRepository extends JpaRepository<ElementSummaryJpaEntity, Long> {

    Optional<ElementSummaryJpaEntity> findByChildProfileIdAndElementId(Long childProfileId, Long elementId);

    List<ElementSummaryJpaEntity> findByChildProfileId(Long childProfileId);
}
