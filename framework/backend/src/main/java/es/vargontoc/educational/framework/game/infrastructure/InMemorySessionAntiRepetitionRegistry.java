package es.vargontoc.educational.framework.game.infrastructure;

import es.vargontoc.educational.framework.game.ports.out.SessionAntiRepetitionRegistry;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class InMemorySessionAntiRepetitionRegistry implements SessionAntiRepetitionRegistry {

    private final Map<Long, Map<Long, List<String>>> data = new ConcurrentHashMap<>();

    @Override
    public void registerRecentElement(Long childSessionId, Long topicId, String elementId) {
        if (childSessionId == null || topicId == null || elementId == null) {
            return;
        }
        data.computeIfAbsent(childSessionId, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(topicId, k -> new CopyOnWriteArrayList<>())
                .add(elementId);
    }

    @Override
    public List<String> getRecentElements(Long childSessionId, Long topicId) {
        if (childSessionId == null || topicId == null) {
            return List.of();
        }
        Map<Long, List<String>> topicMap = data.get(childSessionId);
        if (topicMap == null) {
            return List.of();
        }
        List<String> elements = topicMap.get(topicId);
        if (elements == null) {
            return List.of();
        }
        return new ArrayList<>(elements);
    }

    @Override
    public void clearSession(Long childSessionId) {
        if (childSessionId == null) {
            return;
        }
        data.remove(childSessionId);
    }
}
