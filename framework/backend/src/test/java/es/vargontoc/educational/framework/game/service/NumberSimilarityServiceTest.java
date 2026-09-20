package es.vargontoc.educational.framework.game.service;

import es.vargontoc.educational.framework.game.infrastructure.persistence.RecognitionSimilarityPairJpaEntity;
import es.vargontoc.educational.framework.game.infrastructure.persistence.RecognitionSimilarityPairJpaRepository;
import es.vargontoc.educational.framework.game.model.enums.RecognitionCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NumberSimilarityServiceTest {

    private RecognitionSimilarityPairJpaRepository repository;
    private RecognitionSimilarityService service;

    @BeforeEach
    void setUp() {
        repository = mock(RecognitionSimilarityPairJpaRepository.class);
    }

    private void initWithPairs(List<RecognitionSimilarityPairJpaEntity> pairs) {
        when(repository.findAll()).thenReturn(pairs);
        service = new RecognitionSimilarityService(repository);
        service.init();
    }

    private RecognitionSimilarityPairJpaEntity pair(String a, String b, String strength) {
        return new RecognitionSimilarityPairJpaEntity("NUMBER", a, b, strength);
    }

    @Test
    void getExcludedElements_number_returnsAllPairedNumbers() {
        initWithPairs(List.of(
                pair("number_6", "number_9", "STRONG"),
                pair("number_0", "number_9", "WEAK"),
                pair("number_3", "number_8", "STRONG")
        ));

        Set<String> excluded = service.getExcludedElements(RecognitionCategory.NUMBER, "number_9");

        assertEquals(2, excluded.size());
        assertTrue(excluded.contains("number_6"));
        assertTrue(excluded.contains("number_0"));
    }

    @Test
    void getExcludedElements_number_bidirectional() {
        initWithPairs(List.of(
                pair("number_6", "number_9", "STRONG")
        ));

        Set<String> excludedFrom6 = service.getExcludedElements(RecognitionCategory.NUMBER, "number_6");
        Set<String> excludedFrom9 = service.getExcludedElements(RecognitionCategory.NUMBER, "number_9");

        assertEquals(1, excludedFrom6.size());
        assertTrue(excludedFrom6.contains("number_9"));
        assertEquals(1, excludedFrom9.size());
        assertTrue(excludedFrom9.contains("number_6"));
    }

    @Test
    void getExcludedElements_number_unknownNumberReturnsEmpty() {
        initWithPairs(List.of(
                pair("number_6", "number_9", "STRONG")
        ));

        Set<String> excluded = service.getExcludedElements(RecognitionCategory.NUMBER, "number_4");

        assertTrue(excluded.isEmpty());
    }

    @Test
    void getSimilarElements_number_strongFirst() {
        initWithPairs(List.of(
                pair("number_6", "number_9", "STRONG"),
                pair("number_6", "number_0", "WEAK"),
                pair("number_1", "number_7", "MODERATE")
        ));

        // number_6 has: STRONG -> number_9, WEAK -> number_0
        List<String> similar = service.getSimilarElements(RecognitionCategory.NUMBER, "number_6", 2);

        assertEquals(2, similar.size());
        assertEquals("number_9", similar.get(0)); // STRONG first
        assertEquals("number_0", similar.get(1)); // WEAK second (only option)
    }

    @Test
    void getSimilarElements_number_priorityOrder() {
        initWithPairs(List.of(
                pair("number_6", "number_9", "STRONG"),
                pair("number_0", "number_6", "MODERATE"),
                pair("number_6", "number_2", "WEAK")
        ));

        List<String> similar = service.getSimilarElements(RecognitionCategory.NUMBER, "number_6", 3);

        assertEquals(3, similar.size());
        assertEquals("number_9", similar.get(0)); // STRONG first
        assertEquals("number_0", similar.get(1)); // MODERATE second
        assertEquals("number_2", similar.get(2)); // WEAK third
    }

    @Test
    void getSimilarElements_number_fewerAvailableThanRequested() {
        initWithPairs(List.of(
                pair("number_6", "number_9", "STRONG")
        ));

        List<String> similar = service.getSimilarElements(RecognitionCategory.NUMBER, "number_6", 3);

        assertEquals(1, similar.size());
        assertEquals("number_9", similar.get(0));
    }

    @Test
    void hasSimilarityEntries_number_trueForKnownNumber() {
        initWithPairs(List.of(
                pair("number_6", "number_9", "STRONG")
        ));

        assertTrue(service.hasSimilarityEntries(RecognitionCategory.NUMBER, "number_6"));
        assertTrue(service.hasSimilarityEntries(RecognitionCategory.NUMBER, "number_9"));
    }

    @Test
    void hasSimilarityEntries_number_falseForUnknownNumber() {
        initWithPairs(List.of(
                pair("number_6", "number_9", "STRONG")
        ));

        assertFalse(service.hasSimilarityEntries(RecognitionCategory.NUMBER, "number_4"));
    }

    @Test
    void categoryIsolation_letterPairsDoNotAffectNumbers() {
        // Mix of LETTER and NUMBER pairs
        List<RecognitionSimilarityPairJpaEntity> mixedPairs = List.of(
                new RecognitionSimilarityPairJpaEntity("LETTER", "letter_a", "letter_b", "STRONG"),
                new RecognitionSimilarityPairJpaEntity("NUMBER", "number_6", "number_9", "STRONG")
        );
        initWithPairs(mixedPairs);

        // LETTER category should only see letter pairs
        Set<String> letterExcluded = service.getExcludedElements(RecognitionCategory.LETTER, "letter_a");
        assertEquals(1, letterExcluded.size());
        assertTrue(letterExcluded.contains("letter_b"));

        // NUMBER category should only see number pairs
        Set<String> numberExcluded = service.getExcludedElements(RecognitionCategory.NUMBER, "number_6");
        assertEquals(1, numberExcluded.size());
        assertTrue(numberExcluded.contains("number_9"));

        // Cross-category should not find anything
        Set<String> crossExcluded = service.getExcludedElements(RecognitionCategory.NUMBER, "letter_a");
        assertTrue(crossExcluded.isEmpty());
    }
}
