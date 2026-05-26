package es.vargontoc.educational.framework.content.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoryJpaRepository extends JpaRepository<StoryJpaEntity, Long> {

    List<StoryJpaEntity> findByStatus(String status);
}
