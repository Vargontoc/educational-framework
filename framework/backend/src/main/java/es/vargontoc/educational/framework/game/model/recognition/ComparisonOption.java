package es.vargontoc.educational.framework.game.model.recognition;

/**
 * One option of a comparison round: always the same element, drawn at a different size.
 *
 * @param elementId    the element shown; identical for every option of the round
 * @param scalePercent relative size (100, 75, 65, 50, 40)
 */
public record ComparisonOption(String elementId, double scalePercent) {
}
