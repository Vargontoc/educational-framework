package es.vargontoc.educational.framework.content.infrastructure.persistence;

import es.vargontoc.educational.framework.content.model.Category;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.ports.out.CategoryRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CategoryPersistenceAdapter implements CategoryRepository {

    private final CategoryJpaRepository jpaRepository;

    public CategoryPersistenceAdapter(CategoryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Category> findById(Long id) {
        return jpaRepository.findById(id)
            .map(CategoryPersistenceAdapter::toDomain);
    }

    @Override
    public List<Category> findAll() {
        return jpaRepository.findAll().stream()
            .map(CategoryPersistenceAdapter::toDomain)
            .toList();
    }

    @Override
    public Category save(Category category) {
        var saved = jpaRepository.save(toJpa(category));
        return toDomain(saved);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.findByName(name).isPresent();
    }

    private static Category toDomain(CategoryJpaEntity source) {
        var target = new Category();
        target.setId(source.getId());
        target.setName(source.getName());
        target.setDescription(source.getDescription());
        target.setStatus(ContentStatus.valueOf(source.getStatus()));
        target.setDisplayOrder(source.getDisplayOrder());
        target.setIconUrl(source.getIconUrl());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    private static CategoryJpaEntity toJpa(Category source) {
        var target = new CategoryJpaEntity();
        target.setId(source.getId());
        target.setName(source.getName());
        target.setDescription(source.getDescription());
        target.setStatus(source.getStatus().name());
        target.setDisplayOrder(source.getDisplayOrder());
        target.setIconUrl(source.getIconUrl());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }
}
