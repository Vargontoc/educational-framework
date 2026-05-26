package es.vargontoc.educational.framework.content.infrastructure.persistence;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.StoryPage;
import es.vargontoc.educational.framework.content.ports.out.StoryPageRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class StoryPagePersistenceAdapter implements StoryPageRepository {

    private final StoryPageJpaRepository jpaRepository;

    public StoryPagePersistenceAdapter(StoryPageJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<StoryPage> findById(Long id) {
        return jpaRepository.findById(id)
            .map(StoryPagePersistenceAdapter::toDomain);
    }

    @Override
    public List<StoryPage> findByStoryId(Long storyId) {
        return jpaRepository.findByStoryIdOrderByPageOrderAsc(storyId).stream()
            .map(StoryPagePersistenceAdapter::toDomain)
            .toList();
    }

    @Override
    public List<StoryPage> findByStoryIdAndStatus(Long storyId, ContentStatus status) {
        return jpaRepository.findByStoryIdAndStatusOrderByPageOrderAsc(storyId, status.name()).stream()
            .map(StoryPagePersistenceAdapter::toDomain)
            .toList();
    }

    @Override
    public boolean existsByStoryIdAndPageOrder(Long storyId, Integer pageOrder) {
        return jpaRepository.existsByStoryIdAndPageOrder(storyId, pageOrder);
    }

    @Override
    public StoryPage save(StoryPage storyPage) {
        var saved = jpaRepository.save(toJpa(storyPage));
        return toDomain(saved);
    }

    private static StoryPage toDomain(StoryPageJpaEntity source) {
        var target = new StoryPage();
        target.setId(source.getId());
        target.setStoryId(source.getStoryId());
        target.setPageOrder(source.getPageOrder());
        target.setText(source.getText());
        target.setImageResourceRef(source.getImageResourceRef());
        target.setAudioResourceRef(source.getAudioResourceRef());
        target.setStatus(ContentStatus.valueOf(source.getStatus()));
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    private static StoryPageJpaEntity toJpa(StoryPage source) {
        var target = new StoryPageJpaEntity();
        target.setId(source.getId());
        target.setStoryId(source.getStoryId());
        target.setPageOrder(source.getPageOrder());
        target.setText(source.getText());
        target.setImageResourceRef(source.getImageResourceRef());
        target.setAudioResourceRef(source.getAudioResourceRef());
        target.setStatus(source.getStatus().name());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }
}
