package es.vargontoc.educational.framework.game.service;

import es.vargontoc.educational.framework.game.infrastructure.persistence.LetterSimilarityPairJpaEntity;
import es.vargontoc.educational.framework.game.infrastructure.persistence.LetterSimilarityPairJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LetterSimilarityServiceTest {

    private LetterSimilarityPairJpaRepository repository;
    private LetterSimilarityService service;

    @BeforeEach
    void setUp() {
        repository = mock(LetterSimilarityPairJpaRepository.class);
    }

    private void initWithPairs(List<LetterSimilarityPairJpaEntity> pairs) {
        when(repository.findAll()).thenReturn(pairs);
        service = new LetterSimilarityService(repository);
        service.init();
    }

    private LetterSimilarityPairJpaEntity pair(String a, String b, String strength) {
        return new LetterSimilarityPairJpaEntity(a, b, strength);
    }

    @Test
    void getExcludedLetters_returnsAllPairedLetters() {
        initWithPairs(List.of(
                pair("letter_o", "letter_q", "STRONG"),
                pair("letter_o", "letter_c", "STRONG"),
                pair("letter_o", "letter_d", "MODERATE"),
                pair("letter_c", "letter_g", "MODERATE")
        ));

        Set<String> excluded = service.getExcludedLetters("letter_o");

        assertEquals(3, excluded.size());
        assertTrue(excluded.contains("letter_q"));
        assertTrue(excluded.contains("letter_c"));
        assertTrue(excluded.contains("letter_d"));
    }

    @Test
    void getExcludedLetters_bidirectional() {
        initWithPairs(List.of(
                pair("letter_o", "letter_q", "STRONG")
        ));

        Set<String> excludedFromQ = service.getExcludedLetters("letter_q");

        assertEquals(1, excludedFromQ.size());
        assertTrue(excludedFromQ.contains("letter_o"));
    }

    @Test
    void getExcludedLetters_unknownLetterReturnsEmpty() {
        initWithPairs(List.of(
                pair("letter_o", "letter_q", "STRONG")
        ));

        Set<String> excluded = service.getExcludedLetters("letter_z");

        assertTrue(excluded.isEmpty());
    }

    @Test
    void getExcludedLetters_nullReturnsEmpty() {
        initWithPairs(List.of());

        Set<String> excluded = service.getExcludedLetters(null);

        assertTrue(excluded.isEmpty());
    }

    @Test
    void getSimilarLetters_strongFirst() {
        initWithPairs(List.of(
                pair("letter_o", "letter_q", "STRONG"),
                pair("letter_o", "letter_c", "STRONG"),
                pair("letter_o", "letter_d", "MODERATE"),
                pair("letter_o", "letter_z", "WEAK")
        ));

        List<String> similar = service.getSimilarLetters("letter_o", 2);

        assertEquals(2, similar.size());
        // Strong ones should come first
        assertTrue(similar.contains("letter_q"));
        assertTrue(similar.contains("letter_c"));
    }

    @Test
    void getSimilarLetters_priorityOrder_strongThenModerateThenWeak() {
        initWithPairs(List.of(
                pair("letter_o", "letter_q", "STRONG"),
                pair("letter_o", "letter_d", "MODERATE"),
                pair("letter_o", "letter_z", "WEAK")
        ));

        List<String> similar = service.getSimilarLetters("letter_o", 3);

        assertEquals(3, similar.size());
        assertEquals("letter_q", similar.get(0));   // STRONG first
        assertEquals("letter_d", similar.get(1));   // MODERATE second
        assertEquals("letter_z", similar.get(2));   // WEAK third
    }

    @Test
    void getSimilarLetters_fewerAvailableThanRequested() {
        initWithPairs(List.of(
                pair("letter_o", "letter_q", "STRONG")
        ));

        List<String> similar = service.getSimilarLetters("letter_o", 3);

        assertEquals(1, similar.size());
        assertEquals("letter_q", similar.get(0));
    }

    @Test
    void getSimilarLetters_unknownLetterReturnsEmpty() {
        initWithPairs(List.of(
                pair("letter_o", "letter_q", "STRONG")
        ));

        List<String> similar = service.getSimilarLetters("letter_z", 2);

        assertTrue(similar.isEmpty());
    }

    @Test
    void hasSimilarityEntries_trueForKnownLetter() {
        initWithPairs(List.of(
                pair("letter_o", "letter_q", "STRONG")
        ));

        assertTrue(service.hasSimilarityEntries("letter_o"));
        assertTrue(service.hasSimilarityEntries("letter_q"));
    }

    @Test
    void hasSimilarityEntries_falseForUnknownLetter() {
        initWithPairs(List.of(
                pair("letter_o", "letter_q", "STRONG")
        ));

        assertFalse(service.hasSimilarityEntries("letter_z"));
    }

    @Test
    void getExcludedLetters_fullTableForLetterO() {
        // Simulate the actual seed data for letter_o
        initWithPairs(List.of(
                pair("letter_o", "letter_q", "STRONG"),
                pair("letter_o", "letter_c", "STRONG"),
                pair("letter_c", "letter_g", "MODERATE"),
                pair("letter_g", "letter_q", "MODERATE"),
                pair("letter_o", "letter_d", "MODERATE"),
                pair("letter_d", "letter_q", "MODERATE")
        ));

        Set<String> excluded = service.getExcludedLetters("letter_o");

        // letter_o appears in pairs with: q, c, d
        assertEquals(3, excluded.size());
        assertTrue(excluded.contains("letter_q"));
        assertTrue(excluded.contains("letter_c"));
        assertTrue(excluded.contains("letter_d"));
    }
}
