package com.ecommerce.service.impl;

import com.ecommerce.entity.Product;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Product updateProduct(Long id, Product product) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setComparePrice(product.getComparePrice());
        existingProduct.setCostPrice(product.getCostPrice());
        existingProduct.setStockQuantity(product.getStockQuantity());
        existingProduct.setCategory(product.getCategory());
        existingProduct.setImageUrl(product.getImageUrl());
        existingProduct.setActive(product.getActive());
        existingProduct.setFeatured(product.getFeatured());
        existingProduct.setWeight(product.getWeight());
        existingProduct.setDimensions(product.getDimensions());
        existingProduct.setSku(product.getSku());
        existingProduct.setBarcode(product.getBarcode());
        existingProduct.setMetaTitle(product.getMetaTitle());
        existingProduct.setMetaDescription(product.getMetaDescription());
        existingProduct.setSearchKeywords(product.getSearchKeywords());

        return productRepository.save(existingProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        productRepository.delete(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(Pageable pageable, String name, Long categoryId,
                                        BigDecimal minPrice, BigDecimal maxPrice, Boolean featured) {
        return productRepository.findProductsWithFilters(name, categoryId, minPrice, maxPrice, featured, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> searchProducts(String query, Pageable pageable) {
        return productRepository.searchProducts(query, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> getProductsByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getFeaturedProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findByFeaturedTrueAndActiveTrue(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getLatestProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());
        return productRepository.findByActiveTrue(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getBestSellingProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findBestSellingProducts(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getRelatedProducts(Long productId, int limit) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findRelatedProducts(product.getCategory().getId(), productId, pageable);
    }

    @Override
    public void incrementViewCount(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        product.setViewCount(product.getViewCount() + 1);
        productRepository.save(product);
    }

    @Override
    public void updateRating(Long productId, double newRating, int reviewCount) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        product.setAvgRating(newRating);
        product.setReviewCount(reviewCount);
        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getOutOfStockProducts() {
        return productRepository.findByStockQuantityAndTrackQuantityTrue(0);
    }

    @Override
    public void updateStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        product.setStockQuantity(quantity);
        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isInStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        return !product.getTrackQuantity() ||
                product.getStockQuantity() >= quantity ||
                product.getAllowBackorder();
    }

    @Override
    public Product addProductImage(Long productId, String imageUrl) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        product.getImages().add(imageUrl);
        return productRepository.save(product);
    }

    @Override
    public Product removeProductImage(Long productId, String imageUrl) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        product.getImages().remove(imageUrl);
        return productRepository.save(product);
    }

    @Override
    public Product setMainImage(Long productId, String imageUrl) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        product.setImageUrl(imageUrl);
        return productRepository.save(product);
    }

    @Override
    public Product toggleProductStatus(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        product.setActive(!product.getActive());
        return productRepository.save(product);
    }

    @Override
    public Product toggleFeaturedStatus(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        product.setFeatured(!product.getFeatured());
        return productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getProductsForAdmin(String status, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        if ("active".equals(status)) {
            return productRepository.findByActiveTrue(sort);
        } else if ("inactive".equals(status)) {
            return productRepository.findByActiveFalse(sort);
        } else {
            return productRepository.findAll(sort);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getTrendingProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findTrendingProducts(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getRecommendedProducts(Long userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findRecommendedProducts(userId, pageable);
    }
}