package es.vargontoc.educational.framework.tracking.infrastructure.persistence;

import es.vargontoc.educational.framework.tracking.model.CuriosityViewed;
import es.vargontoc.educational.framework.tracking.ports.out.CuriosityViewedRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CuriosityViewedPersistenceAdapter implements CuriosityViewedRepository {

    private final CuriosityViewedJpaRepository jpaRepository;

    public CuriosityViewedPersistenceAdapter(CuriosityViewedJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<CuriosityViewed> findByChildProfileIdAndTopicId(Long childProfileId, Long topicId) {
        return jpaRepository.findByChildProfileIdAndTopicId(childProfileId, topicId)
                .stream()
                .map(CuriosityViewedPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public List<CuriosityViewed> findByChildProfileIdAndTopicIdAndCycleNumber(
            Long childProfileId, Long topicId, int cycleNumber) {
        return jpaRepository.findByChildProfileIdAndTopicIdAndCycleNumber(childProfileId, topicId, cycleNumber)
                .stream()
                .map(CuriosityViewedPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public Optional<CuriosityViewed> findByChildProfileIdAndTopicIdAndCuriosityIdAndCycleNumber(
            Long childProfileId, Long topicId, Long curiosityId, int cycleNumber) {
        return jpaRepository.findByChildProfileIdAndTopicIdAndCuriosityIdAndCycleNumber(
                        childProfileId, topicId, curiosityId, cycleNumber)
                .map(CuriosityViewedPersistenceAdapter::toDomain);
    }

    @Override
    public CuriosityViewed save(CuriosityViewed curiosityViewed) {
        return toDomain(jpaRepository.save(toJpa(curiosityViewed)));
    }

    @Override
    public Optional<Integer> findMaxCycleNumber(Long childProfileId, Long topicId) {
        return jpaRepository.findMaxCycleNumber(childProfileId, topicId);
    }

    static CuriosityViewed toDomain(CuriosityViewedJpaEntity source) {
        var target = new CuriosityViewed();
        target.setId(source.getId());
        target.setChildProfileId(source.getChildProfileId());
        target.setTopicId(source.getTopicId());
        target.setCuriosityId(source.getCuriosityId());
        target.setCycleNumber(source.getCycleNumber());
        target.setViewedAt(source.getViewedAt());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    static CuriosityViewedJpaEntity toJpa(CuriosityViewed source) {
        var target = new CuriosityViewedJpaEntity();
        target.setId(source.getId());
        target.setChildProfileId(source.getChildProfileId());
        target.setTopicId(source.getTopicId());
        target.setCuriosityId(source.getCuriosityId());
        target.setCycleNumber(source.getCycleNumber());
        target.setViewedAt(source.getViewedAt());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }
}
