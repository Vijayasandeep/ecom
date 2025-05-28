package com.ecommerce.repository;

import com.ecommerce.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // Find cart items by user
    List<CartItem> findByUserId(Long userId);
    List<CartItem> findByUserIdOrderByIdDesc(Long userId);

    // Find specific cart item
    Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId);

    // Count cart items
    int countByUserId(Long userId);

    // Delete operations
    void deleteByUserId(Long userId);
    void deleteByUserIdAndProductId(Long userId, Long productId);

    // Check if item exists
    boolean existsByUserIdAndProductId(Long userId, Long productId);

    // Find cart items by product (for inventory management)
    List<CartItem> findByProductId(Long productId);

    // Custom queries for cart analytics
    @Query("SELECT SUM(ci.quantity) FROM CartItem ci WHERE ci.user.id = :userId")
    Integer getTotalQuantityByUserId(@Param("userId") Long userId);

    @Query("SELECT ci FROM CartItem ci JOIN FETCH ci.product WHERE ci.user.id = :userId")
    List<CartItem> findByUserIdWithProduct(@Param("userId") Long userId);

    // Clear expired guest carts (if implementing guest cart functionality)
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.user.id IS NULL AND ci.id < :cutoffId")
    void deleteExpiredGuestCartItems(@Param("cutoffId") Long cutoffId);

    // Find cart items with low stock products
    @Query("SELECT ci FROM CartItem ci " +
            "JOIN ci.product p " +
            "WHERE ci.user.id = :userId " +
            "AND p.trackQuantity = true " +
            "AND p.stockQuantity < ci.quantity")
    List<CartItem> findCartItemsWithInsufficientStock(@Param("userId") Long userId);

    // Find cart items with inactive products
    @Query("SELECT ci FROM CartItem ci " +
            "JOIN ci.product p " +
            "WHERE ci.user.id = :userId " +
            "AND p.active = false")
    List<CartItem> findCartItemsWithInactiveProducts(@Param("userId") Long userId);
}