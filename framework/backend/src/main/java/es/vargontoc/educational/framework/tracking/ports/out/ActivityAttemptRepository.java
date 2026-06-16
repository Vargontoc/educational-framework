package es.vargontoc.educational.framework.tracking.ports.out;

import es.vargontoc.educational.framework.tracking.model.ActivityAttempt;

public interface ActivityAttemptRepository {

    ActivityAttempt save(ActivityAttempt attempt);
}
