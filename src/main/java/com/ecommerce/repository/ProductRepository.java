package com.ecommerce.repository;

import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAll() ;
    Optional<Product> findById(long id);
    @Query("SELECT DISTINCT p.category.name FROM Product p WHERE p.category IS NOT NULL")
    List<String> findAllDistinctCategoryNames();

}