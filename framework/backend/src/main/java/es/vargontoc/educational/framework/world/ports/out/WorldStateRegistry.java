package es.vargontoc.educational.framework.world.ports.out;

import es.vargontoc.educational.framework.world.model.WorldState;

import java.util.Optional;

public interface WorldStateRegistry {

    void save(WorldState state);

    Optional<WorldState> findByChildSessionId(Long childSessionId);

    boolean existsByChildSessionId(Long childSessionId);

    Optional<Long> getPendingProposalId(Long childSessionId);

    Optional<WorldState> removeByChildSessionId(Long childSessionId);

    void clearClosed();
}