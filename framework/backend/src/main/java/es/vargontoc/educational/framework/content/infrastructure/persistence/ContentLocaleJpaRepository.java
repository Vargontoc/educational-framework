package es.vargontoc.educational.framework.content.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContentLocaleJpaRepository extends JpaRepository<ContentLocaleJpaEntity, Long> {

    List<ContentLocaleJpaEntity> findByEntityTypeAndEntityId(String entityType, Long entityId);

    void deleteByEntityTypeAndEntityIdAndLocaleCode(String entityType, Long entityId, String localeCode);
}
