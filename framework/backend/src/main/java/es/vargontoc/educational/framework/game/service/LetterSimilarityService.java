package es.vargontoc.educational.framework.game.service;

import es.vargontoc.educational.framework.game.infrastructure.persistence.LetterSimilarityPairJpaEntity;
import es.vargontoc.educational.framework.game.infrastructure.persistence.LetterSimilarityPairJpaRepository;
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
 * Service that provides letter similarity lookups based on a preloaded table of similarity pairs.
 * Pairs are bidirectional: if (A, B) is in the table, both A excludes B and B excludes A.
 */
public class LetterSimilarityService {

    private static final Logger log = LoggerFactory.getLogger(LetterSimilarityService.class);

    private final LetterSimilarityPairJpaRepository repository;

    // Cache: letterCode -> set of paired letter codes (all strengths)
    private Map<String, Set<String>> allPairsByLetter;
    // Cache: letterCode -> list of pairs grouped by strength
    private Map<String, Map<SimilarityStrength, List<String>>> pairsByLetterAndStrength;

    public LetterSimilarityService(LetterSimilarityPairJpaRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void init() {
        loadCache();
    }

    private void loadCache() {
        List<LetterSimilarityPairJpaEntity> allPairs = repository.findAll();
        log.info("Loading {} letter similarity pairs into cache", allPairs.size());

        allPairsByLetter = new HashMap<>();
        pairsByLetterAndStrength = new HashMap<>();

        for (LetterSimilarityPairJpaEntity pair : allPairs) {
            String codeA = pair.getLetterCodeA();
            String codeB = pair.getLetterCodeB();
            SimilarityStrength strength;
            try {
                strength = SimilarityStrength.valueOf(pair.getStrength());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid strength '{}' for pair ({}, {}), skipping", pair.getStrength(), codeA, codeB);
                continue;
            }

            // Bidirectional: A -> B
            allPairsByLetter.computeIfAbsent(codeA, k -> new HashSet<>()).add(codeB);
            pairsByLetterAndStrength
                    .computeIfAbsent(codeA, k -> new EnumMap<>(SimilarityStrength.class))
                    .computeIfAbsent(strength, k -> new ArrayList<>())
                    .add(codeB);

            // Bidirectional: B -> A
            allPairsByLetter.computeIfAbsent(codeB, k -> new HashSet<>()).add(codeA);
            pairsByLetterAndStrength
                    .computeIfAbsent(codeB, k -> new EnumMap<>(SimilarityStrength.class))
                    .computeIfAbsent(strength, k -> new ArrayList<>())
                    .add(codeA);
        }

        log.info("Letter similarity cache loaded. {} letters indexed.", allPairsByLetter.size());
    }

    /**
     * Returns all letter codes that appear in any similarity pair with the given target letter code.
     * Used by EASY and MEDIUM difficulties to exclude similar letters from random selection.
     *
     * @param targetLetterCode the letter code (e.g., "letter_o")
     * @return set of letter codes that should be excluded as distractors
     */
    public Set<String> getExcludedLetters(String targetLetterCode) {
        if (targetLetterCode == null) {
            return Collections.emptySet();
        }
        Set<String> excluded = allPairsByLetter.get(targetLetterCode);
        return excluded != null ? Collections.unmodifiableSet(excluded) : Collections.emptySet();
    }

    /**
     * Returns a list of similar letter codes ordered by strength priority: STRONG first, then MODERATE, then WEAK.
     * Used by HARD difficulty to select similar letters as distractors.
     *
     * @param targetLetterCode the target letter code
     * @param count            maximum number of similar letters to return
     * @return ordered list of similar letter codes (may be smaller than count if not enough exist)
     */
    public List<String> getSimilarLetters(String targetLetterCode, int count) {
        if (targetLetterCode == null || count <= 0) {
            return Collections.emptyList();
        }

        Map<SimilarityStrength, List<String>> byStrength = pairsByLetterAndStrength.get(targetLetterCode);
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
     * Checks whether the given letter code has any entries in the similarity table.
     */
    public boolean hasSimilarityEntries(String letterCode) {
        return letterCode != null && allPairsByLetter.containsKey(letterCode);
    }
}
