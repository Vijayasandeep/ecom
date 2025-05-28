package com.ecommerce.controller;

import com.ecommerce.entity.Product;
import com.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<Page<Product>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean featured) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> products = productService.getAllProducts(pageable, name, categoryId, minPrice, maxPrice, featured);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Optional<Product> product = productService.getProductById(id);
        return product.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Product>> searchProducts(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productService.searchProducts(query, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<Product>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productService.getProductsByCategory(categoryId, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/featured")
    public ResponseEntity<List<Product>> getFeaturedProducts(
            @RequestParam(defaultValue = "10") int limit) {
        List<Product> products = productService.getFeaturedProducts(limit);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/latest")
    public ResponseEntity<List<Product>> getLatestProducts(
            @RequestParam(defaultValue = "10") int limit) {
        List<Product> products = productService.getLatestProducts(limit);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/best-selling")
    public ResponseEntity<List<Product>> getBestSellingProducts(
            @RequestParam(defaultValue = "10") int limit) {
        List<Product> products = productService.getBestSellingProducts(limit);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}/related")
    public ResponseEntity<List<Product>> getRelatedProducts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "5") int limit) {
        List<Product> products = productService.getRelatedProducts(id, limit);
        return ResponseEntity.ok(products);
    }

    @PostMapping("/{id}/view")
    public ResponseEntity<Void> incrementViewCount(@PathVariable Long id) {
        productService.incrementViewCount(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<Product>> getLowStockProducts() {
        List<Product> products = productService.getLowStockProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/out-of-stock")
    public ResponseEntity<List<Product>> getOutOfStockProducts() {
        List<Product> products = productService.getOutOfStockProducts();
        return ResponseEntity.ok(products);
    }
}