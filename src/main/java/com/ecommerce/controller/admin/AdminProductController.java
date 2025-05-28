package com.ecommerce.controller.admin;

import com.ecommerce.entity.Product;
import com.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/products")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    @Autowired
    private ProductService productService;

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody @Valid Product product) {
        Product createdProduct = productService.createProduct(product);
        return ResponseEntity.ok(createdProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody @Valid Product product) {
        Product updatedProduct = productService.updateProduct(id, product);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<Page<Product>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status) {

        Pageable pageable = PageRequest.of(page, size);
        // Custom implementation needed for admin filtering
        Page<Product> products = productService.getAllProducts(pageable, null, null, null, null, null);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/toggle-status")
    public ResponseEntity<Product> toggleProductStatus(@PathVariable Long id) {
        Product product = productService.toggleProductStatus(id);
        return ResponseEntity.ok(product);
    }

    @PostMapping("/{id}/toggle-featured")
    public ResponseEntity<Product> toggleFeaturedStatus(@PathVariable Long id) {
        Product product = productService.toggleFeaturedStatus(id);
        return ResponseEntity.ok(product);
    }

    @PutMapping("/{id}/stock")
    public ResponseEntity<Void> updateStock(
            @PathVariable Long id,
            @RequestBody UpdateStockRequest request) {
        productService.updateStock(id, request.getQuantity());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<Product> addProductImage(
            @PathVariable Long id,
            @RequestBody AddImageRequest request) {
        Product product = productService.addProductImage(id, request.getImageUrl());
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{id}/images")
    public ResponseEntity<Product> removeProductImage(
            @PathVariable Long id,
            @RequestParam String imageUrl) {
        Product product = productService.removeProductImage(id, imageUrl);
        return ResponseEntity.ok(product);
    }

    @PostMapping("/{id}/main-image")
    public ResponseEntity<Product> setMainImage(
            @PathVariable Long id,
            @RequestBody SetMainImageRequest request) {
        Product product = productService.setMainImage(id, request.getImageUrl());
        return ResponseEntity.ok(product);
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

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getProductAnalytics() {
        // Implementation needed in service
        Map<String, Object> analytics = Map.of(
                "totalProducts", 0,
                "activeProducts", 0,
                "lowStockCount", 0,
                "outOfStockCount", 0
        );
        return ResponseEntity.ok(analytics);
    }

    @PostMapping("/bulk-update")
    public ResponseEntity<String> bulkUpdateProducts(@RequestBody BulkUpdateRequest request) {
        // Implementation needed for bulk operations
        return ResponseEntity.ok("Bulk update completed");
    }

    @PostMapping("/bulk-delete")
    public ResponseEntity<String> bulkDeleteProducts(@RequestBody List<Long> productIds) {
        // Implementation needed for bulk deletion
        return ResponseEntity.ok("Bulk delete completed");
    }

    // DTOs for admin product operations
    public static class UpdateStockRequest {
        private int quantity;

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }

    public static class AddImageRequest {
        private String imageUrl;

        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    }

    public static class SetMainImageRequest {
        private String imageUrl;

        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    }

    public static class BulkUpdateRequest {
        private List<Long> productIds;
        private String action;
        private Object value;

        public List<Long> getProductIds() { return productIds; }
        public void setProductIds(List<Long> productIds) { this.productIds = productIds; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public Object getValue() { return value; }
        public void setValue(Object value) { this.value = value; }
    }
}