package es.vargontoc.educational.framework.tracking.ports.out;

import java.util.Map;
import java.util.Set;

public interface ActivityInformationPort {

    Map<Long, String> getGameEngineTypeByActivityIds(Set<Long> activityIds);
}
