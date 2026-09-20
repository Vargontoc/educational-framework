package es.vargontoc.educational.framework.game.service;

import es.vargontoc.educational.framework.family.model.ColorVisionMode;
import es.vargontoc.educational.framework.family.ports.in.ColorAdaptativeUseCase;
import es.vargontoc.educational.framework.game.model.recognition.CandidateMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Validates color distractors to ensure they are sufficiently distinguishable
 * from the target and from each other, based on the child's color vision mode.
 *
 * Uses {@link ColorAdaptativeUseCase#isTooSimilar(ColorVisionMode, String, String)}
 * which applies a Delta E threshold of 20.0 in Lab color space.
 */
public class ColorSimilarityValidator {

    private static final Logger log = LoggerFactory.getLogger(ColorSimilarityValidator.class);

    private static final int MAX_CACHED_PAIRS = 4096;

    private final ColorAdaptativeUseCase colorAdaptativeUseCase;
    private final Map<String, Boolean> similarityCache = new ConcurrentHashMap<>();

    public ColorSimilarityValidator(ColorAdaptativeUseCase colorAdaptativeUseCase) {
        this.colorAdaptativeUseCase = colorAdaptativeUseCase;
    }

    /**
     * Validates that all distractors are sufficiently different from the target
     * and from each other, according to the given color vision mode.
     *
     * @param targetColorHex   hex color of the target element
     * @param distractorColorHexes hex colors of the distractor elements
     * @param colorVisionMode  the child's color vision mode
     * @return true if all colors are sufficiently distinguishable
     */
    public boolean validateDistractors(String targetColorHex, List<String> distractorColorHexes, ColorVisionMode colorVisionMode) {
        if (targetColorHex == null || distractorColorHexes == null || distractorColorHexes.isEmpty()) {
            return true; // No validation possible, assume valid
        }
        if (colorVisionMode == null) {
            colorVisionMode = ColorVisionMode.NONE;
        }

        // Check each distractor against the target
        for (String distractorHex : distractorColorHexes) {
            if (distractorHex == null) continue;
            if (isTooSimilar(colorVisionMode, targetColorHex, distractorHex)) {
                return false;
            }
        }

        // Check each distractor against every other distractor
        for (int i = 0; i < distractorColorHexes.size(); i++) {
            for (int j = i + 1; j < distractorColorHexes.size(); j++) {
                String hexA = distractorColorHexes.get(i);
                String hexB = distractorColorHexes.get(j);
                if (hexA == null || hexB == null) continue;
                if (isTooSimilar(colorVisionMode, hexA, hexB)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Filters and selects valid distractors from the candidate pool, ensuring they are
     * sufficiently different from the target and from each other.
     *
     * If not enough valid distractors can be found, falls back to relaxing the constraint
     * (accepting any remaining candidates).
     *
     * @param targetId        the target element ID
     * @param candidatePool   all candidate element IDs (excluding target)
     * @param count           number of distractors to select
     * @param colorVisionMode the child's color vision mode
     * @param elementResolver function to resolve CandidateMetadata from element ID
     * @return list of selected distractor IDs
     */
    public List<String> filterValidDistractors(
            String targetId,
            List<String> candidatePool,
            int count,
            ColorVisionMode colorVisionMode,
            Function<String, CandidateMetadata> elementResolver) {

        if (candidatePool == null || candidatePool.isEmpty() || count <= 0) {
            return Collections.emptyList();
        }
        if (colorVisionMode == null) {
            colorVisionMode = ColorVisionMode.NONE;
        }

        CandidateMetadata targetMeta = elementResolver.apply(targetId);
        String targetColorHex = (targetMeta != null) ? targetMeta.colorHex() : null;

        // If we can't resolve the target color, fall back to random selection
        if (targetColorHex == null) {
            log.debug("Target color hex not available for elementId={}, falling back to random selection", targetId);
            List<String> fallback = new ArrayList<>(candidatePool);
            Collections.shuffle(fallback);
            return fallback.subList(0, Math.min(count, fallback.size()));
        }

        // Build list of candidates with their color hex
        List<CandidateWithColor> candidatesWithColor = new ArrayList<>();
        for (String candidateId : candidatePool) {
            CandidateMetadata meta = elementResolver.apply(candidateId);
            String colorHex = (meta != null) ? meta.colorHex() : null;
            candidatesWithColor.add(new CandidateWithColor(candidateId, colorHex));
        }

        // Shuffle to add randomness
        Collections.shuffle(candidatesWithColor);

        List<String> selected = new ArrayList<>();
        List<String> selectedColorHexes = new ArrayList<>();

        // First pass: select distractors that are valid (not too similar to target or each other)
        for (CandidateWithColor candidate : candidatesWithColor) {
            if (selected.size() >= count) break;

            if (candidate.colorHex == null) {
                // If candidate has no color info, include it (can't validate)
                selected.add(candidate.id);
                continue;
            }

            // Check against target
            if (isTooSimilar(colorVisionMode, targetColorHex, candidate.colorHex)) {
                continue;
            }

            // Check against already selected distractors
            boolean tooSimilarToSelected = false;
            for (String selectedHex : selectedColorHexes) {
                if (isTooSimilar(colorVisionMode, candidate.colorHex, selectedHex)) {
                    tooSimilarToSelected = true;
                    break;
                }
            }

            if (!tooSimilarToSelected) {
                selected.add(candidate.id);
                selectedColorHexes.add(candidate.colorHex);
            }
        }

        // Fallback: if not enough valid distractors, fill from remaining candidates
        if (selected.size() < count) {
            log.debug("Not enough valid color distractors for targetId={}, relaxing constraint. Found {}, needed {}",
                    targetId, selected.size(), count);
            for (CandidateWithColor candidate : candidatesWithColor) {
                if (selected.size() >= count) break;
                if (!selected.contains(candidate.id)) {
                    selected.add(candidate.id);
                }
            }
        }

        return selected;
    }

    /**
     * Cached, order-independent wrapper over {@link ColorAdaptativeUseCase#isTooSimilar}.
     * A colour that cannot be evaluated (malformed hex) is treated as "not too similar" so a bad
     * seed value never breaks a round; it is logged instead.
     */
    private boolean isTooSimilar(ColorVisionMode mode, String hexA, String hexB) {
        String a = hexA.toUpperCase();
        String b = hexB.toUpperCase();
        String key = mode + "|" + (a.compareTo(b) <= 0 ? a + "|" + b : b + "|" + a);

        Boolean cached = similarityCache.get(key);
        if (cached != null) {
            return cached;
        }

        boolean tooSimilar;
        try {
            tooSimilar = colorAdaptativeUseCase.isTooSimilar(mode, hexA, hexB);
        } catch (RuntimeException e) {
            log.warn("Cannot compare colors {} and {}: {}", hexA, hexB, e.getMessage());
            return false;
        }

        if (similarityCache.size() >= MAX_CACHED_PAIRS) {
            similarityCache.clear();
        }
        similarityCache.put(key, tooSimilar);
        return tooSimilar;
    }

    private record CandidateWithColor(String id, String colorHex) {}
}
