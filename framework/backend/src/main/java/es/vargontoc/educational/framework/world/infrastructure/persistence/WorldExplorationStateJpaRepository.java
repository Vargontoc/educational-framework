package es.vargontoc.educational.framework.world.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface WorldExplorationStateJpaRepository extends JpaRepository<WorldExplorationStateJpaEntity, Long> {

    @Modifying
    @Query("UPDATE WorldExplorationStateJpaEntity e SET e.positionX = :positionX, e.positionY = :positionY, e.updatedAt = :updatedAt WHERE e.childProfileId = :childProfileId")
    int updatePosition(@Param("childProfileId") Long childProfileId, @Param("positionX") Double positionX, @Param("positionY") Double positionY, @Param("updatedAt") LocalDateTime updatedAt);

    @Modifying
    @Query("UPDATE WorldExplorationStateJpaEntity e SET e.biome = :biome, e.positionX = :positionX, e.positionY = :positionY, e.updatedAt = :updatedAt WHERE e.childProfileId = :childProfileId")
    int updateBiomeAndPosition(@Param("childProfileId") Long childProfileId, @Param("biome") String biome, @Param("positionX") Double positionX, @Param("positionY") Double positionY, @Param("updatedAt") LocalDateTime updatedAt);
}
