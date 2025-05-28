package com.ecommerce.repository;

import com.ecommerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Basic queries
    Page<Product> findByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);
    List<Product> findByFeaturedTrueAndActiveTrue(Pageable pageable);
    List<Product> findByActiveTrue(Pageable pageable);
    List<Product> findByActiveTrue(Sort sort);
    List<Product> findByActiveFalse(Sort sort);
    List<Product> findByStockQuantityAndTrackQuantityTrue(int stockQuantity);

    // Search functionality
    @Query("SELECT p FROM Product p WHERE " +
            "p.active = true AND (" +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.searchKeywords) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Product> searchProducts(@Param("query") String query, Pageable pageable);

    // Complex filtering
    @Query("SELECT p FROM Product p WHERE " +
            "p.active = true AND " +
            "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
            "(:featured IS NULL OR p.featured = :featured)")
    Page<Product> findProductsWithFilters(@Param("name") String name,
                                          @Param("categoryId") Long categoryId,
                                          @Param("minPrice") BigDecimal minPrice,
                                          @Param("maxPrice") BigDecimal maxPrice,
                                          @Param("featured") Boolean featured,
                                          Pageable pageable);

    // Best selling products
    @Query("SELECT p FROM Product p " +
            "JOIN p.orderItems oi " +
            "WHERE p.active = true " +
            "GROUP BY p.id " +
            "ORDER BY SUM(oi.quantity) DESC")
    List<Product> findBestSellingProducts(Pageable pageable);

    // Related products
    @Query("SELECT p FROM Product p WHERE " +
            "p.category.id = :categoryId AND " +
            "p.id != :productId AND " +
            "p.active = true " +
            "ORDER BY p.avgRating DESC, p.viewCount DESC")
    List<Product> findRelatedProducts(@Param("categoryId") Long categoryId,
                                      @Param("productId") Long productId,
                                      Pageable pageable);

    // Low stock products
    @Query("SELECT p FROM Product p WHERE " +
            "p.trackQuantity = true AND " +
            "p.stockQuantity <= p.lowStockThreshold AND " +
            "p.active = true")
    List<Product> findLowStockProducts();

    // Trending products (based on recent views and orders)
    @Query("SELECT p FROM Product p WHERE p.active = true " +
            "ORDER BY (p.viewCount * 0.3 + p.orderCount * 0.7) DESC")
    List<Product> findTrendingProducts(Pageable pageable);

    // Recommended products for user (simplified recommendation)
    @Query("SELECT DISTINCT p FROM Product p " +
            "JOIN p.category c " +
            "JOIN p.orderItems oi " +
            "JOIN oi.order o " +
            "WHERE o.user.id IN (" +
            "   SELECT DISTINCT o2.user.id FROM Order o2 " +
            "   JOIN o2.orderItems oi2 " +
            "   JOIN oi2.product p2 " +
            "   WHERE p2.id IN (" +
            "       SELECT p3.id FROM Product p3 " +
            "       JOIN p3.orderItems oi3 " +
            "       JOIN oi3.order o3 " +
            "       WHERE o3.user.id = :userId" +
            "   )" +
            ") AND o.user.id != :userId AND p.active = true " +
            "ORDER BY p.avgRating DESC")
    List<Product> findRecommendedProducts(@Param("userId") Long userId, Pageable pageable);

    // Price range queries
    List<Product> findByPriceBetweenAndActiveTrue(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    // Category and status combinations
    @Query("SELECT p FROM Product p WHERE " +
            "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
            "(:active IS NULL OR p.active = :active)")
    Page<Product> findByCategoryAndStatus(@Param("categoryId") Long categoryId,
                                          @Param("active") Boolean active,
                                          Pageable pageable);

    // Analytics queries
    @Query("SELECT COUNT(p) FROM Product p WHERE p.active = true")
    long countActiveProducts();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.stockQuantity = 0 AND p.trackQuantity = true")
    long countOutOfStockProducts();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.stockQuantity <= p.lowStockThreshold AND p.trackQuantity = true")
    long countLowStockProducts();

    @Query("SELECT AVG(p.price) FROM Product p WHERE p.active = true")
    BigDecimal getAverageProductPrice();

    // SKU and barcode lookups
    Product findBySkuAndActiveTrue(String sku);
    Product findByBarcodeAndActiveTrue(String barcode);
    boolean existsBySku(String sku);
    boolean existsByBarcode(String barcode);
}