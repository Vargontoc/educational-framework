package es.vargontoc.educational.framework.content.ports.out;

import es.vargontoc.educational.framework.content.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {

    Optional<Category> findById(Long id);

    List<Category> findAll();

    Category save(Category category);

    boolean existsByName(String name);
}
