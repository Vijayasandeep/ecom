package com.ecommerce.controller;

import com.ecommerce.entity.Review;
import com.ecommerce.entity.User;
import com.ecommerce.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping
    public ResponseEntity<Review> createReview(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid CreateReviewRequest request) {
        Review review = reviewService.createReview(user.getId(), request.getProductId(),
                request.getRating(), request.getComment());
        return ResponseEntity.ok(review);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<Review>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviews = reviewService.getProductReviews(productId, pageable, sortBy, sortDir);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<Review> getReviewById(@PathVariable Long reviewId) {
        Optional<Review> review = reviewService.getReviewById(reviewId);
        return review.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<Review> updateReview(
            @AuthenticationPrincipal User user,
            @PathVariable Long reviewId,
            @RequestBody @Valid UpdateReviewRequest request) {
        Review review = reviewService.updateReview(reviewId, user.getId(),
                request.getRating(), request.getComment());
        return ResponseEntity.ok(review);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @AuthenticationPrincipal User user,
            @PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId, user.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user")
    public ResponseEntity<Page<Review>> getUserReviews(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviews = reviewService.getUserReviews(user.getId(), pageable);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/product/{productId}/stats")
    public ResponseEntity<ReviewStats> getReviewStats(@PathVariable Long productId) {
        ReviewStats stats = reviewService.getReviewStats(productId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/product/{productId}/summary")
    public ResponseEntity<ReviewSummary> getReviewSummary(@PathVariable Long productId) {
        ReviewSummary summary = reviewService.getReviewSummary(productId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/product/{productId}/rating/{rating}")
    public ResponseEntity<Page<Review>> getReviewsByRating(
            @PathVariable Long productId,
            @PathVariable int rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviews = reviewService.getReviewsByRating(productId, rating, pageable);
        return ResponseEntity.ok(reviews);
    }

    @PostMapping("/{reviewId}/helpful")
    public ResponseEntity<Void> markReviewHelpful(
            @AuthenticationPrincipal User user,
            @PathVariable Long reviewId) {
        reviewService.markReviewHelpful(reviewId, user.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{reviewId}/report")
    public ResponseEntity<Void> reportReview(
            @AuthenticationPrincipal User user,
            @PathVariable Long reviewId,
            @RequestBody @Valid ReportReviewRequest request) {
        reviewService.reportReview(reviewId, user.getId(), request.getReason());
        return ResponseEntity.ok().build();
    }

    // DTOs for review operations
    public static class CreateReviewRequest {
        private Long productId;
        private int rating;
        private String comment;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public int getRating() { return rating; }
        public void setRating(int rating) { this.rating = rating; }
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
    }

    public static class UpdateReviewRequest {
        private int rating;
        private String comment;

        public int getRating() { return rating; }
        public void setRating(int rating) { this.rating = rating; }
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
    }

    public static class ReportReviewRequest {
        private String reason;

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class ReviewStats {
        private double averageRating;
        private int totalReviews;
        private Map<Integer, Integer> ratingDistribution;

        public ReviewStats(double averageRating, int totalReviews, Map<Integer, Integer> ratingDistribution) {
            this.averageRating = averageRating;
            this.totalReviews = totalReviews;
            this.ratingDistribution = ratingDistribution;
        }

        public double getAverageRating() { return averageRating; }
        public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
        public int getTotalReviews() { return totalReviews; }
        public void setTotalReviews(int totalReviews) { this.totalReviews = totalReviews; }
        public Map<Integer, Integer> getRatingDistribution() { return ratingDistribution; }
        public void setRatingDistribution(Map<Integer, Integer> ratingDistribution) { this.ratingDistribution = ratingDistribution; }
    }

    public static class ReviewSummary {
        private double averageRating;
        private int totalReviews;
        private List<Review> featuredReviews;
        private Map<Integer, Double> ratingPercentages;

        public ReviewSummary(double averageRating, int totalReviews, List<Review> featuredReviews, Map<Integer, Double> ratingPercentages) {
            this.averageRating = averageRating;
            this.totalReviews = totalReviews;
            this.featuredReviews = featuredReviews;
            this.ratingPercentages = ratingPercentages;
        }

        public double getAverageRating() { return averageRating; }
        public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
        public int getTotalReviews() { return totalReviews; }
        public void setTotalReviews(int totalReviews) { this.totalReviews = totalReviews; }
        public List<Review> getFeaturedReviews() { return featuredReviews; }
        public void setFeaturedReviews(List<Review> featuredReviews) { this.featuredReviews = featuredReviews; }
        public Map<Integer, Double> getRatingPercentages() { return ratingPercentages; }
        public void setRatingPercentages(Map<Integer, Double> ratingPercentages) { this.ratingPercentages = ratingPercentages; }
    }
}