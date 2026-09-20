package es.vargontoc.educational.framework.game.service;

import es.vargontoc.educational.framework.game.infrastructure.persistence.RecognitionSimilarityPairJpaEntity;
import es.vargontoc.educational.framework.game.infrastructure.persistence.RecognitionSimilarityPairJpaRepository;
import es.vargontoc.educational.framework.game.model.enums.RecognitionCategory;
import es.vargontoc.educational.framework.game.model.enums.SimilarityStrength;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service that provides recognition element similarity lookups based on a preloaded table of similarity pairs.
 * Supports multiple categories (LETTER, NUMBER) with category-specific similarity tables.
 * Pairs are bidirectional: if (A, B) is in the table, both A excludes B and B excludes A.
 */
public class RecognitionSimilarityService {

    private static final Logger log = LoggerFactory.getLogger(RecognitionSimilarityService.class);

    private final RecognitionSimilarityPairJpaRepository repository;

    // Cache: category -> (elementCode -> set of paired element codes, all strengths)
    private Map<RecognitionCategory, Map<String, Set<String>>> allPairsByCategoryAndElement;
    // Cache: category -> (elementCode -> list of pairs grouped by strength)
    private Map<RecognitionCategory, Map<String, Map<SimilarityStrength, List<String>>>> pairsByCategoryElementAndStrength;

    public RecognitionSimilarityService(RecognitionSimilarityPairJpaRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void init() {
        loadCache();
    }

    private void loadCache() {
        List<RecognitionSimilarityPairJpaEntity> allPairs = repository.findAll();
        log.info("Loading {} recognition similarity pairs into cache", allPairs.size());

        allPairsByCategoryAndElement = new EnumMap<>(RecognitionCategory.class);
        pairsByCategoryElementAndStrength = new EnumMap<>(RecognitionCategory.class);

        for (RecognitionSimilarityPairJpaEntity pair : allPairs) {
            RecognitionCategory category;
            try {
                category = RecognitionCategory.valueOf(pair.getCategory());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid category '{}' for pair ({}, {}), skipping", pair.getCategory(), pair.getElementCodeA(), pair.getElementCodeB());
                continue;
            }

            String codeA = pair.getElementCodeA();
            String codeB = pair.getElementCodeB();
            SimilarityStrength strength;
            try {
                strength = SimilarityStrength.valueOf(pair.getStrength());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid strength '{}' for pair ({}, {}), skipping", pair.getStrength(), codeA, codeB);
                continue;
            }

            // Initialize category maps if needed
            allPairsByCategoryAndElement.computeIfAbsent(category, k -> new HashMap<>());
            pairsByCategoryElementAndStrength.computeIfAbsent(category, k -> new HashMap<>());

            Map<String, Set<String>> categoryPairs = allPairsByCategoryAndElement.get(category);
            Map<String, Map<SimilarityStrength, List<String>>> categoryPairsByStrength = pairsByCategoryElementAndStrength.get(category);

            // Bidirectional: A -> B
            categoryPairs.computeIfAbsent(codeA, k -> new HashSet<>()).add(codeB);
            categoryPairsByStrength
                    .computeIfAbsent(codeA, k -> new EnumMap<>(SimilarityStrength.class))
                    .computeIfAbsent(strength, k -> new ArrayList<>())
                    .add(codeB);

            // Bidirectional: B -> A
            categoryPairs.computeIfAbsent(codeB, k -> new HashSet<>()).add(codeA);
            categoryPairsByStrength
                    .computeIfAbsent(codeB, k -> new EnumMap<>(SimilarityStrength.class))
                    .computeIfAbsent(strength, k -> new ArrayList<>())
                    .add(codeA);
        }

        int totalCategories = allPairsByCategoryAndElement.size();
        int totalElements = allPairsByCategoryAndElement.values().stream().mapToInt(Map::size).sum();
        log.info("Recognition similarity cache loaded. {} categories, {} elements indexed.", totalCategories, totalElements);
    }

    /**
     * Returns all element codes that appear in any similarity pair with the given target element code
     * for the specified category.
     * Used by EASY and MEDIUM difficulties to exclude similar elements from random selection.
     *
     * @param category the recognition category (LETTER, NUMBER, etc.)
     * @param targetElementCode the element code (e.g., "letter_o", "number_6")
     * @return set of element codes that should be excluded as distractors
     */
    public Set<String> getExcludedElements(RecognitionCategory category, String targetElementCode) {
        if (category == null || targetElementCode == null) {
            return Collections.emptySet();
        }
        Map<String, Set<String>> categoryPairs = allPairsByCategoryAndElement.get(category);
        if (categoryPairs == null) {
            return Collections.emptySet();
        }
        Set<String> excluded = categoryPairs.get(targetElementCode);
        return excluded != null ? Collections.unmodifiableSet(excluded) : Collections.emptySet();
    }

    /**
     * Returns a list of similar element codes ordered by strength priority: STRONG first, then MODERATE, then WEAK.
     * Used by HARD difficulty to select similar elements as distractors.
     *
     * @param category the recognition category (LETTER, NUMBER, etc.)
     * @param targetElementCode the target element code
     * @param count maximum number of similar elements to return
     * @return ordered list of similar element codes (may be smaller than count if not enough exist)
     */
    public List<String> getSimilarElements(RecognitionCategory category, String targetElementCode, int count) {
        if (category == null || targetElementCode == null || count <= 0) {
            return Collections.emptyList();
        }

        Map<String, Map<SimilarityStrength, List<String>>> categoryPairsByStrength = pairsByCategoryElementAndStrength.get(category);
        if (categoryPairsByStrength == null) {
            return Collections.emptyList();
        }

        Map<SimilarityStrength, List<String>> byStrength = categoryPairsByStrength.get(targetElementCode);
        if (byStrength == null || byStrength.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<>();

        // Priority: STRONG first
        List<String> strong = byStrength.get(SimilarityStrength.STRONG);
        if (strong != null) {
            result.addAll(strong);
        }

        // Then MODERATE
        if (result.size() < count) {
            List<String> moderate = byStrength.get(SimilarityStrength.MODERATE);
            if (moderate != null) {
                result.addAll(moderate);
            }
        }

        // Then WEAK
        if (result.size() < count) {
            List<String> weak = byStrength.get(SimilarityStrength.WEAK);
            if (weak != null) {
                result.addAll(weak);
            }
        }

        // Trim to requested count
        if (result.size() > count) {
            return result.subList(0, count);
        }

        return result;
    }

    /**
     * Checks whether the given element code has any entries in the similarity table for the specified category.
     */
    public boolean hasSimilarityEntries(RecognitionCategory category, String elementCode) {
        if (category == null || elementCode == null) {
            return false;
        }
        Map<String, Set<String>> categoryPairs = allPairsByCategoryAndElement.get(category);
        return categoryPairs != null && categoryPairs.containsKey(elementCode);
    }
}
