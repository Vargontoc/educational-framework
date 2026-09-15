package es.vargontoc.educational.framework.game.service;

import es.vargontoc.educational.framework.game.model.recognition.CandidateMetadata;
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
            Function<String, CandidateMetadata> elementResolver) {
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
            Function<String, CandidateMetadata> elementResolver) {
        if (strategy == null || strategy == DistractorStrategy.SEMANTICALLY_FAR) {
            return new ArrayList<>(pool);
        }

        CandidateMetadata targetMetadata = elementResolver.apply(target);
        if (targetMetadata == null) {
            return new ArrayList<>();
        }

        return switch (strategy) {
            case SAME_CATEGORY -> filterBy(pool, elementResolver,
                    m -> m.topicId() != null && m.topicId().equals(targetMetadata.topicId()));
            case SIMILAR_OUTLINE -> filterBy(pool, elementResolver,
                    m -> targetMetadata.similarityGroup() != null
                            && targetMetadata.similarityGroup().equals(m.similarityGroup()));
            case SEMANTICALLY_FAR -> new ArrayList<>(pool);
        };
    }

    private List<String> filterBy(
            List<String> pool,
            Function<String, CandidateMetadata> elementResolver,
            java.util.function.Predicate<CandidateMetadata> matches) {
        List<String> result = new ArrayList<>();
        for (String candidateId : pool) {
            CandidateMetadata candidate = elementResolver.apply(candidateId);
            if (candidate != null && matches.test(candidate)) {
                result.add(candidateId);
            }
        }
        return result;
    }
}
