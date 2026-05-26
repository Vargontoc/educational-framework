package es.vargontoc.educational.framework.content.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoryPageJpaRepository extends JpaRepository<StoryPageJpaEntity, Long> {

    List<StoryPageJpaEntity> findByStoryIdOrderByPageOrderAsc(Long storyId);

    List<StoryPageJpaEntity> findByStoryIdAndStatusOrderByPageOrderAsc(Long storyId, String status);

    boolean existsByStoryIdAndPageOrder(Long storyId, Integer pageOrder);
}
