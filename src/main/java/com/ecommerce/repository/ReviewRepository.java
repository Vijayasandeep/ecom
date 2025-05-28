package com.ecommerce.repository;

import com.ecommerce.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Basic review queries
    Page<Review> findByProductIdOrderByCreatedAtDesc(Long productId, Pageable pageable);
    Page<Review> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Optional<Review> findByUserIdAndProductId(Long userId, Long productId);

    // Review filtering
//    Page<Review> findByProductIdAndRatingOrderByCreatedAtDesc(Long productId, int rating, Pageable pageable);
    List<Review> findByProductIdAndRatingGreaterThanEqualOrderByCreatedAtDesc(Long productId, int rating);

    // Review statistics
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Double getAverageRatingByProductId(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId")
    Long getReviewCountByProductId(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.user.id = :userId")
    Long getReviewCountByUserId(@Param("userId") Long userId);

    // Rating distribution
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.product.id = :productId GROUP BY r.rating ORDER BY r.rating DESC")
    List<Object[]> getRatingDistribution(@Param("productId") Long productId);

    // Featured reviews (high rating with comments)
    @Query("SELECT r FROM Review r WHERE r.product.id = :productId AND r.rating >= 4 AND r.comment IS NOT NULL AND LENGTH(r.comment) > 20 ORDER BY r.rating DESC, r.createdAt DESC")
    List<Review> findFeaturedReviews(@Param("productId") Long productId, Pageable pageable);

    // Recent reviews
    @Query("SELECT r FROM Review r ORDER BY r.createdAt DESC")
    List<Review> findRecentReviews(Pageable pageable);

    // Top rated reviews for a product
//    List<Review> findByProductIdAndRatingOrderByCreatedAtDesc(Long productId, int rating, Pageable pageable);

    // User review validation
    boolean existsByUserIdAndProductId(Long userId, Long productId);

    // Reviews by date range
    List<Review> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
//    List<Review> findByProductIdAndCreatedAtBetween(Long productId, LocalDateTime startDate, LocalDateTime endDate);

    // Reviews with comments
    @Query("SELECT r FROM Review r WHERE r.product.id = :productId AND r.comment IS NOT NULL AND LENGTH(r.comment) > 0 ORDER BY r.createdAt DESC")
    Page<Review> findReviewsWithComments(@Param("productId") Long productId, Pageable pageable);

    // High rating reviews
    @Query("SELECT r FROM Review r WHERE r.product.id = :productId AND r.rating >= :minRating ORDER BY r.rating DESC, r.createdAt DESC")
    List<Review> findHighRatingReviews(@Param("productId") Long productId, @Param("minRating") int minRating, Pageable pageable);

    // Reviews for moderation (now that Review entity has proper fields)
    @Query("SELECT r FROM Review r WHERE r.approved = false ORDER BY r.createdAt ASC")
    List<Review> findPendingReviews();

    @Query("SELECT r FROM Review r WHERE r.reported = true ORDER BY r.createdAt ASC")
    List<Review> findReportedReviews();

    // Product reviews summary
    @Query("SELECT r.rating, COUNT(r), AVG(CASE WHEN r.comment IS NOT NULL THEN LENGTH(r.comment) ELSE 0 END) " +
            "FROM Review r WHERE r.product.id = :productId GROUP BY r.rating ORDER BY r.rating DESC")
    List<Object[]> getProductReviewSummary(@Param("productId") Long productId);

    // User activity
    @Query("SELECT COUNT(r) FROM Review r WHERE r.user.id = :userId AND r.createdAt >= :since")
    Long getRecentReviewCountByUser(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    // Delete operations
    void deleteByProductId(Long productId);
    void deleteByUserId(Long userId);

    // Reviews by multiple products
    @Query("SELECT r FROM Review r WHERE r.product.id IN :productIds ORDER BY r.createdAt DESC")
    List<Review> findByProductIds(@Param("productIds") List<Long> productIds);

    // Search reviews by comment content
    @Query("SELECT r FROM Review r WHERE r.product.id = :productId AND LOWER(r.comment) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ORDER BY r.createdAt DESC")
    Page<Review> searchReviewsByComment(@Param("productId") Long productId, @Param("searchTerm") String searchTerm, Pageable pageable);

    List<Review> findByProductIdAndRatingOrderByCreatedAtDesc(Long productId, int i, Pageable pageable);
}