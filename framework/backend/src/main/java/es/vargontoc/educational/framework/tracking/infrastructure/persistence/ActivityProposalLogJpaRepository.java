package es.vargontoc.educational.framework.tracking.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityProposalLogJpaRepository extends JpaRepository<ActivityProposalLogJpaEntity, Long> {

    List<ActivityProposalLogJpaEntity> findByChildProfileId(Long childProfileId);
}