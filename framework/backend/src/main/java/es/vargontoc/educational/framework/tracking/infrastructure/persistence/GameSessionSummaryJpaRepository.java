package es.vargontoc.educational.framework.tracking.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameSessionSummaryJpaRepository extends JpaRepository<GameSessionSummaryJpaEntity, Long> {

    List<GameSessionSummaryJpaEntity> findByChildProfileId(Long childProfileId);
}