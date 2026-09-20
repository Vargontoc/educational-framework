package es.vargontoc.educational.framework.game.service;

/**
 * Result of generating audio for a recognition round.
 * Carries the audio data, a unique identifier, and the original text.
 *
 * @param audioId       unique identifier for this audio (UUID)
 * @param audioData     MP3 audio bytes, or null if audio was not generated
 * @param text          the text that was synthesized
 * @param audioAvailable true if audioData is non-null
 */
public record RoundAudioResult(
    String audioId,
    byte[] audioData,
    String text,
    boolean audioAvailable
) {
    /**
     * Creates a result indicating audio is not available.
     */
    public static RoundAudioResult noAudio(String text) {
        return new RoundAudioResult(null, null, text, false);
    }

    /**
     * Creates a successful result with audio data.
     */
    public static RoundAudioResult withAudio(String audioId, byte[] audioData, String text) {
        return new RoundAudioResult(audioId, audioData, text, true);
    }
}
