package com.ecommerce.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @GetMapping("/hello")
    public ResponseEntity<java.lang.String> get() {
        return ResponseEntity.ok("myna it is working fine");
    }
}