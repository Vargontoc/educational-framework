package es.vargontoc.educational.framework.game.service;

import es.vargontoc.educational.framework.content.model.RecognitionElement;
import es.vargontoc.educational.framework.game.model.recognition.DistractorStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class DistractorSelector {

    private final Random random;

    public DistractorSelector() {
        this(new Random());
    }

    public DistractorSelector(Random random) {
        this.random = random;
    }

    public List<String> select(
            String target,
            List<String> candidates,
            DistractorStrategy strategy,
            int count,
            Function<String, RecognitionElement> elementResolver) {
        List<String> pool = new ArrayList<>();
        for (String candidate : candidates) {
            if (!candidate.equals(target)) {
                pool.add(candidate);
            }
        }

        List<String> primary = filterByStrategy(target, pool, strategy, elementResolver);
        Collections.shuffle(primary, random);

        List<String> selected = new ArrayList<>();
        for (int i = 0; i < count && i < primary.size(); i++) {
            selected.add(primary.get(i));
        }

        if (selected.size() < count) {
            List<String> remaining = new ArrayList<>(pool);
            remaining.removeAll(selected);
            Collections.shuffle(remaining, random);
            for (String candidate : remaining) {
                if (selected.size() >= count) {
                    break;
                }
                selected.add(candidate);
            }
        }

        return selected;
    }

    private List<String> filterByStrategy(
            String target,
            List<String> pool,
            DistractorStrategy strategy,
            Function<String, RecognitionElement> elementResolver) {
        if (strategy == null || strategy == DistractorStrategy.SEMANTICALLY_FAR) {
            return new ArrayList<>(pool);
        }

        RecognitionElement targetElement = elementResolver.apply(target);
        if (targetElement == null) {
            return new ArrayList<>();
        }

        return switch (strategy) {
            case SAME_CATEGORY -> filterBy(pool, elementResolver,
                    e -> e.getTopicId() != null && e.getTopicId().equals(targetElement.getTopicId()));
            case SIMILAR_OUTLINE -> filterBy(pool, elementResolver,
                    e -> targetElement.getSimilarityGroup() != null
                            && targetElement.getSimilarityGroup().equals(e.getSimilarityGroup()));
            case SEMANTICALLY_FAR -> new ArrayList<>(pool);
        };
    }

    private List<String> filterBy(
            List<String> pool,
            Function<String, RecognitionElement> elementResolver,
            java.util.function.Predicate<RecognitionElement> matches) {
        List<String> result = new ArrayList<>();
        for (String candidateId : pool) {
            RecognitionElement candidate = elementResolver.apply(candidateId);
            if (candidate != null && matches.test(candidate)) {
                result.add(candidateId);
            }
        }
        return result;
    }
}
