package es.vargontoc.educational.framework.tracking.service;

import es.vargontoc.educational.framework.tracking.model.TopicGroupedByPerformance;
import es.vargontoc.educational.framework.tracking.model.TopicSelectionResult;
import es.vargontoc.educational.framework.tracking.model.DifficultyLevel;
import es.vargontoc.educational.framework.tracking.model.TopicPerformanceBand;
import es.vargontoc.educational.framework.tracking.model.TopicSummary;
import es.vargontoc.educational.framework.tracking.ports.out.TopicSummaryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TopicSelectionServiceTest {

    @Mock
    private TopicSummaryRepository topicSummaryRepository;

    @InjectMocks
    private TopicSelectionService service;

    @Test
    void classifyTopics_returnsEmptyMap_whenNoTopics() {
        when(topicSummaryRepository.findByChildProfileId(1L)).thenReturn(List.of());

        TopicGroupedByPerformance result = service.classifyTopics(1L);

        assertTrue(result.getTopics(TopicPerformanceBand.WEAK).isEmpty());
        assertTrue(result.getTopics(TopicPerformanceBand.MEDIUM).isEmpty());
        assertTrue(result.getTopics(TopicPerformanceBand.STRONG).isEmpty());
    }

    @Test
    void classifyTopics_groupsTopicsByPerformanceBand() {
        var weak = createTopicSummary(1L, 1L, TopicPerformanceBand.WEAK);
        var medium = createTopicSummary(2L, 2L, TopicPerformanceBand.MEDIUM);
        var strong = createTopicSummary(3L, 3L, TopicPerformanceBand.STRONG);

        when(topicSummaryRepository.findByChildProfileId(1L))
                .thenReturn(List.of(weak, medium, strong));

        TopicGroupedByPerformance result = service.classifyTopics(1L);

        assertEquals(1, result.getTopics(TopicPerformanceBand.WEAK).size());
        assertEquals(1L, result.getTopics(TopicPerformanceBand.WEAK).get(0));
        assertEquals(1, result.getTopics(TopicPerformanceBand.MEDIUM).size());
        assertEquals(2L, result.getTopics(TopicPerformanceBand.MEDIUM).get(0));
        assertEquals(1, result.getTopics(TopicPerformanceBand.STRONG).size());
        assertEquals(3L, result.getTopics(TopicPerformanceBand.STRONG).get(0));
    }

    @Test
    void selectTopicsForDifficulty_EASY_returnsCorrectDistribution() {
        when(topicSummaryRepository.findByChildProfileId(1L))
                .thenReturn(createTopicsWithBands(10, 10, 10));

        TopicSelectionResult result = service.selectTopicsForDifficulty(1L, DifficultyLevel.EASY, 10);

        assertEquals(10, result.getSelectedTopicIds().size());
    }

    @Test
    void selectTopicsForDifficulty_MEDIUM_returnsCorrectDistribution() {
        when(topicSummaryRepository.findByChildProfileId(1L))
                .thenReturn(createTopicsWithBands(10, 10, 10));

        TopicSelectionResult result = service.selectTopicsForDifficulty(1L, DifficultyLevel.MEDIUM, 10);

        assertEquals(10, result.getSelectedTopicIds().size());
    }

    @Test
    void selectTopicsForDifficulty_HARD_returnsCorrectDistribution() {
        when(topicSummaryRepository.findByChildProfileId(1L))
                .thenReturn(createTopicsWithBands(10, 10, 10));

        TopicSelectionResult result = service.selectTopicsForDifficulty(1L, DifficultyLevel.HARD, 10);

        assertEquals(10, result.getSelectedTopicIds().size());
    }

    @Test
    void selectTopicsForDifficulty_fallbackWhenBandEmpty() {
        when(topicSummaryRepository.findByChildProfileId(1L))
                .thenReturn(createTopicsWithBands(10, 0, 0));

        TopicSelectionResult result = service.selectTopicsForDifficulty(1L, DifficultyLevel.EASY, 10);

        assertEquals(10, result.getSelectedTopicIds().size());
    }

    @Test
    void selectTopicsForDifficulty_returnsAllWhenCountExceedsAvailable() {
        when(topicSummaryRepository.findByChildProfileId(1L))
                .thenReturn(createTopicsWithBands(5, 3, 2));

        TopicSelectionResult result = service.selectTopicsForDifficulty(1L, DifficultyLevel.EASY, 100);

        assertEquals(10, result.getSelectedTopicIds().size());
    }

    @Test
    void selectTopicsForDifficulty_defaultCount_returnsAllMatching() {
        when(topicSummaryRepository.findByChildProfileId(1L))
                .thenReturn(createTopicsWithBands(5, 3, 2));

        TopicSelectionResult result = service.selectTopicsForDifficulty(1L, DifficultyLevel.EASY, null);

        assertEquals(10, result.getSelectedTopicIds().size());
    }

    @Test
    void selectTopicsForDifficulty_nullCount_returnsAllMatching() {
        when(topicSummaryRepository.findByChildProfileId(1L))
                .thenReturn(createTopicsWithBands(5, 3, 2));

        TopicSelectionResult result = service.selectTopicsForDifficulty(1L, DifficultyLevel.EASY, null);

        assertEquals(10, result.getSelectedTopicIds().size());
    }

    @Test
    void selectTopicsForDifficulty_emptyTopicList() {
        when(topicSummaryRepository.findByChildProfileId(1L)).thenReturn(List.of());

        TopicSelectionResult result = service.selectTopicsForDifficulty(1L, DifficultyLevel.EASY, 10);

        assertTrue(result.getSelectedTopicIds().isEmpty());
    }

    @Test
    void selectTopicsForDifficulty_respectsExactCount() {
        when(topicSummaryRepository.findByChildProfileId(1L))
                .thenReturn(createTopicsWithBands(10, 10, 10));

        TopicSelectionResult result = service.selectTopicsForDifficulty(1L, DifficultyLevel.EASY, 3);

        assertEquals(3, result.getSelectedTopicIds().size());
    }

    @Test
    void selectTopicsForDifficulty_topicsFromWeakBandFirstForEasy() {
        var topics = createTopicsWithBands(10, 10, 10);
        when(topicSummaryRepository.findByChildProfileId(1L)).thenReturn(topics);

        TopicSelectionResult result = service.selectTopicsForDifficulty(1L, DifficultyLevel.EASY, 10);

        assertEquals(10, result.getSelectedTopicIds().size());
        assertTrue(result.getSelectedTopicIds().stream().allMatch(id -> id >= 1L && id <= 30L));
    }

    @Test
    void selectTopicsForDifficulty_deterministicOrdering() {
        when(topicSummaryRepository.findByChildProfileId(1L))
                .thenReturn(createTopicsWithBands(3, 2, 1));

        TopicSelectionResult result1 = service.selectTopicsForDifficulty(1L, DifficultyLevel.EASY, 5);
        TopicSelectionResult result2 = service.selectTopicsForDifficulty(1L, DifficultyLevel.EASY, 5);

        assertEquals(result1.getSelectedTopicIds(), result2.getSelectedTopicIds());
    }

    private TopicSummary createTopicSummary(Long topicId, Long id, TopicPerformanceBand band) {
        var summary = new TopicSummary();
        summary.setId(id);
        summary.setTopicId(topicId);
        summary.setChildProfileId(1L);
        summary.setPerformanceBand(band);
        return summary;
    }

    private List<TopicSummary> createTopicsWithBands(int weakCount, int mediumCount, int strongCount) {
        var topics = new java.util.ArrayList<TopicSummary>();
        long id = 1L;
        for (int i = 0; i < weakCount; i++) {
            topics.add(createTopicSummary(id, id, TopicPerformanceBand.WEAK));
            id++;
        }
        for (int i = 0; i < mediumCount; i++) {
            topics.add(createTopicSummary(id, id, TopicPerformanceBand.MEDIUM));
            id++;
        }
        for (int i = 0; i < strongCount; i++) {
            topics.add(createTopicSummary(id, id, TopicPerformanceBand.STRONG));
            id++;
        }
        return topics;
    }
}
