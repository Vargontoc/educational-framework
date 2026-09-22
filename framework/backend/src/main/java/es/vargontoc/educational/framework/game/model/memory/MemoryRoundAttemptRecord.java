package es.vargontoc.educational.framework.game.model.memory;

import es.vargontoc.educational.framework.tracking.model.AttemptResult;

/**
 * A resolved pair attempt waiting in the buffer until the game completes (the counterpart of
 * {@code RoundAttemptRecord}). The engine does not know the topic nor the difficulty level: the orchestrator adds
 * them when it flushes.
 *
 * @param elementId       element of the pair; null when the pair did not match (no single element to attribute it to)
 * @param result          CORRECT for a matching pair, INCORRECT otherwise
 * @param responseTimeMs  client-reported time of the second card
 * @param attemptContext  the serialised {@link MemoryAttemptContext}
 */
public record MemoryRoundAttemptRecord(
        String elementId,
        AttemptResult result,
        Integer responseTimeMs,
        String attemptContext) {
}
