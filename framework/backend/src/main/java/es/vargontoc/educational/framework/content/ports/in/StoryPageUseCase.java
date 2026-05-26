package es.vargontoc.educational.framework.content.ports.in;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.StoryPage;

import java.util.List;

public interface StoryPageUseCase {

    StoryPage createPage(Long storyId, Integer pageOrder, String text, String imageResourceRef, String audioResourceRef, ContentStatus status);

    StoryPage getPage(Long id);

    List<StoryPage> listPagesByStory(Long storyId);

    List<StoryPage> listActivePagesByStory(Long storyId);

    StoryPage updatePage(Long id, Long storyId, Integer pageOrder, String text, String imageResourceRef, String audioResourceRef, ContentStatus status);
}
