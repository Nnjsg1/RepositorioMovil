package com.levelup.backend.repository;

import com.levelup.backend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findByCategory_Id(Integer categoryId);
    List<Product> findByTitleContainingIgnoreCase(String title);
}
