package es.vargontoc.educational.framework.game.ports.out;

import java.util.List;

public interface SessionAntiRepetitionRegistry {

    void registerRecentElement(Long childSessionId, Long topicId, String elementId);

    List<String> getRecentElements(Long childSessionId, Long topicId);

    void clearSession(Long childSessionId);
}
