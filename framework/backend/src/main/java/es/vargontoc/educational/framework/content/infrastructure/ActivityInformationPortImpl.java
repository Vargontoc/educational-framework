package es.vargontoc.educational.framework.content.infrastructure;

import es.vargontoc.educational.framework.content.infrastructure.persistence.ActivityJpaRepository;
import es.vargontoc.educational.framework.tracking.ports.out.ActivityInformationPort;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class ActivityInformationPortImpl implements ActivityInformationPort {

    private final ActivityJpaRepository activityJpaRepository;

    public ActivityInformationPortImpl(ActivityJpaRepository activityJpaRepository) {
        this.activityJpaRepository = activityJpaRepository;
    }

    @Override
    public Map<Long, String> getGameEngineTypeByActivityIds(Set<Long> activityIds) {
        if (activityIds == null || activityIds.isEmpty()) {
            return Map.of();
        }

        return activityJpaRepository.findAllById(activityIds).stream()
            .filter(entity -> entity.getGameEngineType() != null)
            .collect(Collectors.toMap(
                entity -> entity.getId(),
                entity -> entity.getGameEngineType()
            ));
    }
}
