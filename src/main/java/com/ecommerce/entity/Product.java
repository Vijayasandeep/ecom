package com.ecommerce.entity;

import com.ecommerce.entity.audit.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products", indexes = {
        @Index(name = "idx_product_name", columnList = "name"),
        @Index(name = "idx_product_category", columnList = "category_id"),
        @Index(name = "idx_product_price", columnList = "price"),
        @Index(name = "idx_product_active", columnList = "active")
})
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    private String name;

    @Column(length = 2000)
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @Column(name = "compare_price", precision = 10, scale = 2)
    private BigDecimal comparePrice;

    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "stock_quantity")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity = 0;

    @Column(name = "low_stock_threshold")
    private Integer lowStockThreshold = 5;

    @Column(name = "track_quantity")
    private Boolean trackQuantity = true;

    @Column(name = "allow_backorder")
    private Boolean allowBackorder = false;

    @Column(name = "image_url")
    private String imageUrl;

    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    private List<String> images = new ArrayList<>();

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "featured")
    private Boolean featured = false;

    @Column(name = "weight")
    private BigDecimal weight;

    @Column(name = "dimensions")
    private String dimensions;

    @Column(name = "sku", unique = true)
    private String sku;

    @Column(name = "barcode")
    private String barcode;

    @Column(name = "meta_title")
    private String metaTitle;

    @Column(name = "meta_description")
    private String metaDescription;

    @Column(name = "search_keywords")
    private String searchKeywords;

    @Column(name = "avg_rating")
    private Double avgRating = 0.0;

    @Column(name = "review_count")
    private Integer reviewCount = 0;

    @Column(name = "view_count")
    private Long viewCount = 0L;

    @Column(name = "order_count")
    private Long orderCount = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<CartItem> cartItems = new ArrayList<>();

    public Product(String name, String description, BigDecimal price) {
        this.name = name;
        this.description = description;
        this.price = price;
    }

    public boolean isInStock() {
        return !trackQuantity || stockQuantity > 0 || allowBackorder;
    }

    public boolean isLowStock() {
        return trackQuantity && stockQuantity <= lowStockThreshold;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void updateRating(double newAvgRating, int newReviewCount) {
        this.avgRating = newAvgRating;
        this.reviewCount = newReviewCount;
    }

}