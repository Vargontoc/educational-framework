package es.vargontoc.educational.framework.content.service;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.TracingPattern;
import es.vargontoc.educational.framework.content.ports.in.TracingPatternUseCase;
import es.vargontoc.educational.framework.content.ports.out.TopicRepository;
import es.vargontoc.educational.framework.content.ports.out.TracingPatternRepository;
import es.vargontoc.educational.framework.content.validation.TracingPatternValidator;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Transactional
public class TracingPatternService implements TracingPatternUseCase {

    private final TracingPatternRepository tracingPatternRepository;
    private final TopicRepository topicRepository;
    private final TracingPatternValidator tracingPatternValidator;

    public TracingPatternService(TracingPatternRepository tracingPatternRepository, TopicRepository topicRepository) {
        this.tracingPatternRepository = tracingPatternRepository;
        this.topicRepository = topicRepository;
        this.tracingPatternValidator = new TracingPatternValidator();
    }

    @Override
    public TracingPattern createTracingPattern(Long topicId, String name, String description, String patternType, List<List<Double>> points, Integer minAge, Integer maxAge, ContentStatus status) {
        tracingPatternValidator.validateForCreate(topicId, name, points, status);

        if (topicRepository.findById(topicId).isEmpty()) {
            throw new ResourceNotFoundException("Topic not found with id: " + topicId);
        }

        var tracingPattern = new TracingPattern();
        tracingPattern.setTopicId(topicId);
        tracingPattern.setName(name);
        tracingPattern.setDescription(description);
        tracingPattern.setPatternType(patternType);
        tracingPattern.setPoints(points);
        tracingPattern.setMinAge(minAge);
        tracingPattern.setMaxAge(maxAge);
        tracingPattern.setStatus(status);
        tracingPattern.setCreatedAt(LocalDateTime.now());

        return tracingPatternRepository.save(tracingPattern);
    }

    @Override
    @Transactional(readOnly = true)
    public TracingPattern getTracingPattern(Long id) {
        return tracingPatternRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("TracingPattern not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TracingPattern> listTracingPatterns() {
        return tracingPatternRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TracingPattern> listTracingPatternsByTopic(Long topicId) {
        return tracingPatternRepository.findByTopicId(topicId);
    }

    @Override
    public TracingPattern updateTracingPattern(Long id, Long topicId, String name, String description, String patternType, List<List<Double>> points, Integer minAge, Integer maxAge, ContentStatus status) {
        tracingPatternValidator.validateForUpdate(topicId, name, points, status);

        var existing = tracingPatternRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("TracingPattern not found with id: " + id));

        if (topicRepository.findById(topicId).isEmpty()) {
            throw new ResourceNotFoundException("Topic not found with id: " + topicId);
        }

        existing.setTopicId(topicId);
        existing.setName(name);
        existing.setDescription(description);
        existing.setPatternType(patternType);
        existing.setPoints(points);
        existing.setMinAge(minAge);
        existing.setMaxAge(maxAge);
        existing.setStatus(status);
        existing.setUpdatedAt(LocalDateTime.now());

        return tracingPatternRepository.save(existing);
    }
}
