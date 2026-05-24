package es.vargontoc.educational.framework.content.service;

import es.vargontoc.educational.framework.content.model.Category;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.ports.out.CategoryRepository;
import es.vargontoc.educational.framework.shared.exception.ConflictException;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryService(categoryRepository);
    }

    @Test
    void createCategory_happyPath() {
        when(categoryRepository.existsByName("Mathematics")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = categoryService.createCategory("Mathematics", "Math concepts", ContentStatus.ACTIVE, 1, "/icons/math.png");

        assertEquals("Mathematics", result.getName());
        assertEquals("Math concepts", result.getDescription());
        assertEquals(ContentStatus.ACTIVE, result.getStatus());
        assertEquals(1, result.getDisplayOrder());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void createCategory_duplicateName_throwsConflict() {
        when(categoryRepository.existsByName("Mathematics")).thenReturn(true);

        assertThrows(ConflictException.class, () ->
            categoryService.createCategory("Mathematics", "Math concepts", ContentStatus.ACTIVE, 1, null));
    }

    @Test
    void createCategory_blankName_throwsValidation() {
        assertThrows(ValidationException.class, () ->
            categoryService.createCategory(" ", "Math concepts", ContentStatus.ACTIVE, 1, null));
    }

    @Test
    void getCategory_happyPath() {
        var category = new Category();
        category.setId(1L);
        category.setName("Mathematics");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        var result = categoryService.getCategory(1L);

        assertEquals("Mathematics", result.getName());
    }

    @Test
    void getCategory_notFound_throwsResourceNotFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.getCategory(99L));
    }

    @Test
    void listCategories_returnsAll() {
        when(categoryRepository.findAll()).thenReturn(List.of(new Category(), new Category()));

        var result = categoryService.listCategories();

        assertEquals(2, result.size());
    }

    @Test
    void updateCategory_happyPath() {
        var existing = new Category();
        existing.setId(1L);
        existing.setName("Old Name");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = categoryService.updateCategory(1L, "New Name", "New Desc", ContentStatus.ACTIVE, 2, null);

        assertEquals("New Name", result.getName());
        assertEquals("New Desc", result.getDescription());
        assertNotNull(result.getUpdatedAt());
    }
}
