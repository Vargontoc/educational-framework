package es.vargontoc.educational.framework.tracking.service;

import es.vargontoc.educational.framework.content.model.RecognitionType;
import es.vargontoc.educational.framework.content.model.Topic;
import es.vargontoc.educational.framework.content.ports.out.TopicRepository;
import es.vargontoc.educational.framework.tracking.config.NumberUnlockProperties;
import es.vargontoc.educational.framework.tracking.model.NumberUnlockState;
import es.vargontoc.educational.framework.tracking.model.RecognitionCategory;
import es.vargontoc.educational.framework.tracking.ports.in.FilterAllowedRecognitionCategoriesUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.NumberUnlockReadinessUseCase;
import es.vargontoc.educational.framework.tracking.ports.out.TopicSummaryRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Transactional
public class NumberUnlockReadinessService implements NumberUnlockReadinessUseCase, FilterAllowedRecognitionCategoriesUseCase {

    private final TopicSummaryRepository topicSummaryRepository;
    private final TopicRepository topicRepository;
    private final NumberUnlockProperties properties;

    public NumberUnlockReadinessService(
            TopicSummaryRepository topicSummaryRepository,
            TopicRepository topicRepository,
            NumberUnlockProperties properties) {
        this.topicSummaryRepository = topicSummaryRepository;
        this.topicRepository = topicRepository;
        this.properties = properties;
    }

    @Override
    public NumberUnlockState evaluateNumberUnlock(Long childProfileId) {
        boolean letterMastered = isCategoryMastered(childProfileId, RecognitionType.LETTER);
        boolean shapeMastered = isCategoryMastered(childProfileId, RecognitionType.SHAPE);
        return new NumberUnlockState(childProfileId, letterMastered && shapeMastered, letterMastered, shapeMastered);
    }

    @Override
    public List<RecognitionCategory> filterAllowedCategories(Long childProfileId, List<RecognitionCategory> candidateCategories) {
        if (candidateCategories == null || candidateCategories.isEmpty()) {
            return List.of();
        }
        if (!candidateCategories.contains(RecognitionCategory.NUMBER)) {
            return candidateCategories;
        }

        boolean numberUnlocked = evaluateNumberUnlock(childProfileId).unlocked();
        if (numberUnlocked) {
            return candidateCategories;
        }

        List<RecognitionCategory> allowed = new ArrayList<>();
        for (RecognitionCategory category : candidateCategories) {
            if (category != RecognitionCategory.NUMBER) {
                allowed.add(category);
            }
        }
        return allowed;
    }

    private boolean isCategoryMastered(Long childProfileId, RecognitionType recognitionType) {
        List<Long> topicIds = topicRepository.findByRecognitionType(recognitionType).stream()
                .map(Topic::getId)
                .toList();
        if (topicIds.isEmpty()) {
            return false;
        }

        int totalAttempts = 0;
        int totalCorrect = 0;
        for (Long topicId : topicIds) {
            var summary = topicSummaryRepository.findByChildProfileIdAndTopicId(childProfileId, topicId);
            if (summary.isPresent()) {
                totalAttempts += summary.get().getTotalAttempts();
                totalCorrect += summary.get().getTotalCorrect();
            }
        }

        if (totalAttempts < properties.getMinAttemptsPerCategory()) {
            return false;
        }

        BigDecimal successRate = calculateSuccessRate(totalCorrect, totalAttempts);
        return successRate.compareTo(BigDecimal.valueOf(properties.getMinSuccessRatePercent())) >= 0;
    }

    private BigDecimal calculateSuccessRate(int correct, int total) {
        return BigDecimal.valueOf(correct)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }
}
