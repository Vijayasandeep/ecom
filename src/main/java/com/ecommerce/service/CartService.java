package com.ecommerce.service;

import com.ecommerce.controller.CartController.CartSummary;
import com.ecommerce.entity.CartItem;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
@Service
public interface CartService {

    // Basic cart operations
    CartItem addToCart(Long userId, Long productId, int quantity);
    CartItem updateCartItem(Long userId, Long productId, int quantity);
    void removeFromCart(Long userId, Long productId);
    void clearCart(Long userId);

    // Cart information
    List<CartItem> getCartItems(Long userId);
    int getCartItemCount(Long userId);
    BigDecimal getCartTotal(Long userId);
    CartSummary getCartSummary(Long userId);

    // Cart validation
    boolean validateCartItem(Long userId, Long productId, int quantity);
    List<String> validateCart(Long userId);

    // Cart merging (for guest to user conversion)
    void mergeGuestCart(String guestCartId, Long userId);

    // Cart persistence for guests
    List<CartItem> getGuestCartItems(String guestCartId);
    CartItem addToGuestCart(String guestCartId, Long productId, int quantity);
    void updateGuestCartItem(String guestCartId, Long productId, int quantity);
    void removeFromGuestCart(String guestCartId, Long productId);

    // Bulk operations
    void addMultipleToCart(Long userId, List<CartItemRequest> items);
    void updateMultipleCartItems(Long userId, List<CartItemRequest> items);

    // Cart analytics
    BigDecimal getCartSubtotal(Long userId);
    BigDecimal getCartTax(Long userId);
    BigDecimal getCartShipping(Long userId);

    // Cart item helper class
    class CartItemRequest {
        private Long productId;
        private int quantity;

        public CartItemRequest() {}

        public CartItemRequest(Long productId, int quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
}