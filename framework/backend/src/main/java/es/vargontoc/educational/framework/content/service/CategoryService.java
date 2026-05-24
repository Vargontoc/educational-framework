package es.vargontoc.educational.framework.content.service;

import es.vargontoc.educational.framework.content.model.Category;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.ports.in.CategoryUseCase;
import es.vargontoc.educational.framework.content.ports.out.CategoryRepository;
import es.vargontoc.educational.framework.content.validation.CategoryValidator;
import es.vargontoc.educational.framework.shared.exception.ConflictException;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Transactional
public class CategoryService implements CategoryUseCase {

    private final CategoryRepository categoryRepository;
    private final CategoryValidator categoryValidator;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
        this.categoryValidator = new CategoryValidator();
    }

    @Override
    public Category createCategory(String name, String description, ContentStatus status, Integer displayOrder, String iconUrl) {
        categoryValidator.validateForCreate(name, status);

        if (categoryRepository.existsByName(name)) {
            throw new ConflictException("Category with name '" + name + "' already exists");
        }

        var category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setStatus(status);
        category.setDisplayOrder(displayOrder);
        category.setIconUrl(iconUrl);
        category.setCreatedAt(LocalDateTime.now());

        return categoryRepository.save(category);
    }

    @Override
    @Transactional(readOnly = true)
    public Category getCategory(Long id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> listCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Category updateCategory(Long id, String name, String description, ContentStatus status, Integer displayOrder, String iconUrl) {
        categoryValidator.validateForUpdate(name, status);

        var existing = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        existing.setName(name);
        existing.setDescription(description);
        existing.setStatus(status);
        existing.setDisplayOrder(displayOrder);
        existing.setIconUrl(iconUrl);
        existing.setUpdatedAt(LocalDateTime.now());

        return categoryRepository.save(existing);
    }
}
