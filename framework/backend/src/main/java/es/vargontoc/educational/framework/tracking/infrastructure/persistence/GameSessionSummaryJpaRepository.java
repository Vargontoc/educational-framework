package es.vargontoc.educational.framework.tracking.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameSessionSummaryJpaRepository extends JpaRepository<GameSessionSummaryJpaEntity, Long> {

    List<GameSessionSummaryJpaEntity> findByChildProfileId(Long childProfileId);
}