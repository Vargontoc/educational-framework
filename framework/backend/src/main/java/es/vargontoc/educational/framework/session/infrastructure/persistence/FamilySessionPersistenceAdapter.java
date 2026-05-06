package es.vargontoc.educational.framework.session.infrastructure.persistence;

import es.vargontoc.educational.framework.session.model.FamilySession;
import es.vargontoc.educational.framework.session.model.FamilySessionStatus;
import es.vargontoc.educational.framework.session.ports.out.FamilySessionRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class FamilySessionPersistenceAdapter implements FamilySessionRepository {

    private final FamilySessionJpaRepository jpaRepository;

    public FamilySessionPersistenceAdapter(FamilySessionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<FamilySession> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash)
            .map(FamilySessionPersistenceAdapter::toDomain);
    }

    @Override
    public List<FamilySession> findActiveByFamilyId(Long familyId) {
        return jpaRepository.findByFamilyIdAndStatus(familyId, FamilySessionStatus.ACTIVE.name()).stream()
            .map(FamilySessionPersistenceAdapter::toDomain)
            .toList();
    }

    @Override
    public FamilySession save(FamilySession session) {
        return toDomain(jpaRepository.save(toJpa(session)));
    }

    @Override
    public void saveAll(List<FamilySession> sessions) {
        jpaRepository.saveAll(sessions.stream()
            .map(FamilySessionPersistenceAdapter::toJpa)
            .toList());
    }

    @Override
    public int deleteInactiveUpdatedBefore(LocalDateTime cutoff) {
        return jpaRepository.deleteInactiveUpdatedBefore(cutoff);
    }

    static FamilySession toDomain(FamilySessionJpaEntity source) {
        var target = new FamilySession();
        target.setId(source.getId());
        target.setTokenHash(source.getTokenHash());
        target.setTokenType(source.getTokenType());
        target.setFamilyId(source.getFamilyId());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        target.setExpiresAt(source.getExpiresAt());
        target.setRevoked(source.isRevoked());
        target.setCreatedByIp(source.getCreatedByIp());
        target.setDeviceId(source.getDeviceId());
        target.setStatus(FamilySessionStatus.valueOf(source.getStatus()));
        return target;
    }

    static FamilySessionJpaEntity toJpa(FamilySession source) {
        var target = new FamilySessionJpaEntity();
        target.setId(source.getId());
        target.setTokenHash(source.getTokenHash());
        target.setTokenType(source.getTokenType());
        target.setFamilyId(source.getFamilyId());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        target.setExpiresAt(source.getExpiresAt());
        target.setRevoked(source.isRevoked());
        target.setCreatedByIp(source.getCreatedByIp());
        target.setDeviceId(source.getDeviceId());
        target.setStatus(source.getStatus().name());
        return target;
    }
}
