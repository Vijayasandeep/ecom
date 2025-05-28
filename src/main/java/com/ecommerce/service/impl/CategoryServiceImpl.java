package com.ecommerce.service.impl;

import com.ecommerce.controller.CategoryController.CategoryHierarchy;
import com.ecommerce.entity.Category;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public Category createCategory(Category category) {
        // Generate slug if not provided
        if (category.getSlug() == null || category.getSlug().isEmpty()) {
            category.setSlug(generateSlug(category.getName()));
        }

        // Ensure slug uniqueness
        if (!isSlugUnique(category.getSlug(), null)) {
            category.setSlug(category.getSlug() + "-" + System.currentTimeMillis());
        }

        // Validate parent category if provided
        if (category.getParent() != null) {
            validateCategoryHierarchy(null, category.getParent().getId());
        }

        return categoryRepository.save(category);
    }

    @Override
    public Category updateCategory(Long id, Category category) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        existingCategory.setName(category.getName());
        existingCategory.setDescription(category.getDescription());
        existingCategory.setImageUrl(category.getImageUrl());
        existingCategory.setDisplayOrder(category.getDisplayOrder());
        existingCategory.setActive(category.getActive());
        existingCategory.setMetaTitle(category.getMetaTitle());
        existingCategory.setMetaDescription(category.getMetaDescription());

        // Update slug if name changed
        if (!existingCategory.getName().equals(category.getName())) {
            String newSlug = generateSlug(category.getName());
            if (!isSlugUnique(newSlug, id)) {
                newSlug = newSlug + "-" + System.currentTimeMillis();
            }
            existingCategory.setSlug(newSlug);
        }

        // Validate parent category change
        if (category.getParent() != null &&
                !category.getParent().getId().equals(existingCategory.getParent().getId())) {
            validateCategoryHierarchy(id, category.getParent().getId());
            existingCategory.setParent(category.getParent());
        }

        return categoryRepository.save(existingCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        // Check if category has subcategories
        if (!category.getSubcategories().isEmpty()) {
            throw new BadRequestException("Cannot delete category with subcategories");
        }

        // Check if category has products
        if (!category.getProducts().isEmpty()) {
            throw new BadRequestException("Cannot delete category with products");
        }

        categoryRepository.delete(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAllByOrderByDisplayOrderAscNameAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getActiveCategories() {
        return categoryRepository.findByActiveTrueOrderByDisplayOrderAscNameAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getRootCategories() {
        return categoryRepository.findByParentIsNullAndActiveTrueOrderByDisplayOrderAscNameAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getSubcategories(Long parentId) {
        return categoryRepository.findByParentIdAndActiveTrueOrderByDisplayOrderAscNameAsc(parentId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Category> getCategoryBySlug(String slug) {
        return categoryRepository.findBySlugAndActiveTrue(slug);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryHierarchy> getCategoryHierarchy() {
        List<Category> rootCategories = getRootCategories();
        return rootCategories.stream()
                .map(this::buildCategoryHierarchy)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getCategoryBreadcrumb(Long categoryId) {
        List<Category> breadcrumb = new ArrayList<>();
        Category category = categoryRepository.findById(categoryId).orElse(null);

        while (category != null) {
            breadcrumb.add(0, category); // Add to beginning
            category = category.getParent();
        }

        return breadcrumb;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> searchCategories(String query) {
        return categoryRepository.searchCategories(query);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getPopularCategories(int limit) {
        return categoryRepository.findPopularCategories(limit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getCategoriesWithProducts() {
        return categoryRepository.findCategoriesWithProducts();
    }

    @Override
    public Category toggleCategoryStatus(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        category.setActive(!category.getActive());
        return categoryRepository.save(category);
    }

    @Override
    public void updateDisplayOrder(Long categoryId, int displayOrder) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        category.setDisplayOrder(displayOrder);
        categoryRepository.save(category);
    }

    @Override
    @Transactional(readOnly = true)
    public long getCategoryProductCount(Long categoryId) {
        return categoryRepository.countProductsInCategory(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalCategoriesCount() {
        return categoryRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long getActiveCategoriesCount() {
        return categoryRepository.countByActiveTrue();
    }

    @Override
    public String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSlugUnique(String slug, Long categoryId) {
        if (categoryId == null) {
            return !categoryRepository.existsBySlug(slug);
        } else {
            return !categoryRepository.existsBySlugAndIdNot(slug, categoryId);
        }
    }

    @Override
    public void validateCategoryHierarchy(Long categoryId, Long parentId) {
        if (categoryId != null && categoryId.equals(parentId)) {
            throw new BadRequestException("Category cannot be its own parent");
        }

        // Check for circular dependency
        Category parent = categoryRepository.findById(parentId).orElse(null);
        while (parent != null) {
            if (categoryId != null && categoryId.equals(parent.getId())) {
                throw new BadRequestException("Circular dependency detected in category hierarchy");
            }
            parent = parent.getParent();
        }
    }

    // Helper method to build category hierarchy
    private CategoryHierarchy buildCategoryHierarchy(Category category) {
        List<CategoryHierarchy> children = category.getSubcategories().stream()
                .filter(Category::getActive)
                .map(this::buildCategoryHierarchy)
                .collect(Collectors.toList());

        return new CategoryHierarchy(category, children);
    }
}