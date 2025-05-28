package com.ecommerce.service;

import com.ecommerce.controller.ReviewController.ReviewStats;
import com.ecommerce.controller.ReviewController.ReviewSummary;
import com.ecommerce.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public interface ReviewService {

    // Review CRUD operations
    Review createReview(Long userId, Long productId, int rating, String comment);
    Review updateReview(Long reviewId, Long userId, int rating, String comment);
    void deleteReview(Long reviewId, Long userId);
    Optional<Review> getReviewById(Long reviewId);

    // Review retrieval
    Page<Review> getProductReviews(Long productId, Pageable pageable, String sortBy, String sortDir);
    Page<Review> getUserReviews(Long userId, Pageable pageable);
    Page<Review> getReviewsByRating(Long productId, int rating, Pageable pageable);

    // Review statistics
    ReviewStats getReviewStats(Long productId);
    ReviewSummary getReviewSummary(Long productId);
    double getAverageRating(Long productId);
    int getTotalReviewCount(Long productId);

    // Review interactions
    void markReviewHelpful(Long reviewId, Long userId);
    void reportReview(Long reviewId, Long userId, String reason);

    // Review validation
    boolean canUserReviewProduct(Long userId, Long productId);
    boolean hasUserReviewedProduct(Long userId, Long productId);

    // Review moderation
    List<Review> getReportedReviews();
    void approveReview(Long reviewId);
    void rejectReview(Long reviewId, String reason);

    // Review analytics
    List<Review> getFeaturedReviews(Long productId, int limit);
    List<Review> getRecentReviews(int limit);
    List<Review> getTopRatedReviews(Long productId, int limit);

    // Bulk operations
    void deleteReviewsByProduct(Long productId);
    void deleteReviewsByUser(Long userId);
}