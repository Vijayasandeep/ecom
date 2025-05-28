package com.ecommerce.service;

import com.ecommerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
@Service
public interface ProductService {

    // Basic CRUD operations
    Product createProduct(Product product);
    Product updateProduct(Long id, Product product);
    Optional<Product> getProductById(Long id);
    void deleteProduct(Long id);

    // Product listing and filtering
    Page<Product> getAllProducts(Pageable pageable, String name, Long categoryId,
                                 BigDecimal minPrice, BigDecimal maxPrice, Boolean featured);
    Page<Product> searchProducts(String query, Pageable pageable);
    Page<Product> getProductsByCategory(Long categoryId, Pageable pageable);

    // Featured and special products
    List<Product> getFeaturedProducts(int limit);
    List<Product> getLatestProducts(int limit);
    List<Product> getBestSellingProducts(int limit);
    List<Product> getRelatedProducts(Long productId, int limit);

    // Product interactions
    void incrementViewCount(Long productId);
    void updateRating(Long productId, double newRating, int reviewCount);

    // Inventory management
    List<Product> getLowStockProducts();
    List<Product> getOutOfStockProducts();
    void updateStock(Long productId, int quantity);
    boolean isInStock(Long productId, int quantity);

    // Product images
    Product addProductImage(Long productId, String imageUrl);
    Product removeProductImage(Long productId, String imageUrl);
    Product setMainImage(Long productId, String imageUrl);

    // Admin operations
    Product toggleProductStatus(Long productId);
    Product toggleFeaturedStatus(Long productId);
    List<Product> getProductsForAdmin(String status, String sortBy, String sortDir);

    // Analytics
    List<Product> getTrendingProducts(int limit);
    List<Product> getRecommendedProducts(Long userId, int limit);
}