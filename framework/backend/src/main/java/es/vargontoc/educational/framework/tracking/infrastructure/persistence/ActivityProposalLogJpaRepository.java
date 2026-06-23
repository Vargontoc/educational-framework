package es.vargontoc.educational.framework.tracking.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityProposalLogJpaRepository extends JpaRepository<ActivityProposalLogJpaEntity, Long> {
}