package es.vargontoc.educational.framework.session.infrastructure.persistence;

import es.vargontoc.educational.framework.session.model.ChildSession;
import es.vargontoc.educational.framework.session.model.ChildSessionStatus;
import es.vargontoc.educational.framework.session.ports.out.ChildSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ChildSessionPersistenceAdapter implements ChildSessionRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChildSessionPersistenceAdapter.class);

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
        List<ChildSessionJpaEntity> activeSessions = jpaRepository
            .findByChildProfileIdAndStatusOrderByStartedAtDesc(childProfileId, ChildSessionStatus.ACTIVE.name());

        if (activeSessions.size() > 1) {
            LOGGER.warn(
                "Found {} ACTIVE child_session rows for childProfileId={} (expected at most 1); using the most "
                    + "recently started one (id={}) and ignoring the rest: {}",
                activeSessions.size(),
                childProfileId,
                activeSessions.get(0).getId(),
                activeSessions.stream().map(ChildSessionJpaEntity::getId).toList());
        }

        return activeSessions.stream().findFirst().map(ChildSessionPersistenceAdapter::toDomain);
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
    public ChildSession saveAndFlush(ChildSession session) {
        return toDomain(jpaRepository.saveAndFlush(toJpa(session)));
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
