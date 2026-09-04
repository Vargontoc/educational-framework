package es.vargontoc.educational.framework.game.model.recognition;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.vargontoc.educational.framework.game.model.enums.RecognitionCategory;

class RecognitionCategoryTest {

    @Test
    void recognitionCategory_hasAllExpectedValues() {
        var values = RecognitionCategory.values();

        assertEquals(5, values.length);
        assertEquals(RecognitionCategory.LETTER, RecognitionCategory.valueOf("LETTER"));
        assertEquals(RecognitionCategory.NUMBER, RecognitionCategory.valueOf("NUMBER"));
        assertEquals(RecognitionCategory.SHAPE, RecognitionCategory.valueOf("SHAPE"));
        assertEquals(RecognitionCategory.COLOR, RecognitionCategory.valueOf("COLOR"));
        assertEquals(RecognitionCategory.ANIMAL, RecognitionCategory.valueOf("ANIMAL"));
    }
}
