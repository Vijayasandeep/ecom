package com.ecommerce.service.impl;

import com.ecommerce.controller.CartController.CartSummary;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    private static final BigDecimal TAX_RATE = new BigDecimal("0.08"); // 8% tax
    private static final BigDecimal SHIPPING_THRESHOLD = new BigDecimal("50.00");
    private static final BigDecimal SHIPPING_COST = new BigDecimal("5.99");

    @Override
    public CartItem addToCart(Long userId, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new BadRequestException("Quantity must be greater than 0");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!product.getActive()) {
            throw new BadRequestException("Product is not available");
        }

        // Check stock availability
        if (product.getTrackQuantity() && !product.getAllowBackorder() &&
                product.getStockQuantity() < quantity) {
            throw new BadRequestException("Insufficient stock available");
        }

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cartItemRepository.findByUserIdAndProductId(userId, productId);

        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            int newQuantity = cartItem.getQuantity() + quantity;

            // Validate new quantity
            if (product.getTrackQuantity() && !product.getAllowBackorder() &&
                    product.getStockQuantity() < newQuantity) {
                throw new BadRequestException("Cannot add more items. Insufficient stock available");
            }

            cartItem.setQuantity(newQuantity);
            return cartItemRepository.save(cartItem);
        } else {
            CartItem cartItem = new CartItem(product, user, quantity);
            return cartItemRepository.save(cartItem);
        }
    }

    @Override
    public CartItem updateCartItem(Long userId, Long productId, int quantity) {
        if (quantity < 0) {
            throw new BadRequestException("Quantity cannot be negative");
        }

        CartItem cartItem = cartItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (quantity == 0) {
            cartItemRepository.delete(cartItem);
            return null;
        }

        Product product = cartItem.getProduct();

        // Check stock availability
        if (product.getTrackQuantity() && !product.getAllowBackorder() &&
                product.getStockQuantity() < quantity) {
            throw new BadRequestException("Insufficient stock available");
        }

        cartItem.setQuantity(quantity);
        return cartItemRepository.save(cartItem);
    }

    @Override
    public void removeFromCart(Long userId, Long productId) {
        CartItem cartItem = cartItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        cartItemRepository.delete(cartItem);
    }

    @Override
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartItem> getCartItems(Long userId) {
        return cartItemRepository.findByUserIdOrderByIdDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public int getCartItemCount(Long userId) {
        return cartItemRepository.countByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getCartTotal(Long userId) {
        List<CartItem> cartItems = getCartItems(userId);
        BigDecimal subtotal = calculateSubtotal(cartItems);
        BigDecimal tax = calculateTax(subtotal);
        BigDecimal shipping = calculateShipping(subtotal);

        return subtotal.add(tax).add(shipping);
    }

    @Override
    @Transactional(readOnly = true)
    public CartSummary getCartSummary(Long userId) {
        List<CartItem> cartItems = getCartItems(userId);
        int totalItems = cartItems.stream().mapToInt(CartItem::getQuantity).sum();

        BigDecimal subtotal = calculateSubtotal(cartItems);
        BigDecimal tax = calculateTax(subtotal);
        BigDecimal shipping = calculateShipping(subtotal);
        BigDecimal total = subtotal.add(tax).add(shipping);

        return new CartSummary(cartItems, totalItems, subtotal, tax, shipping, total);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateCartItem(Long userId, Long productId, int quantity) {
        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            if (!product.getActive()) {
                return false;
            }

            if (product.getTrackQuantity() && !product.getAllowBackorder() &&
                    product.getStockQuantity() < quantity) {
                return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> validateCart(Long userId) {
        List<String> errors = new ArrayList<>();
        List<CartItem> cartItems = getCartItems(userId);

        for (CartItem item : cartItems) {
            Product product = item.getProduct();

            if (!product.getActive()) {
                errors.add("Product '" + product.getName() + "' is no longer available");
                continue;
            }

            if (product.getTrackQuantity() && !product.getAllowBackorder() &&
                    product.getStockQuantity() < item.getQuantity()) {
                errors.add("Only " + product.getStockQuantity() + " units available for '" + product.getName() + "'");
            }
        }

        return errors;
    }

    @Override
    public void mergeGuestCart(String guestCartId, Long userId) {
        // Implementation for merging guest cart to user cart
        // This would typically involve fetching guest cart items from cache/session
        // and adding them to the user's cart
        List<CartItem> guestItems = getGuestCartItems(guestCartId);

        for (CartItem guestItem : guestItems) {
            try {
                addToCart(userId, guestItem.getProduct().getId(), guestItem.getQuantity());
            } catch (Exception e) {
                // Log error but continue with other items
                System.err.println("Failed to merge cart item: " + e.getMessage());
            }
        }

        // Clear guest cart after merging
        // clearGuestCart(guestCartId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartItem> getGuestCartItems(String guestCartId) {
        // Implementation for guest cart items
        // This would typically fetch from Redis or session storage
        return new ArrayList<>(); // Placeholder
    }

    @Override
    public CartItem addToGuestCart(String guestCartId, Long productId, int quantity) {
        // Implementation for guest cart
        // This would typically store in Redis or session storage
        return null; // Placeholder
    }

    @Override
    public void updateGuestCartItem(String guestCartId, Long productId, int quantity) {
        // Implementation for guest cart updates
        // This would typically update Redis or session storage
    }

    @Override
    public void removeFromGuestCart(String guestCartId, Long productId) {
        // Implementation for guest cart item removal
        // This would typically remove from Redis or session storage
    }

    @Override
    public void addMultipleToCart(Long userId, List<CartItemRequest> items) {
        for (CartItemRequest item : items) {
            try {
                addToCart(userId, item.getProductId(), item.getQuantity());
            } catch (Exception e) {
                // Log error but continue with other items
                System.err.println("Failed to add item to cart: " + e.getMessage());
            }
        }
    }

    @Override
    public void updateMultipleCartItems(Long userId, List<CartItemRequest> items) {
        for (CartItemRequest item : items) {
            try {
                updateCartItem(userId, item.getProductId(), item.getQuantity());
            } catch (Exception e) {
                // Log error but continue with other items
                System.err.println("Failed to update cart item: " + e.getMessage());
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getCartSubtotal(Long userId) {
        List<CartItem> cartItems = getCartItems(userId);
        return calculateSubtotal(cartItems);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getCartTax(Long userId) {
        BigDecimal subtotal = getCartSubtotal(userId);
        return calculateTax(subtotal);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getCartShipping(Long userId) {
        BigDecimal subtotal = getCartSubtotal(userId);
        return calculateShipping(subtotal);
    }

    // Helper methods
    private BigDecimal calculateSubtotal(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateTax(BigDecimal subtotal) {
        return subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateShipping(BigDecimal subtotal) {
        return subtotal.compareTo(SHIPPING_THRESHOLD) >= 0 ?
                BigDecimal.ZERO : SHIPPING_COST;
    }
}