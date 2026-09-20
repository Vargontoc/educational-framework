package es.vargontoc.educational.framework.game.service;

import es.vargontoc.educational.framework.game.infrastructure.persistence.LetterSimilarityPairJpaEntity;
import es.vargontoc.educational.framework.game.infrastructure.persistence.LetterSimilarityPairJpaRepository;
import es.vargontoc.educational.framework.game.model.enums.RecognitionCategory;
import es.vargontoc.educational.framework.game.model.recognition.CandidateMetadata;
import es.vargontoc.educational.framework.game.model.recognition.DistractorStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DistractorSelectorLetterSimilarityTest {

    private LetterSimilarityService letterSimilarityService;
    private DistractorSelector selector;

    @BeforeEach
    void setUp() {
        LetterSimilarityPairJpaRepository repository = mock(LetterSimilarityPairJpaRepository.class);
        when(repository.findAll()).thenReturn(List.of(
                new LetterSimilarityPairJpaEntity("letter_o", "letter_q", "STRONG"),
                new LetterSimilarityPairJpaEntity("letter_o", "letter_c", "STRONG"),
                new LetterSimilarityPairJpaEntity("letter_o", "letter_d", "MODERATE"),
                new LetterSimilarityPairJpaEntity("letter_c", "letter_g", "MODERATE"),
                new LetterSimilarityPairJpaEntity("letter_g", "letter_q", "MODERATE"),
                new LetterSimilarityPairJpaEntity("letter_d", "letter_q", "MODERATE"),
                new LetterSimilarityPairJpaEntity("letter_i", "letter_l", "STRONG"),
                new LetterSimilarityPairJpaEntity("letter_l", "letter_t", "STRONG"),
                new LetterSimilarityPairJpaEntity("letter_i", "letter_t", "MODERATE"),
                new LetterSimilarityPairJpaEntity("letter_n", "letter_z", "WEAK")
        ));
        letterSimilarityService = new LetterSimilarityService(repository);
        letterSimilarityService.init();
        selector = new DistractorSelector(new Random(42), letterSimilarityService);
    }

    private CandidateMetadata letterMeta(String code) {
        return new CandidateMetadata(code, 1L, null, code);
    }

    private Function<String, CandidateMetadata> letterResolver() {
        Map<String, CandidateMetadata> map = new HashMap<>();
        for (String code : List.of("letter_a", "letter_b", "letter_c", "letter_d", "letter_e",
                "letter_f", "letter_g", "letter_h", "letter_i", "letter_j", "letter_k",
                "letter_l", "letter_m", "letter_n", "letter_o", "letter_p", "letter_q",
                "letter_r", "letter_s", "letter_t", "letter_u", "letter_v", "letter_w",
                "letter_x", "letter_y", "letter_z")) {
            map.put(code, letterMeta(code));
        }
        return map::get;
    }

    private List<String> allLetterCandidates() {
        return List.of("letter_a", "letter_b", "letter_c", "letter_d", "letter_e",
                "letter_f", "letter_g", "letter_h", "letter_i", "letter_j", "letter_k",
                "letter_l", "letter_m", "letter_n", "letter_o", "letter_p", "letter_q",
                "letter_r", "letter_s", "letter_t", "letter_u", "letter_v", "letter_w",
                "letter_x", "letter_y", "letter_z");
    }

    // --- EASY: SEMANTICALLY_FAR for LETTER should exclude similarity pairs ---

    @Test
    void easy_letter_excludesSimilarLettersFromTarget() {
        List<String> candidates = allLetterCandidates();
        Function<String, CandidateMetadata> resolver = letterResolver();

        List<String> result = selector.select(
                "letter_o", candidates, DistractorStrategy.SEMANTICALLY_FAR, 1,
                RecognitionCategory.LETTER, resolver);

        assertEquals(1, result.size());
        // letter_o has similarity pairs with: letter_q, letter_c, letter_d
        assertFalse(result.contains("letter_q"));
        assertFalse(result.contains("letter_c"));
        assertFalse(result.contains("letter_d"));
        // Should not contain target
        assertFalse(result.contains("letter_o"));
    }

    // --- MEDIUM: SAME_CATEGORY for LETTER should also exclude similarity pairs ---

    @Test
    void medium_letter_excludesSimilarLettersFromTarget() {
        List<String> candidates = allLetterCandidates();
        Function<String, CandidateMetadata> resolver = letterResolver();

        List<String> result = selector.select(
                "letter_o", candidates, DistractorStrategy.SAME_CATEGORY, 2,
                RecognitionCategory.LETTER, resolver);

        assertEquals(2, result.size());
        // letter_o has similarity pairs with: letter_q, letter_c, letter_d
        assertFalse(result.contains("letter_q"));
        assertFalse(result.contains("letter_c"));
        assertFalse(result.contains("letter_d"));
        assertFalse(result.contains("letter_o"));
    }

    // --- HARD: SIMILAR_OUTLINE for LETTER should use similarity table ---

    @Test
    void hard_letter_usesSimilarityTableWithStrongPriority() {
        List<String> candidates = allLetterCandidates();
        Function<String, CandidateMetadata> resolver = letterResolver();

        List<String> result = selector.select(
                "letter_o", candidates, DistractorStrategy.SIMILAR_OUTLINE, 2,
                RecognitionCategory.LETTER, resolver);

        assertEquals(2, result.size());
        // letter_o has STRONG pairs with: letter_q, letter_c
        // Both should be selected as they are the highest priority
        assertTrue(result.contains("letter_q"));
        assertTrue(result.contains("letter_c"));
    }

    @Test
    void hard_letter_fallsToModerateWhenNotEnoughStrong() {
        // letter_i has 1 STRONG (letter_l) and 1 MODERATE (letter_t)
        List<String> candidates = allLetterCandidates();
        Function<String, CandidateMetadata> resolver = letterResolver();

        List<String> result = selector.select(
                "letter_i", candidates, DistractorStrategy.SIMILAR_OUTLINE, 2,
                RecognitionCategory.LETTER, resolver);

        assertEquals(2, result.size());
        // letter_i: STRONG -> letter_l, MODERATE -> letter_t
        assertTrue(result.contains("letter_l"));
        assertTrue(result.contains("letter_t"));
    }

    @Test
    void hard_letter_fallsBackToRandomWhenNotEnoughInTable() {
        // letter_n has only 1 pair in table: letter_z (WEAK)
        // Requesting 2 distractors should fallback to random for the second
        List<String> candidates = allLetterCandidates();
        Function<String, CandidateMetadata> resolver = letterResolver();

        List<String> result = selector.select(
                "letter_n", candidates, DistractorStrategy.SIMILAR_OUTLINE, 2,
                RecognitionCategory.LETTER, resolver);

        assertEquals(2, result.size());
        // letter_z should be in the result (from table)
        assertTrue(result.contains("letter_z"));
        assertFalse(result.contains("letter_n")); // not target
    }

    // --- Non-letter category should not use similarity table ---

    @Test
    void nonLetterCategory_doesNotUseSimilarityTable() {
        List<String> candidates = allLetterCandidates();
        Function<String, CandidateMetadata> resolver = letterResolver();

        // Even though letter_o has similarity pairs, with NUMBER category it should not use them
        List<String> result = selector.select(
                "letter_o", candidates, DistractorStrategy.SEMANTICALLY_FAR, 1,
                RecognitionCategory.NUMBER, resolver);

        assertEquals(1, result.size());
        // Could be any candidate including similarity pairs since it's not LETTER category
        assertFalse(result.contains("letter_o"));
    }

    // --- Null category should behave like before ---

    @Test
    void nullCategory_behavesLikeOriginal() {
        List<String> candidates = List.of("target", "a", "b", "c");
        Function<String, CandidateMetadata> resolver = id -> null;

        List<String> result = selector.select(
                "target", candidates, DistractorStrategy.SEMANTICALLY_FAR, 2,
                null, resolver);

        assertEquals(2, result.size());
        assertFalse(result.contains("target"));
    }

    // --- No service available: backward compatibility ---

    @Test
    void noService_available_backwardCompatible() {
        DistractorSelector selectorNoService = new DistractorSelector(new Random(42));
        List<String> candidates = List.of("target", "a", "b", "c");
        Function<String, CandidateMetadata> resolver = id -> null;

        List<String> result = selectorNoService.select(
                "target", candidates, DistractorStrategy.SEMANTICALLY_FAR, 2,
                RecognitionCategory.LETTER, resolver);

        assertEquals(2, result.size());
        assertFalse(result.contains("target"));
    }

    // --- Fallback when target has no similarity entries ---

    @Test
    void hard_letter_noSimilarityEntries_fallsToSimilarityGroupOrRandom() {
        // letter_a has no entries in the similarity table
        List<String> candidates = allLetterCandidates();
        Function<String, CandidateMetadata> resolver = letterResolver();

        List<String> result = selector.select(
                "letter_a", candidates, DistractorStrategy.SIMILAR_OUTLINE, 2,
                RecognitionCategory.LETTER, resolver);

        assertEquals(2, result.size());
        assertFalse(result.contains("letter_a"));
    }
}
