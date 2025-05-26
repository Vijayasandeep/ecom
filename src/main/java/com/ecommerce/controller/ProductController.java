package com.ecommerce.controller;

import com.ecommerce.entity.Product;
import com.ecommerce.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProductController {
    @Autowired
    private ProductService productService;


    @GetMapping("/products")
    public List<Product> get() {
        return productService.findAllProducts();
    }

    @GetMapping("/products/{id}")
    public Optional<Product> getProduct(@PathVariable long id) {
        return productService.findById(id);
    }
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategoryNames() {
        return ResponseEntity.ok(productService.getAllCategoryNames());
    }

//    @PostMapping("/products")
//    public void post(List<Product> products) {
//        return ;
//    }
}