package es.vargontoc.educational.framework.game.service;

import es.vargontoc.educational.framework.game.model.recognition.CandidateMetadata;
import es.vargontoc.educational.framework.game.model.recognition.DistractorStrategy;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DistractorSelectorTest {

    private final DistractorSelector selector = new DistractorSelector(new Random(42));

    private CandidateMetadata element(Long topicId, String similarityGroup) {
        return new CandidateMetadata("unused", topicId, similarityGroup);
    }

    private Function<String, CandidateMetadata> resolverOf(Map<String, CandidateMetadata> elements) {
        return elements::get;
    }

    @Test
    void select_semanticallyFar_ignoresElementResolverAndReturnsFromCandidates() {
        List<String> candidates = List.of("target", "a", "b", "c");
        Function<String, CandidateMetadata> resolver = id -> null;

        List<String> result = selector.select("target", candidates, DistractorStrategy.SEMANTICALLY_FAR, 2, resolver);

        assertEquals(2, result.size());
        assertFalse(result.contains("target"));
        assertTrue(candidates.containsAll(result));
    }

    @Test
    void select_sameCategory_returnsOnlyMatchingTopicId() {
        Map<String, CandidateMetadata> elements = new HashMap<>();
        elements.put("target", element(1L, null));
        elements.put("sameTopicA", element(1L, null));
        elements.put("sameTopicB", element(1L, null));
        elements.put("otherTopic", element(2L, null));
        List<String> candidates = List.of("target", "sameTopicA", "sameTopicB", "otherTopic");

        List<String> result = selector.select(
                "target", candidates, DistractorStrategy.SAME_CATEGORY, 2, resolverOf(elements));

        assertEquals(2, result.size());
        assertTrue(result.contains("sameTopicA"));
        assertTrue(result.contains("sameTopicB"));
    }

    @Test
    void select_similarOutline_returnsOnlyMatchingSimilarityGroup() {
        Map<String, CandidateMetadata> elements = new HashMap<>();
        elements.put("target", element(1L, "curve_round"));
        elements.put("sameGroup", element(1L, "curve_round"));
        elements.put("differentGroup", element(1L, "angular_peak"));
        elements.put("noGroup", element(1L, null));
        List<String> candidates = List.of("target", "sameGroup", "differentGroup", "noGroup");

        List<String> result = selector.select(
                "target", candidates, DistractorStrategy.SIMILAR_OUTLINE, 1, resolverOf(elements));

        assertEquals(List.of("sameGroup"), result);
    }

    @Test
    void select_similarOutline_nullGroupNeverMatchesAnotherNullGroup() {
        Map<String, CandidateMetadata> elements = new HashMap<>();
        elements.put("target", element(1L, null));
        elements.put("alsoNoGroup", element(1L, null));
        List<String> candidates = List.of("target", "alsoNoGroup");

        List<String> result = selector.select(
                "target", candidates, DistractorStrategy.SIMILAR_OUTLINE, 1, resolverOf(elements));

        assertEquals(List.of("alsoNoGroup"), result);
    }

    @Test
    void select_partialFallback_fillsRemainderFromOtherCandidates() {
        Map<String, CandidateMetadata> elements = new HashMap<>();
        elements.put("target", element(1L, null));
        elements.put("sameTopic", element(1L, null));
        elements.put("otherTopicA", element(2L, null));
        elements.put("otherTopicB", element(2L, null));
        List<String> candidates = List.of("target", "sameTopic", "otherTopicA", "otherTopicB");

        List<String> result = selector.select(
                "target", candidates, DistractorStrategy.SAME_CATEGORY, 3, resolverOf(elements));

        assertEquals(Set.of("sameTopic", "otherTopicA", "otherTopicB"), new HashSet<>(result));
    }

    @Test
    void select_totalFallback_whenNoCandidateMatchesStrategy() {
        Map<String, CandidateMetadata> elements = new HashMap<>();
        elements.put("target", element(1L, "groupA"));
        elements.put("candidateA", element(2L, "groupB"));
        elements.put("candidateB", element(3L, "groupC"));
        List<String> candidates = List.of("target", "candidateA", "candidateB");

        List<String> result = selector.select(
                "target", candidates, DistractorStrategy.SIMILAR_OUTLINE, 2, resolverOf(elements));

        assertEquals(2, result.size());
        assertTrue(candidates.containsAll(result));
    }

    @Test
    void select_exhaustedPool_returnsAvailableWithoutThrowing() {
        Map<String, CandidateMetadata> elements = new HashMap<>();
        elements.put("target", element(1L, null));
        elements.put("onlyOther", element(1L, null));
        List<String> candidates = List.of("target", "onlyOther");

        List<String> result = selector.select(
                "target", candidates, DistractorStrategy.SAME_CATEGORY, 5, resolverOf(elements));

        assertEquals(1, result.size());
        assertEquals("onlyOther", result.get(0));
    }

    @Test
    void select_unresolvableElement_treatedAsNonMatchAndFallsBackToRandomPool() {
        Map<String, CandidateMetadata> elements = new HashMap<>();
        elements.put("target", element(1L, null));
        elements.put("sameTopic", element(1L, null));
        // "unresolvable" intentionally absent from the map -> resolver returns null
        List<String> candidates = List.of("target", "sameTopic", "unresolvable");

        List<String> result = selector.select(
                "target", candidates, DistractorStrategy.SAME_CATEGORY, 2, resolverOf(elements));

        assertEquals(2, result.size());
        assertTrue(candidates.containsAll(result));
    }

    @Test
    void select_similarOutline_colorWarmCoolGrouping() {
        Map<String, CandidateMetadata> elements = new HashMap<>();
        elements.put("color_red", element(1L, "warm"));
        elements.put("color_yellow", element(1L, "warm"));
        elements.put("color_blue", element(1L, "cool"));
        elements.put("color_green", element(1L, "cool"));
        List<String> candidates = List.of("color_red", "color_yellow", "color_blue", "color_green");

        List<String> result = selector.select(
                "color_red", candidates, DistractorStrategy.SIMILAR_OUTLINE, 1, resolverOf(elements));

        assertEquals(List.of("color_yellow"), result);
    }

    @Test
    void select_similarOutline_shapeGroups() {
        Map<String, CandidateMetadata> elements = new HashMap<>();
        elements.put("shape_circle", element(1L, "round"));
        elements.put("shape_oval", element(1L, "round"));
        elements.put("shape_square", element(1L, "angular_quad"));
        elements.put("shape_rectangle", element(1L, "angular_quad"));
        elements.put("shape_triangle", element(1L, "pointed"));
        elements.put("shape_star", element(1L, "pointed"));
        List<String> candidates = List.of(
                "shape_circle", "shape_oval", "shape_square", "shape_rectangle", "shape_triangle", "shape_star");

        List<String> result = selector.select(
                "shape_square", candidates, DistractorStrategy.SIMILAR_OUTLINE, 1, resolverOf(elements));

        assertEquals(List.of("shape_rectangle"), result);
    }
}
