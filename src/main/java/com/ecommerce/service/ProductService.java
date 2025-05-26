package com.ecommerce.service;

import com.ecommerce.entity.Product;
import com.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    @Autowired
    ProductRepository repository;
    @Autowired
    private ProductRepository productRepository;

    public List<Product> findAllProducts() {
        return repository.findAll();
    }
    public Optional<Product> findById(long id) {
        return repository.findById(id);
    }
    public void saveProd(Product product) {repository.save(product);}

    public void updateProd(long id,Product product) {
        try {
            Product newProduct = repository.findById(id).get();
//            newProduct.setId(product.getId());
            newProduct.setName(product.getName());
            newProduct.setPrice(product.getPrice());
            newProduct.setCategory(product.getCategory());
            newProduct.setDescription(product.getDescription());
            repository.save(newProduct);
        } catch (Exception ignored) {
        }
    }

    public void deleteProduct(long id) {
        try {
            repository.deleteById(id);
        }
        catch (Exception ignored) {

        }
    }
    public List<String> getAllCategoryNames() {
        return productRepository.findAllDistinctCategoryNames();
    }
}