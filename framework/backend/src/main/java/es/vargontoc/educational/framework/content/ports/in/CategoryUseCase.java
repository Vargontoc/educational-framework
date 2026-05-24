package es.vargontoc.educational.framework.content.ports.in;

import es.vargontoc.educational.framework.content.model.Category;
import es.vargontoc.educational.framework.content.model.ContentStatus;

import java.util.List;

public interface CategoryUseCase {

    Category createCategory(String name, String description, ContentStatus status, Integer displayOrder, String iconUrl);

    Category getCategory(Long id);

    List<Category> listCategories();

    Category updateCategory(Long id, String name, String description, ContentStatus status, Integer displayOrder, String iconUrl);
}
