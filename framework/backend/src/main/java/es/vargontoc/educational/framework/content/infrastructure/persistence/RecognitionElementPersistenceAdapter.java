package es.vargontoc.educational.framework.content.infrastructure.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.stereotype.Repository;

import es.vargontoc.educational.framework.audio.application.ports.in.AudioUseCase;
import es.vargontoc.educational.framework.audio.domain.AudioRequest;
import es.vargontoc.educational.framework.audio.domain.enums.TonePreset;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.RecognitionElement;
import es.vargontoc.educational.framework.content.ports.out.RecognitionElementRepository;

@Repository
public class RecognitionElementPersistenceAdapter implements RecognitionElementRepository {

    private final RecognitionElementJpaRepository jpaRepository;
    private final AudioUseCase audio;

    final Map<String, List<AudioRequest>> curiosities = new HashMap<>();

    public RecognitionElementPersistenceAdapter(RecognitionElementJpaRepository jpaRepository, AudioUseCase audio) {
        this.jpaRepository = jpaRepository;
        this.audio = audio;
    }

    @Override
    public List<RecognitionElement> findByTopicIdAndStatus(Long topicId, ContentStatus status) {
        return jpaRepository.findByTopicIdAndStatus(topicId, status.name())
                .stream()
                .map(RecognitionElementPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public List<RecognitionElement> findByTopicIdInAndStatus(List<Long> topicIds, ContentStatus status) {
        if (topicIds == null || topicIds.isEmpty()) {
            return List.of();
        }
        return jpaRepository.findByTopicIdInAndStatus(topicIds, status.name())
                .stream()
                .map(RecognitionElementPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public RecognitionElement save(RecognitionElement element) {
        return toDomain(jpaRepository.save(toJpa(element)));
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<RecognitionElement> findAllById(List<Long> ids) {
        return jpaRepository.findAllById(ids)
                .stream()
                .map(RecognitionElementPersistenceAdapter::toDomain)
                .toList();
    }

    static RecognitionElement toDomain(RecognitionElementJpaEntity source) {
        var target = new RecognitionElement();
        target.setId(source.getId());
        target.setTopicId(source.getTopicId());
        target.setCode(source.getCode());
        target.setDisplayValue(source.getDisplayValue());
        target.setResourceRefs(source.getResourceRefs());
        target.setSortOrder(source.getSortOrder());
        target.setStatus(ContentStatus.valueOf(source.getStatus()));
        target.setSimilarityGroup(source.getSimilarityGroup());
        target.setAccessibleColorId(source.getAccessibleColorId());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    static RecognitionElementJpaEntity toJpa(RecognitionElement source) {
        var target = new RecognitionElementJpaEntity();
        target.setId(source.getId());
        target.setTopicId(source.getTopicId());
        target.setCode(source.getCode());
        target.setDisplayValue(source.getDisplayValue());
        target.setResourceRefs(source.getResourceRefs());
        target.setSortOrder(source.getSortOrder());
        target.setStatus(source.getStatus().name());
        target.setSimilarityGroup(source.getSimilarityGroup());
        target.setAccessibleColorId(source.getAccessibleColorId());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    @Override
    public void addCuriosity(String code, String[] curiosities) {
        this.curiosities.put(code, new ArrayList<>());
        for(String c : curiosities) {
            AudioRequest r = AudioRequest.withPreset(c, TonePreset.ADVENTURE);

            this.curiosities.get(code).add(r);
            audio.getAudio(r);
        }
        
    }

    @Override
    public byte[] getCuriosityAudio(String code) {
        if(!curiosities.containsKey(code) || curiosities.get(code).isEmpty())
            return new byte[] {};

        Random rng = new Random();
        int v = rng.nextInt(0, curiosities.get(code).size());
        return audio.getAudio(curiosities.get(code).get(v));
    }
}
