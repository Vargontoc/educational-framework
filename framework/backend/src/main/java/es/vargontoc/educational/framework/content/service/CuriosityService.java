package es.vargontoc.educational.framework.content.service;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.Curiosity;
import es.vargontoc.educational.framework.content.ports.in.CuriosityUseCase;
import es.vargontoc.educational.framework.content.ports.out.CuriosityRepository;
import es.vargontoc.educational.framework.content.ports.out.TopicRepository;
import es.vargontoc.educational.framework.content.validation.CuriosityValidator;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Transactional
public class CuriosityService implements CuriosityUseCase {

    private final CuriosityRepository curiosityRepository;
    private final TopicRepository topicRepository;
    private final CuriosityValidator curiosityValidator;

    public CuriosityService(CuriosityRepository curiosityRepository, TopicRepository topicRepository) {
        this.curiosityRepository = curiosityRepository;
        this.topicRepository = topicRepository;
        this.curiosityValidator = new CuriosityValidator();
    }

    @Override
    public Curiosity createCuriosity(String text, Long topicId, Integer minAge, Integer maxAge, List<String> tags, String locale, String phoneticHint, ContentStatus status) {
        curiosityValidator.validateForCreate(text, minAge, maxAge, locale, status);

        if (topicId != null && topicRepository.findById(topicId).isEmpty()) {
            throw new ResourceNotFoundException("Topic not found with id: " + topicId);
        }

        var curiosity = new Curiosity();
        curiosity.setText(text);
        curiosity.setTopicId(topicId);
        curiosity.setMinAge(minAge);
        curiosity.setMaxAge(maxAge);
        curiosity.setTags(tags != null ? tags : Collections.emptyList());
        curiosity.setLocale(locale);
        curiosity.setPhoneticHint(phoneticHint);
        curiosity.setStatus(status);
        curiosity.setCreatedAt(LocalDateTime.now());

        return curiosityRepository.save(curiosity);
    }

    @Override
    @Transactional(readOnly = true)
    public Curiosity getCuriosity(Long id) {
        return curiosityRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Curiosity not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Curiosity> listCuriosities() {
        return curiosityRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Curiosity> listCuriositiesByTopic(Long topicId) {
        return curiosityRepository.findByTopicId(topicId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Curiosity> listActiveCuriositiesByFilters(Long topicId, Integer age, String locale) {
        return curiosityRepository.findActiveByFilters(topicId, age, locale);
    }

    @Override
    public Curiosity updateCuriosity(Long id, String text, Long topicId, Integer minAge, Integer maxAge, List<String> tags, String locale, String phoneticHint, ContentStatus status) {
        curiosityValidator.validateForUpdate(text, minAge, maxAge, locale, status);

        var existing = curiosityRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Curiosity not found with id: " + id));

        if (topicId != null && topicRepository.findById(topicId).isEmpty()) {
            throw new ResourceNotFoundException("Topic not found with id: " + topicId);
        }

        existing.setText(text);
        existing.setTopicId(topicId);
        existing.setMinAge(minAge);
        existing.setMaxAge(maxAge);
        existing.setTags(tags != null ? tags : Collections.emptyList());
        existing.setLocale(locale);
        existing.setPhoneticHint(phoneticHint);
        existing.setStatus(status);
        existing.setUpdatedAt(LocalDateTime.now());

        return curiosityRepository.save(existing);
    }
}
