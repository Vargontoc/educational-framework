package es.vargontoc.educational.framework.tracking.infrastructure.persistence;

import es.vargontoc.educational.framework.tracking.model.GameSessionSummary;
import es.vargontoc.educational.framework.tracking.model.GameSessionFinalStatus;
import es.vargontoc.educational.framework.tracking.ports.out.GameSessionSummaryRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class GameSessionSummaryPersistenceAdapter implements GameSessionSummaryRepository {

    private final GameSessionSummaryJpaRepository jpaRepository;

    public GameSessionSummaryPersistenceAdapter(GameSessionSummaryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public GameSessionSummary save(GameSessionSummary summary) {
        return toDomain(jpaRepository.save(toJpa(summary)));
    }

    @Override
    public List<GameSessionSummary> findByChildProfileId(Long childProfileId) {
        return jpaRepository.findByChildProfileId(childProfileId).stream()
            .map(GameSessionSummaryPersistenceAdapter::toDomain)
            .toList();
    }

    static GameSessionSummary toDomain(GameSessionSummaryJpaEntity source) {
        var target = new GameSessionSummary();
        target.setId(source.getId());
        target.setChildProfileId(source.getChildProfileId());
        target.setChildSessionId(source.getChildSessionId());
        target.setActivityId(source.getActivityId());
        target.setDifficultyLevelStartId(source.getDifficultyLevelStartId());
        target.setDifficultyLevelEndId(source.getDifficultyLevelEndId());
        target.setScore(source.getScore());
        target.setTotalAttempts(source.getTotalAttempts());
        target.setTotalCorrect(source.getTotalCorrect());
        target.setTotalTimeouts(source.getTotalTimeouts());
        target.setStartedAt(source.getStartedAt());
        target.setEndedAt(source.getEndedAt());
        target.setFinalStatus(GameSessionFinalStatus.valueOf(source.getFinalStatus()));
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    static GameSessionSummaryJpaEntity toJpa(GameSessionSummary source) {
        var target = new GameSessionSummaryJpaEntity();
        target.setId(source.getId());
        target.setChildProfileId(source.getChildProfileId());
        target.setChildSessionId(source.getChildSessionId());
        target.setActivityId(source.getActivityId());
        target.setDifficultyLevelStartId(source.getDifficultyLevelStartId());
        target.setDifficultyLevelEndId(source.getDifficultyLevelEndId());
        target.setScore(source.getScore());
        target.setTotalAttempts(source.getTotalAttempts());
        target.setTotalCorrect(source.getTotalCorrect());
        target.setTotalTimeouts(source.getTotalTimeouts());
        target.setStartedAt(source.getStartedAt());
        target.setEndedAt(source.getEndedAt());
        target.setFinalStatus(source.getFinalStatus().name());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }
}