package com.ecommerce.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api/order")
public class OrderController {

    @GetMapping("/getAll")
    public ResponseEntity<java.lang.String> get() {
        return ResponseEntity.ok("");
    }

}