package es.vargontoc.educational.framework.content.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorldHostJpaRepository extends JpaRepository<WorldHostJpaEntity, Long> {
    Optional<WorldHostJpaEntity> findByCode(String code);
    boolean existsByCode(String code);

    @Query("SELECT h FROM WorldHostJpaEntity h WHERE h.status = :status AND h.minAge <= :targetAge AND h.maxAge >= :targetAge")
    List<WorldHostJpaEntity> findByStatusAndAgeRange(@Param("status") String status, @Param("targetAge") Integer targetAge);
}
