package es.vargontoc.educational.framework.content.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DevSeedStateJpaRepository extends JpaRepository<DevSeedStateJpaEntity, String> {

    boolean existsBySeedKey(String seedKey);
}
