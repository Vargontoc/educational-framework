package es.vargontoc.educational.framework.content.service;

import es.vargontoc.educational.framework.content.model.Biome;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.RecognitionType;
import es.vargontoc.educational.framework.content.model.Topic;
import es.vargontoc.educational.framework.content.ports.in.TopicUseCase;
import es.vargontoc.educational.framework.content.ports.out.CategoryRepository;
import es.vargontoc.educational.framework.content.ports.out.TopicRepository;
import es.vargontoc.educational.framework.content.validation.TopicValidator;
import es.vargontoc.educational.framework.shared.exception.ConflictException;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Transactional
public class TopicService implements TopicUseCase {

    private final TopicRepository topicRepository;
    private final CategoryRepository categoryRepository;
    private final TopicValidator topicValidator;

    public TopicService(TopicRepository topicRepository, CategoryRepository categoryRepository) {
        this.topicRepository = topicRepository;
        this.categoryRepository = categoryRepository;
        this.topicValidator = new TopicValidator();
    }

    @Override
    public Topic createTopic(String name, String description, Long categoryId, ContentStatus status, Integer minAge, Integer maxAge, List<String> compatibleVariants) {
        topicValidator.validateForCreate(name, categoryId, status, minAge, maxAge);

        if (!categoryRepository.findById(categoryId).isPresent()) {
            throw new ResourceNotFoundException("Category not found with id: " + categoryId);
        }

        if (topicRepository.existsByNameAndCategoryId(name, categoryId)) {
            throw new ConflictException("Topic with name '" + name + "' already exists in this category");
        }

        var topic = new Topic();
        topic.setName(name);
        topic.setDescription(description);
        topic.setCategoryId(categoryId);
        topic.setStatus(status);
        topic.setMinAge(minAge);
        topic.setMaxAge(maxAge);
        topic.setCompatibleVariants(compatibleVariants != null ? compatibleVariants : Collections.emptyList());
        topic.setCreatedAt(LocalDateTime.now());

        return topicRepository.save(topic);
    }

    @Override
    @Transactional(readOnly = true)
    public Topic getTopic(Long id) {
        return topicRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Topic> listTopics() {
        return topicRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Topic> listTopicsByCategory(Long categoryId) {
        return topicRepository.findByCategoryId(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Topic> listTopicsByRecognitionType(RecognitionType recognitionType) {
        return topicRepository.findByRecognitionType(recognitionType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Topic> listTopicsByRecognitionTypeAndHabitat(RecognitionType recognitionType, Biome habitatTag) {
        return topicRepository.findByRecognitionTypeAndHabitatTag(recognitionType, habitatTag);
    }

    @Override
    public Topic updateTopic(Long id, String name, String description, Long categoryId, ContentStatus status, Integer minAge, Integer maxAge, List<String> compatibleVariants) {
        topicValidator.validateForUpdate(name, categoryId, status, minAge, maxAge);

        var existing = topicRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + id));

        existing.setName(name);
        existing.setDescription(description);
        existing.setCategoryId(categoryId);
        existing.setStatus(status);
        existing.setMinAge(minAge);
        existing.setMaxAge(maxAge);
        existing.setCompatibleVariants(compatibleVariants != null ? compatibleVariants : Collections.emptyList());
        existing.setUpdatedAt(LocalDateTime.now());

        return topicRepository.save(existing);
    }
}
