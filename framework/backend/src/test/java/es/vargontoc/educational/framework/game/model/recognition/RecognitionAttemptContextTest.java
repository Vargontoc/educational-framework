package es.vargontoc.educational.framework.game.model.recognition;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.vargontoc.educational.framework.game.model.enums.EngineType;
import es.vargontoc.educational.framework.game.model.enums.RecognitionCategory;

class RecognitionAttemptContextTest {

    @Test
    void recognitionAttemptContext_storesAllFields() {
        var ctx = new RecognitionAttemptContext();
        ctx.setRecognitionCategory(RecognitionCategory.NUMBER);
        ctx.setRoundIndex(2);
        ctx.setTargetElementId("num-5");
        ctx.setSelectedOptionId("num-3");
        ctx.setOptionIds(List.of("num-5", "num-3", "num-7"));
        ctx.setFirstTry(false);
        ctx.setHintActive(true);
        ctx.setHintTriggeredBeforeAnswer(true);
        ctx.setAttemptNumberInRound(3);
        ctx.setResponseTimeMs(4500L);

        assertEquals(EngineType.RECOGNITION, ctx.getEngineType());
        assertEquals(RecognitionCategory.NUMBER, ctx.getRecognitionCategory());
        assertEquals(2, ctx.getRoundIndex());
        assertEquals("num-5", ctx.getTargetElementId());
        assertEquals("num-3", ctx.getSelectedOptionId());
        assertEquals(3, ctx.getOptionIds().size());
        assertFalse(ctx.isFirstTry());
        assertTrue(ctx.isHintActive());
        assertTrue(ctx.isHintTriggeredBeforeAnswer());
        assertEquals(3, ctx.getAttemptNumberInRound());
        assertEquals(4500L, ctx.getResponseTimeMs());
    }

    @Test
    void recognitionAttemptContext_defaultEngineTypeIsRecognition() {
        var ctx = new RecognitionAttemptContext();

        assertEquals(EngineType.RECOGNITION, ctx.getEngineType());
    }

    @Test
    void recognitionAttemptContext_firstTryCorrectAttempt() {
        var ctx = new RecognitionAttemptContext();
        ctx.setRecognitionCategory(RecognitionCategory.COLOR);
        ctx.setRoundIndex(0);
        ctx.setTargetElementId("color-red");
        ctx.setSelectedOptionId("color-red");
        ctx.setOptionIds(List.of("color-red", "color-blue"));
        ctx.setFirstTry(true);
        ctx.setHintActive(false);
        ctx.setHintTriggeredBeforeAnswer(false);
        ctx.setAttemptNumberInRound(1);
        ctx.setResponseTimeMs(1200L);

        assertTrue(ctx.isFirstTry());
        assertFalse(ctx.isHintActive());
        assertFalse(ctx.isHintTriggeredBeforeAnswer());
        assertEquals(1, ctx.getAttemptNumberInRound());
    }

    @Test
    void recognitionAttemptContext_doesNotRequireFrameworkDependencies() {
        var ctx = new RecognitionAttemptContext();
        ctx.setRecognitionCategory(RecognitionCategory.SHAPE);
        ctx.setTargetElementId("shape-circle");
        ctx.setSelectedOptionId("shape-circle");
        ctx.setOptionIds(List.of("shape-circle", "shape-square"));
        ctx.setResponseTimeMs(800L);

        assertEquals(RecognitionCategory.SHAPE, ctx.getRecognitionCategory());
        assertEquals("shape-circle", ctx.getTargetElementId());
        assertEquals(800L, ctx.getResponseTimeMs());
    }
}
