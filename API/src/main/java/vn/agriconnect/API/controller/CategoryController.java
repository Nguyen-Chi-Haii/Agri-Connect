package vn.agriconnect.API.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.agriconnect.API.dto.response.ApiResponse;
import vn.agriconnect.API.model.Category;
import vn.agriconnect.API.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<Category>> create(@RequestBody Category category) {
        Category created = categoryService.create(category);
        return ResponseEntity.ok(ApiResponse.success("Category created", created));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<Category>> getById(@PathVariable String categoryId) {
        Category category = categoryService.getById(categoryId);
        return ResponseEntity.ok(ApiResponse.success(category));
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<Category>> update(
            @PathVariable String categoryId,
            @RequestBody Category category) {
        Category updated = categoryService.update(categoryId, category);
        return ResponseEntity.ok(ApiResponse.success("Category updated", updated));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String categoryId) {
        categoryService.delete(categoryId);
        return ResponseEntity.ok(ApiResponse.success("Category deleted", null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> getAll() {
        List<Category> categories = categoryService.getAll();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/parent/{parentId}")
    public ResponseEntity<ApiResponse<List<Category>>> getByParent(@PathVariable String parentId) {
        List<Category> categories = categoryService.getByParent(parentId);
        return ResponseEntity.ok(ApiResponse.success(categories));
    }
}
