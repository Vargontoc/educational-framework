package es.vargontoc.educational.framework.tracking.ports.out;

import java.util.Map;
import java.util.Set;

public interface ActivityInformationPort {

    Map<Long, String> getGameEngineTypeByActivityIds(Set<Long> activityIds);

    Map<Long, ActivityDetail> getDetailsByActivityIds(Set<Long> activityIds);

    Map<Long, String> getDifficultyCodesByIds(Set<Long> difficultyLevelIds);

    /** {@code subcategory} solo aplica cuando {@code category == "RECOGNITION"} (LETTER/NUMBER/SHAPE/COLOR/ANIMAL); null en el resto. */
    record ActivityDetail(String name, String gameEngineType, String category, String subcategory) {
    }
}
