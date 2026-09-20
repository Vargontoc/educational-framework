package es.vargontoc.educational.framework.game.service;

import es.vargontoc.educational.framework.game.model.enums.RecognitionCategory;
import es.vargontoc.educational.framework.game.model.recognition.CandidateMetadata;
import es.vargontoc.educational.framework.game.model.recognition.DistractorStrategy;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DistractorSelectorAnimalTest {

    private static final Set<String> BULL_MATES = Set.of("cow", "deer", "goat");
    private static final List<String> POOL = List.of(
            "bull", "cow", "deer", "goat", "donkey", "horse", "duck", "goose", "chicken", "sheep", "cat", "dog", "pig");

    private final AnimalGroupService animals = AnimalGroupService.fromSeed();

    /** Candidate ids are numeric strings like the real ones; the animal is identified by its code. */
    private Map<String, CandidateMetadata> metadata(List<String> codes) {
        Map<String, CandidateMetadata> metadata = new HashMap<>();
        for (int i = 0; i < codes.size(); i++) {
            metadata.put(String.valueOf(i), new CandidateMetadata(String.valueOf(i), 1L, null, codes.get(i)));
        }
        return metadata;
    }

    private List<String> ids(int size) {
        return IntStream.range(0, size).mapToObj(String::valueOf).toList();
    }

    private List<String> select(long seed, List<String> codes, String targetCode, DistractorStrategy strategy, int count) {
        DistractorSelector selector = new DistractorSelector(new Random(seed), null, null, null, animals);
        Map<String, CandidateMetadata> metadata = metadata(codes);
        String targetId = String.valueOf(codes.indexOf(targetCode));
        List<String> selectedIds = selector.select(targetId, ids(codes.size()), strategy, count,
                RecognitionCategory.ANIMAL, metadata::get);
        return selectedIds.stream().map(id -> codes.get(Integer.parseInt(id))).toList();
    }

    @Test
    void easy_excludesAnimalsOfTheSameGroup() {
        for (long seed = 0; seed < 50; seed++) {
            List<String> result = select(seed, POOL, "bull", DistractorStrategy.SEMANTICALLY_FAR, 1);

            assertEquals(1, result.size());
            assertFalse(BULL_MATES.contains(result.get(0)), "seed " + seed + ": " + result);
            assertFalse(result.contains("bull"));
        }
    }

    @Test
    void medium_excludesAnimalsOfTheSameGroup() {
        for (long seed = 0; seed < 50; seed++) {
            List<String> result = select(seed, POOL, "bull", DistractorStrategy.SAME_CATEGORY, 2);

            assertEquals(2, result.size());
            String context = "seed " + seed + ": " + result;
            result.forEach(code -> assertFalse(BULL_MATES.contains(code), context));
            assertEquals(2, Set.copyOf(result).size());
        }
    }

    @Test
    void easyMedium_excludeMatesOfEveryGroupOfTheTarget() {
        // deer: horns + equine
        Set<String> deerMates = Set.of("bull", "cow", "goat", "donkey", "horse");
        for (long seed = 0; seed < 50; seed++) {
            List<String> result = select(seed, POOL, "deer", DistractorStrategy.SAME_CATEGORY, 2);

            result.forEach(code -> assertFalse(deerMates.contains(code), result.toString()));
        }
    }

    @Test
    void hard_prioritisesAnimalsOfTheSameGroup() {
        for (long seed = 0; seed < 50; seed++) {
            List<String> result = select(seed, POOL, "bull", DistractorStrategy.SIMILAR_OUTLINE, 2);

            assertEquals(2, result.size());
            String context = "seed " + seed + ": " + result;
            result.forEach(code -> assertTrue(BULL_MATES.contains(code), context));
        }
    }

    @Test
    void hard_withThreeDistractors_takesAllGroupMates() {
        List<String> result = select(3, POOL, "bull", DistractorStrategy.SIMILAR_OUTLINE, 3);

        assertEquals(BULL_MATES, Set.copyOf(result));
    }

    @Test
    void hard_fallsBackToRandomWhenGroupHasTooFewMembers() {
        // sheep only shares "wool" with goat: one group mate, the rest is completed from the pool
        for (long seed = 0; seed < 30; seed++) {
            List<String> result = select(seed, POOL, "sheep", DistractorStrategy.SIMILAR_OUTLINE, 3);

            assertEquals(3, result.size());
            assertEquals(3, Set.copyOf(result).size());
            assertFalse(result.contains("sheep"));
            assertEquals("goat", result.get(0), "the only group mate must come first");
        }
    }

    @Test
    void hard_targetWithoutGroups_isRandomFromThePool() {
        List<String> result = select(1, List.of("bee", "cat", "dog", "pig", "cow"), "bee",
                DistractorStrategy.SIMILAR_OUTLINE, 3);

        assertEquals(3, result.size());
        assertFalse(result.contains("bee"));
    }

    @Test
    void easy_fallsBackToGroupMatesWhenNotEnoughOtherCandidates() {
        // only group mates and the target are available: the round must still get its distractors
        List<String> result = select(1, List.of("bull", "cow", "goat"), "bull", DistractorStrategy.SEMANTICALLY_FAR, 2);

        assertEquals(Set.of("cow", "goat"), Set.copyOf(result));
    }

    @Test
    void doesNotReturnMoreThanTheAvailableCandidates() {
        List<String> result = select(1, List.of("bull", "cat"), "bull", DistractorStrategy.SAME_CATEGORY, 3);

        assertEquals(List.of("cat"), result);
    }

    @Test
    void withoutAnimalService_usesExistingStrategyLogic() {
        DistractorSelector selector = new DistractorSelector(new Random(1));
        Map<String, CandidateMetadata> metadata = metadata(POOL);

        List<String> result = selector.select("0", ids(POOL.size()), DistractorStrategy.SEMANTICALLY_FAR, 2,
                RecognitionCategory.ANIMAL, metadata::get);

        assertEquals(2, result.size());
        assertFalse(result.contains("0"));
    }

    @Test
    void unknownTargetCode_fallsBackToExistingStrategyLogic() {
        List<String> result = select(1, List.of("dragon", "cat", "dog", "pig"), "dragon",
                DistractorStrategy.SEMANTICALLY_FAR, 2);

        assertEquals(2, result.size());
        assertFalse(result.contains("dragon"));
    }
}
