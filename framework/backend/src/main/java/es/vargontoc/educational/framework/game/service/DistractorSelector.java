package es.vargontoc.educational.framework.game.service;

import es.vargontoc.educational.framework.game.model.enums.RecognitionCategory;
import es.vargontoc.educational.framework.game.model.recognition.CandidateMetadata;
import es.vargontoc.educational.framework.game.model.recognition.DistractorStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

public class DistractorSelector {

    private final Random random;
    private final LetterSimilarityService letterSimilarityService;

    public DistractorSelector() {
        this(new Random(), null);
    }

    public DistractorSelector(Random random) {
        this(random, null);
    }

    public DistractorSelector(LetterSimilarityService letterSimilarityService) {
        this(new Random(), letterSimilarityService);
    }

    public DistractorSelector(Random random, LetterSimilarityService letterSimilarityService) {
        this.random = random;
        this.letterSimilarityService = letterSimilarityService;
    }

    /**
     * Backward-compatible select without category awareness.
     */
    public List<String> select(
            String target,
            List<String> candidates,
            DistractorStrategy strategy,
            int count,
            Function<String, CandidateMetadata> elementResolver) {
        return select(target, candidates, strategy, count, null, elementResolver);
    }

    /**
     * Selects distractors with optional letter-similarity awareness.
     *
     * When category is LETTER and letterSimilarityService is available:
     * - EASY/MEDIUM (SEMANTICALLY_FAR, SAME_CATEGORY): exclude letters that appear in similarity pairs with target
     * - HARD (SIMILAR_OUTLINE): use similarity table with priority strong -> moderate -> weak
     *
     * For other categories or when no service is available, falls back to existing strategy logic.
     */
    public List<String> select(
            String target,
            List<String> candidates,
            DistractorStrategy strategy,
            int count,
            RecognitionCategory category,
            Function<String, CandidateMetadata> elementResolver) {

        // Build base pool (all candidates except target)
        List<String> pool = new ArrayList<>();
        for (String candidate : candidates) {
            if (!candidate.equals(target)) {
                pool.add(candidate);
            }
        }

        // LETTER-specific logic
        if (category == RecognitionCategory.LETTER && letterSimilarityService != null) {
            return selectForLetter(target, pool, strategy, count, elementResolver);
        }

        // Non-letter or no service: existing strategy logic
        List<String> primary = filterByStrategy(target, pool, strategy, elementResolver);
        Collections.shuffle(primary, random);

        return fillFromPrimary(primary, pool, count);
    }

    /**
     * LETTER-specific distractor selection using the similarity table.
     */
    private List<String> selectForLetter(
            String target,
            List<String> pool,
            DistractorStrategy strategy,
            int count,
            Function<String, CandidateMetadata> elementResolver) {

        String targetCode = resolveCode(target, elementResolver);

        if (targetCode == null || !letterSimilarityService.hasSimilarityEntries(targetCode)) {
            // No similarity entries for this letter: fallback to similarityGroup or random
            return selectWithFallback(target, pool, strategy, count, elementResolver);
        }

        if (strategy == DistractorStrategy.SIMILAR_OUTLINE) {
            // HARD: use similarity table with priority strong -> moderate -> weak
            return selectHardDistractors(target, pool, count, targetCode, elementResolver);
        } else {
            // EASY/MEDIUM: exclude letters from similarity table, then random
            return selectEasyMediumDistractors(target, pool, count, targetCode, elementResolver);
        }
    }

    /**
     * EASY/MEDIUM: exclude all letters that appear in similarity pairs with target, then random.
     */
    private List<String> selectEasyMediumDistractors(
            String target,
            List<String> pool,
            int count,
            String targetCode,
            Function<String, CandidateMetadata> elementResolver) {

        Set<String> excluded = letterSimilarityService.getExcludedLetters(targetCode);

        // Build set of excluded candidate IDs by matching codes
        java.util.Set<String> excludedIds = new java.util.HashSet<>();
        for (String candidateId : pool) {
            String code = resolveCode(candidateId, elementResolver);
            if (code != null && excluded.contains(code)) {
                excludedIds.add(candidateId);
            }
        }

        // Filter pool: remove excluded letters
        List<String> filtered = new ArrayList<>();
        for (String candidateId : pool) {
            if (!excludedIds.contains(candidateId)) {
                filtered.add(candidateId);
            }
        }

        // Random selection from filtered pool
        Collections.shuffle(filtered, random);

        List<String> selected = new ArrayList<>();
        for (int i = 0; i < count && i < filtered.size(); i++) {
            selected.add(filtered.get(i));
        }

        // Fallback: if not enough after exclusion, fill from full pool
        if (selected.size() < count) {
            List<String> remaining = new ArrayList<>(pool);
            remaining.removeAll(selected);
            Collections.shuffle(remaining, random);
            for (String candidate : remaining) {
                if (selected.size() >= count) break;
                selected.add(candidate);
            }
        }

        return selected;
    }

    /**
     * HARD: select from similarity table with priority strong -> moderate -> weak.
     * If not enough similar letters, fallback to random from remaining pool.
     */
    private List<String> selectHardDistractors(
            String target,
            List<String> pool,
            int count,
            String targetCode,
            Function<String, CandidateMetadata> elementResolver) {

        // Build code -> candidateId map for the pool
        Map<String, String> codeToId = new HashMap<>();
        for (String candidateId : pool) {
            CandidateMetadata meta = elementResolver.apply(candidateId);
            if (meta != null && meta.code() != null) {
                codeToId.put(meta.code(), candidateId);
            }
        }

        // Get similar letters ordered by strength (request more than needed to account for missing candidates)
        List<String> similarCodes = letterSimilarityService.getSimilarLetters(targetCode, count * 2);

        List<String> selected = new ArrayList<>();
        List<String> usedIds = new ArrayList<>();

        // Pick from similarity table first
        for (String similarCode : similarCodes) {
            if (selected.size() >= count) break;
            String candidateId = codeToId.get(similarCode);
            if (candidateId != null && !usedIds.contains(candidateId)) {
                selected.add(candidateId);
                usedIds.add(candidateId);
            }
        }

        // Fallback: fill remaining from pool (excluding already selected)
        if (selected.size() < count) {
            List<String> remaining = new ArrayList<>(pool);
            remaining.removeAll(usedIds);
            Collections.shuffle(remaining, random);
            for (String candidate : remaining) {
                if (selected.size() >= count) break;
                selected.add(candidate);
            }
        }

        return selected;
    }

    /**
     * Fallback when no similarity entries exist for the target letter.
     * Uses similarityGroup if available, otherwise random.
     */
    private List<String> selectWithFallback(
            String target,
            List<String> pool,
            DistractorStrategy strategy,
            int count,
            Function<String, CandidateMetadata> elementResolver) {

        List<String> primary = filterByStrategy(target, pool, strategy, elementResolver);
        Collections.shuffle(primary, random);
        return fillFromPrimary(primary, pool, count);
    }

    private String resolveCode(String elementId, Function<String, CandidateMetadata> elementResolver) {
        if (elementResolver == null) return null;
        CandidateMetadata meta = elementResolver.apply(elementId);
        return meta != null ? meta.code() : null;
    }

    private List<String> fillFromPrimary(List<String> primary, List<String> pool, int count) {
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
