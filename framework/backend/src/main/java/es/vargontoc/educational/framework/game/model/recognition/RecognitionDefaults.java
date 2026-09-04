package es.vargontoc.educational.framework.game.model.recognition;

public final class RecognitionDefaults {

    private RecognitionDefaults() {
    }

    public static final int DEFAULT_TOTAL_ROUNDS = 5;
    public static final int MIN_OPTIONS_PER_ROUND = 2;
    public static final int MAX_OPTIONS_PER_ROUND = 3;
    public static final int DEFAULT_DIFFICULTY_LEVEL = 1;
    public static final int HINT_ACTIVATION_THRESHOLD = 2;
    public static final long GOOD_RESPONSE_TIME_THRESHOLD_MS = 5000L;
}
