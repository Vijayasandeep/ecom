package com.ecommerce.controller;

import com.ecommerce.entity.Category;
import com.ecommerce.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        Optional<Category> category = categoryService.getCategoryById(id);
        return category.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<Category> getCategoryBySlug(@PathVariable String slug) {
        Optional<Category> category = categoryService.getCategoryBySlug(slug);
        return category.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/active")
    public ResponseEntity<List<Category>> getActiveCategories() {
        List<Category> categories = categoryService.getActiveCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/root")
    public ResponseEntity<List<Category>> getRootCategories() {
        List<Category> categories = categoryService.getRootCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}/subcategories")
    public ResponseEntity<List<Category>> getSubcategories(@PathVariable Long id) {
        List<Category> subcategories = categoryService.getSubcategories(id);
        return ResponseEntity.ok(subcategories);
    }

    @GetMapping("/hierarchy")
    public ResponseEntity<List<CategoryHierarchy>> getCategoryHierarchy() {
        List<CategoryHierarchy> hierarchy = categoryService.getCategoryHierarchy();
        return ResponseEntity.ok(hierarchy);
    }

    @GetMapping("/{id}/breadcrumb")
    public ResponseEntity<List<Category>> getCategoryBreadcrumb(@PathVariable Long id) {
        List<Category> breadcrumb = categoryService.getCategoryBreadcrumb(id);
        return ResponseEntity.ok(breadcrumb);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<Category>> getPopularCategories(
            @RequestParam(defaultValue = "10") int limit) {
        List<Category> categories = categoryService.getPopularCategories(limit);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Category>> searchCategories(@RequestParam String query) {
        List<Category> categories = categoryService.searchCategories(query);
        return ResponseEntity.ok(categories);
    }

    // DTO for category hierarchy
    public static class CategoryHierarchy {
        private Category category;
        private List<CategoryHierarchy> children;

        public CategoryHierarchy(Category category, List<CategoryHierarchy> children) {
            this.category = category;
            this.children = children;
        }

        public Category getCategory() { return category; }
        public void setCategory(Category category) { this.category = category; }
        public List<CategoryHierarchy> getChildren() { return children; }
        public void setChildren(List<CategoryHierarchy> children) { this.children = children; }
    }
}