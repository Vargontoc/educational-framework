package es.vargontoc.educational.framework.session.infrastructure.persistence;

import es.vargontoc.educational.framework.session.model.ChildSession;
import es.vargontoc.educational.framework.session.model.ChildSessionStatus;
import es.vargontoc.educational.framework.session.ports.out.ChildSessionRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ChildSessionPersistenceAdapter implements ChildSessionRepository {

    private final ChildSessionJpaRepository jpaRepository;

    public ChildSessionPersistenceAdapter(ChildSessionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<ChildSession> findById(Long id) {
        return jpaRepository.findById(id)
            .map(ChildSessionPersistenceAdapter::toDomain);
    }

    @Override
    public Optional<ChildSession> findActiveByChildProfileId(Long childProfileId) {
        return jpaRepository.findByChildProfileIdAndStatus(childProfileId, ChildSessionStatus.ACTIVE.name())
            .map(ChildSessionPersistenceAdapter::toDomain);
    }

    @Override
    public List<ChildSession> findActiveByFamilyId(Long familyId) {
        return jpaRepository.findByFamilyIdAndStatus(familyId, ChildSessionStatus.ACTIVE.name()).stream()
            .map(ChildSessionPersistenceAdapter::toDomain)
            .toList();
    }

    @Override
    public List<ChildSession> findExpirableSessions(LocalDateTime cutoff) {
        return jpaRepository.findByStatusAndLastActivityAtBefore(ChildSessionStatus.ACTIVE.name(), cutoff).stream()
            .map(ChildSessionPersistenceAdapter::toDomain)
            .toList();
    }

    @Override
    public ChildSession save(ChildSession session) {
        return toDomain(jpaRepository.save(toJpa(session)));
    }

    @Override
    public void saveAll(List<ChildSession> sessions) {
        jpaRepository.saveAll(sessions.stream()
            .map(ChildSessionPersistenceAdapter::toJpa)
            .toList());
    }

    @Override
    public int deleteEndedBefore(LocalDateTime cutoff) {
        return jpaRepository.deleteByEndedAtBeforeAndStatusNot(cutoff, ChildSessionStatus.ACTIVE.name());
    }

    static ChildSession toDomain(ChildSessionJpaEntity source) {
        var target = new ChildSession();
        target.setId(source.getId());
        target.setChildProfileId(source.getChildProfileId());
        target.setFamilyId(source.getFamilyId());
        target.setCreatedAt(source.getCreatedAt());
        target.setStartedAt(source.getStartedAt());
        target.setEndedAt(source.getEndedAt());
        target.setDurationSeconds(source.getDurationSeconds());
        target.setStatus(ChildSessionStatus.valueOf(source.getStatus()));
        target.setLastActivityAt(source.getLastActivityAt());
        target.setHeartbeatIntervalSeconds(source.getHeartbeatIntervalSeconds());
        target.setConnectionMeta(source.getConnectionMeta());
        target.setPersistedGameStateRef(source.getPersistedGameStateRef());
        return target;
    }

    static ChildSessionJpaEntity toJpa(ChildSession source) {
        var target = new ChildSessionJpaEntity();
        target.setId(source.getId());
        target.setChildProfileId(source.getChildProfileId());
        target.setFamilyId(source.getFamilyId());
        target.setCreatedAt(source.getCreatedAt());
        target.setStartedAt(source.getStartedAt());
        target.setEndedAt(source.getEndedAt());
        target.setDurationSeconds(source.getDurationSeconds());
        target.setStatus(source.getStatus().name());
        target.setLastActivityAt(source.getLastActivityAt());
        target.setHeartbeatIntervalSeconds(source.getHeartbeatIntervalSeconds());
        target.setConnectionMeta(source.getConnectionMeta());
        target.setPersistedGameStateRef(source.getPersistedGameStateRef());
        return target;
    }
}
