package es.vargontoc.educational.framework.tracking.infrastructure.persistence;

import es.vargontoc.educational.framework.tracking.model.ActivityProposalLog;
import es.vargontoc.educational.framework.tracking.model.ActivityProposalOutcome;
import es.vargontoc.educational.framework.tracking.ports.out.ActivityProposalLogRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ActivityProposalLogPersistenceAdapter implements ActivityProposalLogRepository {

    private final ActivityProposalLogJpaRepository jpaRepository;

    public ActivityProposalLogPersistenceAdapter(ActivityProposalLogJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ActivityProposalLog save(ActivityProposalLog proposal) {
        return toDomain(jpaRepository.save(toJpa(proposal)));
    }

    @Override
    public Optional<ActivityProposalLog> findById(Long id) {
        return jpaRepository.findById(id).map(ActivityProposalLogPersistenceAdapter::toDomain);
    }

    static ActivityProposalLog toDomain(ActivityProposalLogJpaEntity source) {
        var target = new ActivityProposalLog();
        target.setId(source.getId());
        target.setChildProfileId(source.getChildProfileId());
        target.setChildSessionId(source.getChildSessionId());
        target.setActivityId(source.getActivityId());
        target.setTopicId(source.getTopicId());
        if (source.getOutcome() != null) {
            target.setOutcome(ActivityProposalOutcome.valueOf(source.getOutcome()));
        }
        target.setProposedAt(source.getProposedAt());
        target.setResolvedAt(source.getResolvedAt());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    static ActivityProposalLogJpaEntity toJpa(ActivityProposalLog source) {
        var target = new ActivityProposalLogJpaEntity();
        target.setId(source.getId());
        target.setChildProfileId(source.getChildProfileId());
        target.setChildSessionId(source.getChildSessionId());
        target.setActivityId(source.getActivityId());
        target.setTopicId(source.getTopicId());
        if (source.getOutcome() != null) {
            target.setOutcome(source.getOutcome().name());
        }
        target.setProposedAt(source.getProposedAt());
        target.setResolvedAt(source.getResolvedAt());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }
}
