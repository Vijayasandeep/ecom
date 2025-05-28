package com.ecommerce.repository;

import com.ecommerce.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Basic queries
    List<Category> findAllByOrderByDisplayOrderAscNameAsc();
    List<Category> findByActiveTrueOrderByDisplayOrderAscNameAsc();
    List<Category> findByParentIsNullAndActiveTrueOrderByDisplayOrderAscNameAsc();
    List<Category> findByParentIdAndActiveTrueOrderByDisplayOrderAscNameAsc(Long parentId);

    // Slug queries
    Optional<Category> findBySlug(String slug);
    Optional<Category> findBySlugAndActiveTrue(String slug);
    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, Long id);

    // Parent-child relationships
    List<Category> findByParentId(Long parentId);
    List<Category> findByParentIsNull();

    // Active/inactive queries
    long countByActiveTrue();
    long countByActiveFalse();

    // Search functionality
    @Query("SELECT c FROM Category c WHERE " +
            "c.active = true AND (" +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Category> searchCategories(@Param("query") String query);

    // Popular categories (based on product count)
    @Query("SELECT c FROM Category c " +
            "JOIN c.products p " +
            "WHERE c.active = true AND p.active = true " +
            "GROUP BY c.id " +
            "ORDER BY COUNT(p) DESC " +
            "LIMIT :limit")
    List<Category> findPopularCategories(@Param("limit") int limit);

    // Categories with products
    @Query("SELECT DISTINCT c FROM Category c " +
            "JOIN c.products p " +
            "WHERE c.active = true AND p.active = true " +
            "ORDER BY c.displayOrder ASC, c.name ASC")
    List<Category> findCategoriesWithProducts();

    // Count products in category (including subcategories)
    @Query("SELECT COUNT(DISTINCT p) FROM Category c " +
            "JOIN c.products p " +
            "WHERE c.id = :categoryId OR c.parent.id = :categoryId")
    long countProductsInCategory(@Param("categoryId") Long categoryId);

    // Find categories by level in hierarchy
    @Query("SELECT c FROM Category c WHERE " +
            "c.active = true AND " +
            "SIZE(c.subcategories) = 0") // Leaf categories
    List<Category> findLeafCategories();

    // Categories with specific display order
    List<Category> findByDisplayOrderBetweenAndActiveTrueOrderByDisplayOrderAsc(int start, int end);

    // Categories by name pattern
    List<Category> findByNameContainingIgnoreCaseAndActiveTrueOrderByNameAsc(String name);

    // Categories with image
    @Query("SELECT c FROM Category c WHERE c.imageUrl IS NOT NULL AND c.active = true")
    List<Category> findCategoriesWithImage();

    // Top-level categories ordered by display order
    @Query("SELECT c FROM Category c WHERE " +
            "c.parent IS NULL AND c.active = true " +
            "ORDER BY c.displayOrder ASC, c.name ASC")
    List<Category> findTopLevelCategories();

    // Categories by parent with product count
    @Query("SELECT c, COUNT(p) as productCount FROM Category c " +
            "LEFT JOIN c.products p " +
            "WHERE c.parent.id = :parentId AND c.active = true " +
            "GROUP BY c.id " +
            "ORDER BY c.displayOrder ASC, c.name ASC")
    List<Object[]> findByParentIdWithProductCount(@Param("parentId") Long parentId);

    // Recently created categories
    @Query("SELECT c FROM Category c WHERE c.active = true " +
            "ORDER BY c.createdAt DESC " +
            "LIMIT :limit")
    List<Category> findRecentCategories(@Param("limit") int limit);
}