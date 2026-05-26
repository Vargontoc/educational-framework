package es.vargontoc.educational.framework.content.service;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.StoryPage;
import es.vargontoc.educational.framework.content.ports.in.StoryPageUseCase;
import es.vargontoc.educational.framework.content.ports.out.StoryPageRepository;
import es.vargontoc.educational.framework.content.ports.out.StoryRepository;
import es.vargontoc.educational.framework.content.validation.StoryPageValidator;
import es.vargontoc.educational.framework.shared.exception.ConflictException;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Transactional
public class StoryPageService implements StoryPageUseCase {

    private final StoryPageRepository storyPageRepository;
    private final StoryRepository storyRepository;
    private final StoryPageValidator storyPageValidator;

    public StoryPageService(StoryPageRepository storyPageRepository, StoryRepository storyRepository) {
        this.storyPageRepository = storyPageRepository;
        this.storyRepository = storyRepository;
        this.storyPageValidator = new StoryPageValidator();
    }

    @Override
    public StoryPage createPage(Long storyId, Integer pageOrder, String text, String imageResourceRef, String audioResourceRef, ContentStatus status) {
        storyPageValidator.validateForCreate(storyId, pageOrder, text, status);

        if (storyRepository.findById(storyId).isEmpty()) {
            throw new ResourceNotFoundException("Story not found with id: " + storyId);
        }
        if (storyPageRepository.existsByStoryIdAndPageOrder(storyId, pageOrder)) {
            throw new ConflictException("Page order " + pageOrder + " already exists in story " + storyId);
        }

        var page = new StoryPage();
        page.setStoryId(storyId);
        page.setPageOrder(pageOrder);
        page.setText(text);
        page.setImageResourceRef(imageResourceRef);
        page.setAudioResourceRef(audioResourceRef);
        page.setStatus(status);
        page.setCreatedAt(LocalDateTime.now());

        return storyPageRepository.save(page);
    }

    @Override
    @Transactional(readOnly = true)
    public StoryPage getPage(Long id) {
        return storyPageRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("StoryPage not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoryPage> listPagesByStory(Long storyId) {
        return storyPageRepository.findByStoryId(storyId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoryPage> listActivePagesByStory(Long storyId) {
        return storyPageRepository.findByStoryIdAndStatus(storyId, ContentStatus.ACTIVE);
    }

    @Override
    public StoryPage updatePage(Long id, Long storyId, Integer pageOrder, String text, String imageResourceRef, String audioResourceRef, ContentStatus status) {
        storyPageValidator.validateForUpdate(storyId, pageOrder, text, status);

        var existing = storyPageRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("StoryPage not found with id: " + id));

        if (storyRepository.findById(storyId).isEmpty()) {
            throw new ResourceNotFoundException("Story not found with id: " + storyId);
        }

        boolean samePosition = existing.getStoryId().equals(storyId) && existing.getPageOrder().equals(pageOrder);
        if (!samePosition && storyPageRepository.existsByStoryIdAndPageOrder(storyId, pageOrder)) {
            throw new ConflictException("Page order " + pageOrder + " already exists in story " + storyId);
        }

        existing.setStoryId(storyId);
        existing.setPageOrder(pageOrder);
        existing.setText(text);
        existing.setImageResourceRef(imageResourceRef);
        existing.setAudioResourceRef(audioResourceRef);
        existing.setStatus(status);
        existing.setUpdatedAt(LocalDateTime.now());

        return storyPageRepository.save(existing);
    }
}
