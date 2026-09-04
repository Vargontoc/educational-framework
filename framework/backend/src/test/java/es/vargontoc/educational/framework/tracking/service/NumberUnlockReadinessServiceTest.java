package es.vargontoc.educational.framework.tracking.service;

import es.vargontoc.educational.framework.content.model.RecognitionType;
import es.vargontoc.educational.framework.content.model.Topic;
import es.vargontoc.educational.framework.content.ports.out.TopicRepository;
import es.vargontoc.educational.framework.tracking.config.NumberUnlockProperties;
import es.vargontoc.educational.framework.tracking.model.NumberUnlockState;
import es.vargontoc.educational.framework.tracking.model.RecognitionCategory;
import es.vargontoc.educational.framework.tracking.model.TopicSummary;
import es.vargontoc.educational.framework.tracking.ports.out.TopicSummaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NumberUnlockReadinessServiceTest {

    private static final Long CHILD_PROFILE_ID = 1L;
    private static final Long LETTER_TOPIC_ID = 10L;
    private static final Long SHAPE_TOPIC_ID = 20L;

    @Mock
    private TopicSummaryRepository topicSummaryRepository;

    @Mock
    private TopicRepository topicRepository;

    private NumberUnlockReadinessService service;

    @BeforeEach
    void setUp() {
        NumberUnlockProperties properties = new NumberUnlockProperties();
        properties.setMinSuccessRatePercent(80);
        properties.setMinAttemptsPerCategory(10);
        service = new NumberUnlockReadinessService(topicSummaryRepository, topicRepository, properties);
    }

    private void givenRecognitionTopics() {
        when(topicRepository.findByRecognitionType(RecognitionType.LETTER))
                .thenReturn(List.of(topicWithId(LETTER_TOPIC_ID)));
        when(topicRepository.findByRecognitionType(RecognitionType.SHAPE))
                .thenReturn(List.of(topicWithId(SHAPE_TOPIC_ID)));
    }

    @Test
    void evaluateNumberUnlock_noHistory_staysLocked() {
        givenRecognitionTopics();
        when(topicSummaryRepository.findByChildProfileIdAndTopicId(CHILD_PROFILE_ID, LETTER_TOPIC_ID))
                .thenReturn(Optional.empty());
        when(topicSummaryRepository.findByChildProfileIdAndTopicId(CHILD_PROFILE_ID, SHAPE_TOPIC_ID))
                .thenReturn(Optional.empty());

        NumberUnlockState state = service.evaluateNumberUnlock(CHILD_PROFILE_ID);

        assertFalse(state.unlocked());
        assertFalse(state.letterMastered());
        assertFalse(state.shapeMastered());
    }

    @Test
    void evaluateNumberUnlock_letterAndShapeMeetThreshold_unlocks() {
        givenRecognitionTopics();
        when(topicSummaryRepository.findByChildProfileIdAndTopicId(CHILD_PROFILE_ID, LETTER_TOPIC_ID))
                .thenReturn(Optional.of(summaryOf(9, 10)));
        when(topicSummaryRepository.findByChildProfileIdAndTopicId(CHILD_PROFILE_ID, SHAPE_TOPIC_ID))
                .thenReturn(Optional.of(summaryOf(8, 10)));

        NumberUnlockState state = service.evaluateNumberUnlock(CHILD_PROFILE_ID);

        assertTrue(state.unlocked());
        assertTrue(state.letterMastered());
        assertTrue(state.shapeMastered());
    }

    @Test
    void evaluateNumberUnlock_onlyLetterMastered_staysLocked() {
        givenRecognitionTopics();
        when(topicSummaryRepository.findByChildProfileIdAndTopicId(CHILD_PROFILE_ID, LETTER_TOPIC_ID))
                .thenReturn(Optional.of(summaryOf(9, 10)));
        when(topicSummaryRepository.findByChildProfileIdAndTopicId(CHILD_PROFILE_ID, SHAPE_TOPIC_ID))
                .thenReturn(Optional.of(summaryOf(3, 10)));

        NumberUnlockState state = service.evaluateNumberUnlock(CHILD_PROFILE_ID);

        assertFalse(state.unlocked());
        assertTrue(state.letterMastered());
        assertFalse(state.shapeMastered());
    }

    @Test
    void evaluateNumberUnlock_successRateAboveThresholdButBelowMinAttempts_staysLocked() {
        givenRecognitionTopics();
        when(topicSummaryRepository.findByChildProfileIdAndTopicId(CHILD_PROFILE_ID, LETTER_TOPIC_ID))
                .thenReturn(Optional.of(summaryOf(3, 3)));
        when(topicSummaryRepository.findByChildProfileIdAndTopicId(CHILD_PROFILE_ID, SHAPE_TOPIC_ID))
                .thenReturn(Optional.of(summaryOf(3, 3)));

        NumberUnlockState state = service.evaluateNumberUnlock(CHILD_PROFILE_ID);

        assertFalse(state.unlocked());
        assertFalse(state.letterMastered());
        assertFalse(state.shapeMastered());
    }

    @Test
    void filterAllowedCategories_numberLocked_removesNumberOnly() {
        givenRecognitionTopics();
        when(topicSummaryRepository.findByChildProfileIdAndTopicId(CHILD_PROFILE_ID, LETTER_TOPIC_ID))
                .thenReturn(Optional.empty());
        when(topicSummaryRepository.findByChildProfileIdAndTopicId(CHILD_PROFILE_ID, SHAPE_TOPIC_ID))
                .thenReturn(Optional.empty());

        List<RecognitionCategory> allowed = service.filterAllowedCategories(
                CHILD_PROFILE_ID,
                List.of(RecognitionCategory.LETTER, RecognitionCategory.NUMBER, RecognitionCategory.SHAPE));

        assertEquals(List.of(RecognitionCategory.LETTER, RecognitionCategory.SHAPE), allowed);
    }

    @Test
    void filterAllowedCategories_numberUnlocked_keepsAllCandidates() {
        givenRecognitionTopics();
        when(topicSummaryRepository.findByChildProfileIdAndTopicId(CHILD_PROFILE_ID, LETTER_TOPIC_ID))
                .thenReturn(Optional.of(summaryOf(9, 10)));
        when(topicSummaryRepository.findByChildProfileIdAndTopicId(CHILD_PROFILE_ID, SHAPE_TOPIC_ID))
                .thenReturn(Optional.of(summaryOf(8, 10)));

        List<RecognitionCategory> allowed = service.filterAllowedCategories(
                CHILD_PROFILE_ID,
                List.of(RecognitionCategory.LETTER, RecognitionCategory.NUMBER, RecognitionCategory.SHAPE));

        assertEquals(List.of(RecognitionCategory.LETTER, RecognitionCategory.NUMBER, RecognitionCategory.SHAPE), allowed);
    }

    @Test
    void filterAllowedCategories_candidatesWithoutNumber_untouched() {
        List<RecognitionCategory> allowed = service.filterAllowedCategories(
                CHILD_PROFILE_ID,
                List.of(RecognitionCategory.LETTER, RecognitionCategory.ANIMAL));

        assertEquals(List.of(RecognitionCategory.LETTER, RecognitionCategory.ANIMAL), allowed);
    }

    private Topic topicWithId(Long id) {
        Topic topic = new Topic();
        topic.setId(id);
        return topic;
    }

    private TopicSummary summaryOf(int totalCorrect, int totalAttempts) {
        TopicSummary summary = new TopicSummary();
        summary.setTotalCorrect(totalCorrect);
        summary.setTotalAttempts(totalAttempts);
        return summary;
    }
}
