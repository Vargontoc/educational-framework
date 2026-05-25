package es.vargontoc.educational.framework.content.ports.in;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.Curiosity;

import java.util.List;

public interface CuriosityUseCase {

    Curiosity createCuriosity(String text, Long topicId, Integer minAge, Integer maxAge, List<String> tags, String locale, String phoneticHint, ContentStatus status);

    Curiosity getCuriosity(Long id);

    List<Curiosity> listCuriosities();

    List<Curiosity> listCuriositiesByTopic(Long topicId);

    List<Curiosity> listActiveCuriositiesByFilters(Long topicId, Integer age, String locale);

    Curiosity updateCuriosity(Long id, String text, Long topicId, Integer minAge, Integer maxAge, List<String> tags, String locale, String phoneticHint, ContentStatus status);
}
