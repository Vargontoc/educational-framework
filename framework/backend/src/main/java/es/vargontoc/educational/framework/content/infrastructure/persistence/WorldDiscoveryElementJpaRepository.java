package es.vargontoc.educational.framework.content.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorldDiscoveryElementJpaRepository extends JpaRepository<WorldDiscoveryElementJpaEntity, Long> {
    Optional<WorldDiscoveryElementJpaEntity> findByCode(String code);
    boolean existsByCode(String code);

    @Query("SELECT e FROM WorldDiscoveryElementJpaEntity e WHERE e.status = :status AND e.minAge <= :targetAge AND e.maxAge >= :targetAge")
    List<WorldDiscoveryElementJpaEntity> findByStatusAndAgeRange(@Param("status") String status, @Param("targetAge") Integer targetAge);

    @Query("SELECT e FROM WorldDiscoveryElementJpaEntity e WHERE e.status = :status AND e.biome = :biome AND e.minAge <= :targetAge AND e.maxAge >= :targetAge")
    List<WorldDiscoveryElementJpaEntity> findByStatusAndBiomeAndAgeRange(@Param("status") String status, @Param("biome") String biome, @Param("targetAge") Integer targetAge);
}
