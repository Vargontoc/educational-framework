package es.vargontoc.educational.framework.tracking.service;

import es.vargontoc.educational.framework.tracking.model.TopicGroupedByPerformance;
import es.vargontoc.educational.framework.tracking.model.TopicSelectionResult;
import es.vargontoc.educational.framework.tracking.model.DifficultyLevel;
import es.vargontoc.educational.framework.tracking.model.TopicPerformanceBand;
import es.vargontoc.educational.framework.tracking.model.TopicSummary;
import es.vargontoc.educational.framework.tracking.ports.in.ClassifyTopicsByPerformanceUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.SelectTopicsForDifficultyUseCase;
import es.vargontoc.educational.framework.tracking.ports.out.TopicSummaryRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Transactional
public class TopicSelectionService implements ClassifyTopicsByPerformanceUseCase, SelectTopicsForDifficultyUseCase {

    private static final Map<DifficultyLevel, int[]> DISTRIBUTION = Map.of(
            DifficultyLevel.EASY, new int[]{50, 30, 20},
            DifficultyLevel.MEDIUM, new int[]{60, 30, 10},
            DifficultyLevel.HARD, new int[]{70, 20, 10}
    );

    private final TopicSummaryRepository topicSummaryRepository;

    public TopicSelectionService(TopicSummaryRepository topicSummaryRepository) {
        this.topicSummaryRepository = topicSummaryRepository;
    }

    @Override
    public TopicGroupedByPerformance classifyTopics(Long childProfileId) {
        var summaries = topicSummaryRepository.findByChildProfileId(childProfileId);

        var grouped = new TopicGroupedByPerformance();
        for (var summary : summaries) {
            grouped.addTopic(summary.getPerformanceBand(), summary.getTopicId());
        }

        return grouped;
    }

    @Override
    public TopicSelectionResult selectTopicsForDifficulty(Long childProfileId, DifficultyLevel difficulty, Integer count) {
        var grouped = classifyTopics(childProfileId);

        int targetCount = (count != null && count > 0) ? count : Integer.MAX_VALUE;

        int[] percentages = DISTRIBUTION.get(difficulty);
        int weakPct = percentages[0];
        int mediumPct = percentages[1];
        int strongPct = percentages[2];

        List<Long> weakTopics = grouped.getTopics(TopicPerformanceBand.WEAK);
        List<Long> mediumTopics = grouped.getTopics(TopicPerformanceBand.MEDIUM);
        List<Long> strongTopics = grouped.getTopics(TopicPerformanceBand.STRONG);

        int totalAvailable = weakTopics.size() + mediumTopics.size() + strongTopics.size();

        if (totalAvailable == 0) {
            return new TopicSelectionResult(List.of());
        }

        if (targetCount >= totalAvailable) {
            return new TopicSelectionResult(buildAllTopicsList(weakTopics, mediumTopics, strongTopics));
        }

        int targetWeak = Math.min(weakTopics.size(), (targetCount * weakPct) / 100);
        int targetMedium = Math.min(mediumTopics.size(), (targetCount * mediumPct) / 100);
        int targetStrong = Math.min(strongTopics.size(), (targetCount * strongPct) / 100);

        int actualSelected = targetWeak + targetMedium + targetStrong;
        int remaining = targetCount - actualSelected;
        while (remaining > 0) {
            int before = remaining;
            if (targetWeak < weakTopics.size()) {
                int extra = Math.min(remaining, weakTopics.size() - targetWeak);
                targetWeak += extra;
                remaining -= extra;
            }
            if (remaining > 0 && targetMedium < mediumTopics.size()) {
                int extra = Math.min(remaining, mediumTopics.size() - targetMedium);
                targetMedium += extra;
                remaining -= extra;
            }
            if (remaining > 0 && targetStrong < strongTopics.size()) {
                int extra = Math.min(remaining, strongTopics.size() - targetStrong);
                targetStrong += extra;
                remaining -= extra;
            }
            if (remaining == before) break;
        }

        List<Long> result = new ArrayList<>();
        result.addAll(selectFirstN(sortDeterministic(weakTopics), targetWeak));
        result.addAll(selectFirstN(sortDeterministic(mediumTopics), targetMedium));
        result.addAll(selectFirstN(sortDeterministic(strongTopics), targetStrong));

        return new TopicSelectionResult(result);
    }

    private List<Long> buildAllTopicsList(List<Long> weak, List<Long> medium, List<Long> strong) {
        List<Long> all = new ArrayList<>();
        all.addAll(sortDeterministic(weak));
        all.addAll(sortDeterministic(medium));
        all.addAll(sortDeterministic(strong));
        return all;
    }

    private List<Long> sortDeterministic(List<Long> topicIds) {
        return topicIds.stream()
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    private List<Long> selectFirstN(List<Long> sorted, int n) {
        if (n >= sorted.size()) {
            return sorted;
        }
        return sorted.subList(0, n);
    }
}
