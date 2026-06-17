package es.vargontoc.educational.framework.tracking.service;

import es.vargontoc.educational.framework.tracking.model.CuriosityViewed;
import es.vargontoc.educational.framework.tracking.ports.in.GetViewedCuriositiesUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.RegisterCuriosityViewedUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.ResetCuriosityCycleUseCase;
import es.vargontoc.educational.framework.tracking.ports.out.CuriosityViewedRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Transactional
public class CuriosityViewedService implements
        RegisterCuriosityViewedUseCase,
        GetViewedCuriositiesUseCase,
        ResetCuriosityCycleUseCase {

    private final CuriosityViewedRepository repository;

    public CuriosityViewedService(CuriosityViewedRepository repository) {
        this.repository = repository;
    }

    @Override
    public void registerView(Long childProfileId, Long topicId, Long curiosityId, int cycleNumber) {
        Optional<CuriosityViewed> existing = repository.findByChildProfileIdAndTopicIdAndCuriosityIdAndCycleNumber(
                childProfileId, topicId, curiosityId, cycleNumber);

        if (existing.isPresent()) {
            return;
        }

        var curiosityViewed = new CuriosityViewed();
        curiosityViewed.setChildProfileId(childProfileId);
        curiosityViewed.setTopicId(topicId);
        curiosityViewed.setCuriosityId(curiosityId);
        curiosityViewed.setCycleNumber(cycleNumber);
        curiosityViewed.setViewedAt(LocalDateTime.now());

        repository.save(curiosityViewed);
    }

    @Override
    public List<CuriosityViewed> getViewedCuriosities(Long childProfileId, Long topicId) {
        return repository.findByChildProfileIdAndTopicId(childProfileId, topicId);
    }

    @Override
    public List<CuriosityViewed> getViewedCuriosities(Long childProfileId, Long topicId, int cycleNumber) {
        return repository.findByChildProfileIdAndTopicIdAndCycleNumber(childProfileId, topicId, cycleNumber);
    }

    @Override
    public int resetCycle(Long childProfileId, Long topicId) {
        int currentMax = repository.findMaxCycleNumber(childProfileId, topicId).orElse(-1);
        return currentMax + 1;
    }

}
