package es.vargontoc.educational.framework.game.model.recognition;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.vargontoc.educational.framework.game.model.enums.RecognitionCategory;

class RecognitionStateTest {

    @Test
    void recognitionState_canRepresentInitialRound() {
        var state = new RecognitionState();
        state.setRecognitionCategory(RecognitionCategory.LETTER);
        state.setRoundIndex(0);
        state.setTargetElementId("elem-A");
        state.setOptionIds(List.of("elem-A", "elem-B", "elem-C"));
        state.setRoundStartedAt(LocalDateTime.now());

        assertEquals(RecognitionCategory.LETTER, state.getRecognitionCategory());
        assertEquals(0, state.getRoundIndex());
        assertEquals("elem-A", state.getTargetElementId());
        assertEquals(3, state.getOptionIds().size());
        assertEquals(RecognitionDefaults.DEFAULT_TOTAL_ROUNDS, state.getTotalRounds());
        assertEquals(0, state.getCurrentRoundAttemptCount());
        assertEquals(0, state.getCurrentRoundConsecutiveFailures());
        assertEquals(0, state.getTotalIncorrectAttempts());
        assertEquals(0, state.getTotalCorrectFirstTry());
        assertFalse(state.isHintActive());
        assertNull(state.getSelectedOptionId());
        assertNull(state.getHintTriggeredAtAttempt());
        assertNull(state.getPendingDifficultyLevel());
        assertNotNull(state.getRoundsShownElementIds());
        assertTrue(state.getRoundsShownElementIds().isEmpty());
    }

    @Test
    void recognitionState_canRepresentRetryCountersAndHintState() {
        var state = new RecognitionState();
        state.setCurrentRoundAttemptCount(3);
        state.setCurrentRoundConsecutiveFailures(2);
        state.setTotalIncorrectAttempts(5);
        state.setTotalCorrectFirstTry(2);
        state.setHintActive(true);
        state.setHintTriggeredAtAttempt(2);

        assertEquals(3, state.getCurrentRoundAttemptCount());
        assertEquals(2, state.getCurrentRoundConsecutiveFailures());
        assertEquals(5, state.getTotalIncorrectAttempts());
        assertEquals(2, state.getTotalCorrectFirstTry());
        assertTrue(state.isHintActive());
        assertEquals(2, state.getHintTriggeredAtAttempt());
    }

    @Test
    void recognitionState_defaultsAreApplied() {
        var state = new RecognitionState();

        assertEquals(RecognitionDefaults.DEFAULT_TOTAL_ROUNDS, state.getTotalRounds());
        assertEquals(RecognitionDefaults.DEFAULT_DIFFICULTY_LEVEL, state.getCurrentDifficultyLevel());
        assertNotNull(state.getOptionIds());
        assertNotNull(state.getRoundsShownElementIds());
    }

    @Test
    void recognitionState_totalRoundsIsConfigurable() {
        var state = new RecognitionState();
        state.setTotalRounds(10);

        assertEquals(10, state.getTotalRounds());
    }

    @Test
    void recognitionState_tracksShownElementsForAntiRepetition() {
        var state = new RecognitionState();
        state.setRoundsShownElementIds(List.of("elem-A", "elem-B", "elem-C"));

        assertEquals(3, state.getRoundsShownElementIds().size());
        assertTrue(state.getRoundsShownElementIds().contains("elem-A"));
    }

    @Test
    void recognitionState_pendingDifficultyLevelIsOptional() {
        var state = new RecognitionState();
        assertNull(state.getPendingDifficultyLevel());

        state.setPendingDifficultyLevel(3);
        assertEquals(3, state.getPendingDifficultyLevel());
    }

    @Test
    void recognitionState_doesNotRequireFrameworkDependencies() {
        var state = new RecognitionState();
        state.setRecognitionCategory(RecognitionCategory.ANIMAL);
        state.setRoundIndex(0);
        state.setTargetElementId("animal-1");
        state.setOptionIds(List.of("animal-1", "animal-2"));
        state.setRoundStartedAt(LocalDateTime.now());
        state.setLastActionAt(LocalDateTime.now());

        assertNotNull(state);
        assertEquals(RecognitionCategory.ANIMAL, state.getRecognitionCategory());
    }
}
