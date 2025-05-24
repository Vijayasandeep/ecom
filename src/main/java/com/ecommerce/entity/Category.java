package com.ecommerce.entity;

import com.ecommerce.entity.audit.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter

@Table(name = "categories", indexes = {
        @Index(name = "idx_category_name", columnList = "name"),
        @Index(name = "idx_category_slug", columnList = "slug"),
        @Index(name = "idx_category_parent", columnList = "parent_id")
})
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 50, message = "Category name must be between 2 and 50 characters")
    private String name;

    @Column(unique = true, nullable = false)
    private String slug;

    @Column(length = 500)
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "meta_title")
    private String metaTitle;

    @Column(name = "meta_description")
    private String metaDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Category> subcategories = new ArrayList<>();

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Product> products = new ArrayList<>();

    // Constructors, getters, and setters
    public Category() {}

    public Category(String name, String description) {
        this.name = name;
        this.description = description;
        this.slug = generateSlug(name);
    }

    private String generateSlug(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9]", "-").replaceAll("-+", "-");
    }

}
