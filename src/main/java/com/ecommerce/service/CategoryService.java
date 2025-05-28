package com.ecommerce.service;

import com.ecommerce.controller.CategoryController.CategoryHierarchy;
import com.ecommerce.entity.Category;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public interface CategoryService {

    // Basic CRUD operations
    Category createCategory(Category category);
    Category updateCategory(Long id, Category category);
    Optional<Category> getCategoryById(Long id);
    void deleteCategory(Long id);

    // Category retrieval
    List<Category> getAllCategories();
    List<Category> getActiveCategories();
    List<Category> getRootCategories();
    List<Category> getSubcategories(Long parentId);
    Optional<Category> getCategoryBySlug(String slug);

    // Category hierarchy
    List<CategoryHierarchy> getCategoryHierarchy();
    List<Category> getCategoryBreadcrumb(Long categoryId);

    // Category search and filtering
    List<Category> searchCategories(String query);
    List<Category> getPopularCategories(int limit);
    List<Category> getCategoriesWithProducts();

    // Category management
    Category toggleCategoryStatus(Long categoryId);
    void updateDisplayOrder(Long categoryId, int displayOrder);

    // Category statistics
    long getCategoryProductCount(Long categoryId);
    long getTotalCategoriesCount();
    long getActiveCategoriesCount();

    // Utility methods
    String generateSlug(String name);
    boolean isSlugUnique(String slug, Long categoryId);
    void validateCategoryHierarchy(Long categoryId, Long parentId);
}