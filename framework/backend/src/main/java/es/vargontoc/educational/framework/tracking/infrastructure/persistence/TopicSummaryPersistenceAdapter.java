package es.vargontoc.educational.framework.tracking.infrastructure.persistence;

import es.vargontoc.educational.framework.tracking.model.TopicPerformanceBand;
import es.vargontoc.educational.framework.tracking.model.TopicSummary;
import es.vargontoc.educational.framework.tracking.ports.out.TopicSummaryRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class TopicSummaryPersistenceAdapter implements TopicSummaryRepository {

    private final TopicSummaryJpaRepository jpaRepository;

    public TopicSummaryPersistenceAdapter(TopicSummaryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<TopicSummary> findByChildProfileIdAndTopicId(Long childProfileId, Long topicId) {
        return jpaRepository.findByChildProfileIdAndTopicId(childProfileId, topicId)
            .map(TopicSummaryPersistenceAdapter::toDomain);
    }

    @Override
    public TopicSummary save(TopicSummary summary) {
        return toDomain(jpaRepository.save(toJpa(summary)));
    }

    static TopicSummary toDomain(TopicSummaryJpaEntity source) {
        var target = new TopicSummary();
        target.setId(source.getId());
        target.setChildProfileId(source.getChildProfileId());
        target.setTopicId(source.getTopicId());
        target.setTotalAttempts(source.getTotalAttempts());
        target.setTotalCorrect(source.getTotalCorrect());
        target.setTotalIncorrect(source.getTotalIncorrect());
        target.setTotalTimeouts(source.getTotalTimeouts());
        target.setSuccessRatePercent(source.getSuccessRatePercent());
        target.setFailureRatePercent(source.getFailureRatePercent());
        target.setAverageResponseTimeMs(source.getAverageResponseTimeMs());
        target.setPerformanceBand(TopicPerformanceBand.valueOf(source.getPerformanceBand()));
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    static TopicSummaryJpaEntity toJpa(TopicSummary source) {
        var target = new TopicSummaryJpaEntity();
        target.setId(source.getId());
        target.setChildProfileId(source.getChildProfileId());
        target.setTopicId(source.getTopicId());
        target.setTotalAttempts(source.getTotalAttempts());
        target.setTotalCorrect(source.getTotalCorrect());
        target.setTotalIncorrect(source.getTotalIncorrect());
        target.setTotalTimeouts(source.getTotalTimeouts());
        target.setSuccessRatePercent(source.getSuccessRatePercent());
        target.setFailureRatePercent(source.getFailureRatePercent());
        target.setAverageResponseTimeMs(source.getAverageResponseTimeMs());
        target.setPerformanceBand(source.getPerformanceBand().name());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }
}
