package es.vargontoc.educational.framework.world.infrastructure;

import es.vargontoc.educational.framework.world.model.WorldRuntimeStatus;
import es.vargontoc.educational.framework.world.model.WorldState;
import es.vargontoc.educational.framework.world.ports.out.WorldStateRegistry;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryWorldStateRegistry implements WorldStateRegistry {

    private final Map<Long, WorldState> stateByChildSession = new ConcurrentHashMap<>();

    @Override
    public void save(WorldState state) {
        if (state.getChildSessionId() != null) {
            stateByChildSession.put(state.getChildSessionId(), state);
        }
    }

    @Override
    public Optional<WorldState> findByChildSessionId(Long childSessionId) {
        return Optional.ofNullable(stateByChildSession.get(childSessionId))
            .filter(state -> state.getStatus() == WorldRuntimeStatus.ACTIVE);
    }

    @Override
    public boolean existsByChildSessionId(Long childSessionId) {
        return findByChildSessionId(childSessionId).isPresent();
    }

    @Override
    public Optional<Long> getPendingProposalId(Long childSessionId) {
        return findByChildSessionId(childSessionId)
            .map(WorldState::getPendingProposalId)
            .filter(proposalId -> proposalId != null);
    }

    @Override
    public Optional<WorldState> removeByChildSessionId(Long childSessionId) {
        return Optional.ofNullable(stateByChildSession.remove(childSessionId));
    }

    @Override
    public void clearClosed() {
        stateByChildSession.entrySet().removeIf(entry ->
            entry.getValue().getStatus() == WorldRuntimeStatus.CLOSED
        );
    }
}