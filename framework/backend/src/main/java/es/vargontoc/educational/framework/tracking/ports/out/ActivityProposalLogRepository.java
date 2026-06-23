package es.vargontoc.educational.framework.tracking.ports.out;

import es.vargontoc.educational.framework.tracking.model.ActivityProposalLog;

import java.util.Optional;

public interface ActivityProposalLogRepository {

    ActivityProposalLog save(ActivityProposalLog proposal);

    Optional<ActivityProposalLog> findById(Long id);
}
