package com.ecommerce.service.impl;

import com.ecommerce.controller.ReviewController.ReviewStats;
import com.ecommerce.controller.ReviewController.ReviewSummary;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.Review;
import com.ecommerce.entity.User;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.ReviewRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.ProductService;
import com.ecommerce.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @Override
    public Review createReview(Long userId, Long productId, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new BadRequestException("Rating must be between 1 and 5");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        // Check if user already reviewed this product
        if (reviewRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new BadRequestException("You have already reviewed this product");
        }

        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setRating(rating);
        review.setComment(comment);
        review.setCreatedAt(LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);

        // Update product rating
        updateProductRating(productId);

        return savedReview;
    }

    @Override
    public Review updateReview(Long reviewId, Long userId, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new BadRequestException("Rating must be between 1 and 5");
        }

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        if (!review.getUser().getId().equals(userId)) {
            throw new BadRequestException("You can only update your own reviews");
        }

        review.setRating(rating);
        review.setComment(comment);

        Review updatedReview = reviewRepository.save(review);

        // Update product rating
        updateProductRating(review.getProduct().getId());

        return updatedReview;
    }

    @Override
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        if (!review.getUser().getId().equals(userId)) {
            throw new BadRequestException("You can only delete your own reviews");
        }

        Long productId = review.getProduct().getId();
        reviewRepository.delete(review);

        // Update product rating
        updateProductRating(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Review> getReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Review> getProductReviews(Long productId, Pageable pageable, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId, sortedPageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Review> getUserReviews(Long userId, Pageable pageable) {
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Review> getReviewsByRating(Long productId, int rating, Pageable pageable) {
        return (Page<Review>) reviewRepository.findByProductIdAndRatingOrderByCreatedAtDesc(productId, rating, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewStats getReviewStats(Long productId) {
        Double averageRating = reviewRepository.getAverageRatingByProductId(productId);
        Long totalReviews = reviewRepository.getReviewCountByProductId(productId);

        List<Object[]> distributionData = reviewRepository.getRatingDistribution(productId);
        Map<Integer, Integer> ratingDistribution = new HashMap<>();

        // Initialize all ratings to 0
        for (int i = 1; i <= 5; i++) {
            ratingDistribution.put(i, 0);
        }

        // Fill in actual data
        for (Object[] data : distributionData) {
            Integer rating = (Integer) data[0];
            Long count = (Long) data[1];
            ratingDistribution.put(rating, count.intValue());
        }

        return new ReviewStats(
                averageRating != null ? averageRating : 0.0,
                totalReviews != null ? totalReviews.intValue() : 0,
                ratingDistribution
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewSummary getReviewSummary(Long productId) {
        ReviewStats stats = getReviewStats(productId);

        // Get featured reviews (high rating with good comments)
        Pageable featuredPageable = PageRequest.of(0, 3);
        List<Review> featuredReviews = reviewRepository.findFeaturedReviews(productId, featuredPageable);

        // Calculate rating percentages
        Map<Integer, Double> ratingPercentages = new HashMap<>();
        int totalReviews = stats.getTotalReviews();

        if (totalReviews > 0) {
            for (Map.Entry<Integer, Integer> entry : stats.getRatingDistribution().entrySet()) {
                double percentage = (entry.getValue().doubleValue() / totalReviews) * 100;
                ratingPercentages.put(entry.getKey(), percentage);
            }
        } else {
            for (int i = 1; i <= 5; i++) {
                ratingPercentages.put(i, 0.0);
            }
        }

        return new ReviewSummary(
                stats.getAverageRating(),
                stats.getTotalReviews(),
                featuredReviews,
                ratingPercentages
        );
    }

    @Override
    @Transactional(readOnly = true)
    public double getAverageRating(Long productId) {
        Double average = reviewRepository.getAverageRatingByProductId(productId);
        return average != null ? average : 0.0;
    }

    @Override
    @Transactional(readOnly = true)
    public int getTotalReviewCount(Long productId) {
        Long count = reviewRepository.getReviewCountByProductId(productId);
        return count != null ? count.intValue() : 0;
    }

    @Override
    public void markReviewHelpful(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        // Implementation would require a separate entity to track helpful votes
        // For now, this is a placeholder
    }

    @Override
    public void reportReview(Long reviewId, Long userId, String reason) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        // Implementation would require a separate entity to track reports
        // For now, this is a placeholder
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canUserReviewProduct(Long userId, Long productId) {
        // Check if user has purchased the product (would need Order/OrderItem check)
        // For now, just check if they haven't already reviewed it
        return !reviewRepository.existsByUserIdAndProductId(userId, productId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserReviewedProduct(Long userId, Long productId) {
        return reviewRepository.existsByUserIdAndProductId(userId, productId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> getReportedReviews() {
        return reviewRepository.findReportedReviews();
    }

    @Override
    public void approveReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        // Set approved status (would need approved field in Review entity)
        reviewRepository.save(review);
    }

    @Override
    public void rejectReview(Long reviewId, String reason) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        // Set rejected status and reason (would need additional fields)
        reviewRepository.save(review);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> getFeaturedReviews(Long productId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return reviewRepository.findFeaturedReviews(productId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> getRecentReviews(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return reviewRepository.findRecentReviews(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> getTopRatedReviews(Long productId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return reviewRepository.findByProductIdAndRatingOrderByCreatedAtDesc(productId, 5, pageable);
    }

    @Override
    public void deleteReviewsByProduct(Long productId) {
        reviewRepository.deleteByProductId(productId);
    }

    @Override
    public void deleteReviewsByUser(Long userId) {
        reviewRepository.deleteByUserId(userId);
    }

    // Helper method to update product rating
    private void updateProductRating(Long productId) {
        double averageRating = getAverageRating(productId);
        int reviewCount = getTotalReviewCount(productId);

        productService.updateRating(productId, averageRating, reviewCount);
    }
}