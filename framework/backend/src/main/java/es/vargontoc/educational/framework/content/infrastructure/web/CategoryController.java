package es.vargontoc.educational.framework.content.infrastructure.web;

import es.vargontoc.educational.framework.content.infrastructure.dto.CategoryResponse;
import es.vargontoc.educational.framework.content.infrastructure.dto.CreateCategoryRequest;
import es.vargontoc.educational.framework.content.infrastructure.dto.UpdateCategoryRequest;
import es.vargontoc.educational.framework.content.model.Category;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.ports.in.CategoryUseCase;
import es.vargontoc.educational.framework.shared.api.ApiResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Profile("dev")
@RequestMapping("/api/v1/dev/content/categories")
public class CategoryController {

    private final CategoryUseCase categoryUseCase;

    public CategoryController(CategoryUseCase categoryUseCase) {
        this.categoryUseCase = categoryUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@RequestBody CreateCategoryRequest request) {
        var category = categoryUseCase.createCategory(
            request.name(),
            request.description(),
            ContentStatus.valueOf(request.status()),
            request.displayOrder(),
            request.iconUrl()
        );
        return ResponseEntity.status(201).body(ApiResponse.created(toResponse(category)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> listCategories() {
        var categories = categoryUseCase.listCategories().stream()
            .map(CategoryController::toResponse)
            .toList();
        return ResponseEntity.ok(ApiResponse.ok(categories));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategory(@PathVariable Long id) {
        var category = categoryUseCase.getCategory(id);
        return ResponseEntity.ok(ApiResponse.ok(toResponse(category)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
        @PathVariable Long id,
        @RequestBody UpdateCategoryRequest request
    ) {
        var category = categoryUseCase.updateCategory(
            id,
            request.name(),
            request.description(),
            ContentStatus.valueOf(request.status()),
            request.displayOrder(),
            request.iconUrl()
        );
        return ResponseEntity.ok(ApiResponse.ok(toResponse(category)));
    }

    private static CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
            category.getId(),
            category.getName(),
            category.getDescription(),
            category.getStatus().name(),
            category.getDisplayOrder(),
            category.getIconUrl(),
            category.getCreatedAt(),
            category.getUpdatedAt()
        );
    }
}
