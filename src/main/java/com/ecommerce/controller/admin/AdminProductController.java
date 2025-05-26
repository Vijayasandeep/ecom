package com.ecommerce.controller.admin;

import com.ecommerce.entity.Product;
import com.ecommerce.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminProductController {
    @Autowired
    ProductService productService;

    @GetMapping("")
    public List<Product> getProducts() {
        return productService.findAllProducts();
    }
    @PostMapping("/products")
    public ResponseEntity<String> saveProduct(Product product) {
        productService.saveProd(product);
        return ResponseEntity.ok("Product saved successfully!!");
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<String> updateProduct(@PathVariable long id, Product product) {
        productService.updateProd(id,product);
        return ResponseEntity.ok("Product updated successfully!!");
    }
    @DeleteMapping("/products/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok("Product deleted successfully!!");
    }


}