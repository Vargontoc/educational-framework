package es.vargontoc.educational.framework.tracking.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityProposalLogJpaRepository extends JpaRepository<ActivityProposalLogJpaEntity, Long> {

    List<ActivityProposalLogJpaEntity> findByChildProfileId(Long childProfileId);
}