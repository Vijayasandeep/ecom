package com.ecommerce.controller;

import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.User;
import com.ecommerce.service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/cart")
@CrossOrigin(origins = "*")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public ResponseEntity<List<CartItem>> getCartItems(@AuthenticationPrincipal User user) {
        List<CartItem> cartItems = cartService.getCartItems(user.getId());
        return ResponseEntity.ok(cartItems);
    }

    @PostMapping("/add")
    public ResponseEntity<CartItem> addToCart(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid AddToCartRequest request) {
        CartItem cartItem = cartService.addToCart(user.getId(), request.getProductId(), request.getQuantity());
        return ResponseEntity.ok(cartItem);
    }

    @PutMapping("/update/{productId}")
    public ResponseEntity<CartItem> updateCartItem(
            @AuthenticationPrincipal User user,
            @PathVariable Long productId,
            @RequestBody @Valid UpdateCartRequest request) {
        CartItem cartItem = cartService.updateCartItem(user.getId(), productId, request.getQuantity());
        return ResponseEntity.ok(cartItem);
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<Void> removeFromCart(
            @AuthenticationPrincipal User user,
            @PathVariable Long productId) {
        cartService.removeFromCart(user.getId(), productId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal User user) {
        cartService.clearCart(user.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> getCartItemCount(@AuthenticationPrincipal User user) {
        int count = cartService.getCartItemCount(user.getId());
        return ResponseEntity.ok(count);
    }

    @GetMapping("/total")
    public ResponseEntity<BigDecimal> getCartTotal(@AuthenticationPrincipal User user) {
        BigDecimal total = cartService.getCartTotal(user.getId());
        return ResponseEntity.ok(total);
    }

    @GetMapping("/summary")
    public ResponseEntity<CartSummary> getCartSummary(@AuthenticationPrincipal User user) {
        CartSummary summary = cartService.getCartSummary(user.getId());
        return ResponseEntity.ok(summary);
    }

    // DTOs for cart operations
    public static class AddToCartRequest {
        private Long productId;
        private int quantity = 1;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }

    public static class UpdateCartRequest {
        private int quantity;

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }

    public static class CartSummary {
        private List<CartItem> items;
        private int totalItems;
        private BigDecimal subtotal;
        private BigDecimal tax;
        private BigDecimal shipping;
        private BigDecimal total;

        // Constructors, getters, and setters
        public CartSummary(List<CartItem> items, int totalItems, BigDecimal subtotal,
                           BigDecimal tax, BigDecimal shipping, BigDecimal total) {
            this.items = items;
            this.totalItems = totalItems;
            this.subtotal = subtotal;
            this.tax = tax;
            this.shipping = shipping;
            this.total = total;
        }

        public List<CartItem> getItems() { return items; }
        public void setItems(List<CartItem> items) { this.items = items; }
        public int getTotalItems() { return totalItems; }
        public void setTotalItems(int totalItems) { this.totalItems = totalItems; }
        public BigDecimal getSubtotal() { return subtotal; }
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
        public BigDecimal getTax() { return tax; }
        public void setTax(BigDecimal tax) { this.tax = tax; }
        public BigDecimal getShipping() { return shipping; }
        public void setShipping(BigDecimal shipping) { this.shipping = shipping; }
        public BigDecimal getTotal() { return total; }
        public void setTotal(BigDecimal total) { this.total = total; }
    }
}