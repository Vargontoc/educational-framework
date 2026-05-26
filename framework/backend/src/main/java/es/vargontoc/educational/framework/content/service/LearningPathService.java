package es.vargontoc.educational.framework.content.service;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.LearningPath;
import es.vargontoc.educational.framework.content.ports.in.LearningPathUseCase;
import es.vargontoc.educational.framework.content.ports.out.LearningPathRepository;
import es.vargontoc.educational.framework.content.validation.LearningPathValidator;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Transactional
public class LearningPathService implements LearningPathUseCase {

    private final LearningPathRepository learningPathRepository;
    private final LearningPathValidator learningPathValidator;

    public LearningPathService(LearningPathRepository learningPathRepository) {
        this.learningPathRepository = learningPathRepository;
        this.learningPathValidator = new LearningPathValidator();
    }

    @Override
    public LearningPath createLearningPath(String name, String description, Integer minAge, Integer maxAge, String locale, ContentStatus status) {
        learningPathValidator.validateForCreate(name, minAge, maxAge, locale, status);

        var learningPath = new LearningPath();
        learningPath.setName(name);
        learningPath.setDescription(description);
        learningPath.setMinAge(minAge);
        learningPath.setMaxAge(maxAge);
        learningPath.setLocale(locale);
        learningPath.setStatus(status);
        learningPath.setCreatedAt(LocalDateTime.now());

        return learningPathRepository.save(learningPath);
    }

    @Override
    @Transactional(readOnly = true)
    public LearningPath getLearningPath(Long id) {
        return learningPathRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("LearningPath not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearningPath> listLearningPaths() {
        return learningPathRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearningPath> listLearningPathsByStatus(ContentStatus status) {
        return learningPathRepository.findByStatus(status);
    }

    @Override
    public LearningPath updateLearningPath(Long id, String name, String description, Integer minAge, Integer maxAge, String locale, ContentStatus status) {
        learningPathValidator.validateForUpdate(name, minAge, maxAge, locale, status);

        var existing = learningPathRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("LearningPath not found with id: " + id));

        existing.setName(name);
        existing.setDescription(description);
        existing.setMinAge(minAge);
        existing.setMaxAge(maxAge);
        existing.setLocale(locale);
        existing.setStatus(status);
        existing.setUpdatedAt(LocalDateTime.now());

        return learningPathRepository.save(existing);
    }
}
